import contextlib
import importlib.util
import io
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

from support import ROOT

FILE = "feature/foo/src/commonMain/kotlin/Foo.kt"


def load_ui_audit():
    spec = importlib.util.spec_from_file_location("ui_audit", ROOT / "scripts/ui-audit/ui_audit.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class UiAuditTest(unittest.TestCase):

    def setUp(self) -> None:
        self.root = Path(tempfile.mkdtemp(prefix="ui-audit-test-")).resolve()
        self.addCleanup(shutil.rmtree, self.root, ignore_errors=True)
        self.audit = load_ui_audit()
        self.audit.ROOT = self.root

    def write(self, rel: str, text: str) -> Path:
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
        return path

    def rules(self, text: str, rel: str = FILE) -> list[str]:
        return [f["rule"] for f in self.audit.audit_file(self.write(rel, text))]

    def quiet(self, call, *args) -> int:
        with contextlib.redirect_stdout(io.StringIO()):
            return call(*args)

    def test_detects_each_rule(self):
        cases = {
            "val c = Color(0xFF00FF00)": ["hex_color"],
            "val c = Color.White": ["raw_color"],
            "val c = Color(0.1f, 0.2f, 0.3f)": ["raw_color"],
            "val s = 14.sp": ["raw_sp"],
            "val t = TextStyle(fontSize = size)": ["bare_text_style", "text_size_arg"],
            "val t = TextStyle()": ["bare_text_style"],
            "val s = size.sp": ["raw_sp"],
            "base.copy(lineHeight = h)": ["text_size_arg"],
            "fun t(size: TextUnit) = Unit": ["text_unit"],
            "val s = RoundedCornerShape(12.dp)": ["raw_shape"],
            "val s = RoundedCornerShape(topStart = 8.dp)": ["raw_shape"],
            "import androidx.compose.material3.Text": ["material_import"],
            "import androidx.compose.foundation.Image": ["ui_component_import"],
            "import coil3.compose.AsyncImage": ["coil_import"],
        }
        for line, expected in cases.items():
            with self.subTest(line=line):
                self.assertEqual(expected, self.rules(line + "\n"))

    def test_allowed_constructs_are_not_findings(self):
        text = "\n".join([
            "val a = Color.Transparent",
            "val b = Color.Unspecified",
            "val c = CmpTextStyle.Body.copy(letterSpacing = 0.5.sp)",
            "val c2 = x.copy(letterSpacing = spacing.sp)",
            "val w = SpanStyle(fontWeight = FontWeight.Bold)",
            "val sp = spacing",
            "val r = RoundedCornerShape(CmpRadius.cardMedium)",
            "val r = RoundedCornerShape(percent = 50)",
            "val d = MyTextStyle(x)",
            "// val e = 12.sp",
            "import androidx.compose.foundation.layout.Row",
        ])

        self.assertEqual([], self.rules(text))

    def test_ignore_comments_on_line_previous_line_and_file(self):
        self.assertEqual([], self.rules("val c = Color.Red // ui-audit: ignore raw_color — причина\n"))
        self.assertEqual([], self.rules("// ui-audit: ignore raw_color — причина\nval c = Color.Red\n"))
        self.assertEqual(["hex_color"], self.rules("// ui-audit: ignore raw_color — причина\nval c = Color(0xFF000000)\n"))
        self.assertEqual([], self.rules("// ui-audit: ignore-file raw_color,hex_color — причина\n\nval c = Color.Red\n"
                                        "val d = Color(0xFF000000)\n"))

    def test_text_rules_ignore_comments_do_not_apply(self):
        cases = [
            "val s = 14.sp // ui-audit: ignore raw_sp — причина\n",
            "// ui-audit: ignore text_size_arg — причина\nval t = x.copy(fontSize = s)\n",
            "// ui-audit: ignore-file text_unit,bare_text_style — причина\nval u: TextUnit = TextStyle().fontSize\n",
        ]
        for text in cases:
            with self.subTest(text=text):
                self.assertTrue(set(self.rules(text)) & self.audit.STRICT)

    def test_text_rules_cannot_be_baselined(self):
        path = self.write(FILE, "val a = 1.sp\nval t = x.copy(fontSize = s)\n")
        base = {FILE: {"raw_sp": 9, "text_size_arg": 9}}

        self.assertEqual(1, self.quiet(self.audit.check_file, str(path), base))
        self.assertEqual(1, self.quiet(self.audit.diff, self.audit.run(["feature"]), base))

    def test_write_baseline_skips_text_rules(self):
        self.write(FILE, "val a = 1.sp\nval c = Color.Red\n")
        self.audit.BASELINE = self.root / "baseline.json"
        with contextlib.redirect_stdout(io.StringIO()):
            argv, sys.argv = sys.argv, ["ui_audit.py", "--write-baseline"]
            try:
                self.audit.main()
            finally:
                sys.argv = argv
        written = (self.root / "baseline.json").read_text(encoding="utf-8")
        self.assertIn("raw_color", written)
        self.assertNotIn("raw_sp", written)

    def test_core_modules_are_audited_except_design_system(self):
        self.assertEqual(["raw_sp"], self.rules("val s = 1.sp\n", "core/barcode/src/commonMain/kotlin/A.kt"))
        self.assertFalse(self.audit.audited(self.root / "core/ui/src/commonMain/kotlin/A.kt"))
        self.assertFalse(self.audit.audited(self.root / "core/testing/src/commonMain/kotlin/A.kt"))

    def test_coil_rule_applies_only_to_features(self):
        self.assertEqual([], self.rules("import coil3.compose.AsyncImage\n", "composeApp/src/commonMain/kotlin/A.kt"))

    def test_scope_excludes_tests_testing_modules_and_build_outputs(self):
        audited = self.audit.audited
        self.assertTrue(audited(self.root / FILE))
        for rel in ("feature/foo/src/commonTest/kotlin/FooTest.kt", "feature/foo/src/test/kotlin/Foo.kt",
                    "feature/foo/testing/src/commonMain/kotlin/Fake.kt", "feature/foo/build/gen/src/Foo.kt",
                    "core/ui/src/commonMain/kotlin/Foo.kt", "feature/foo/src/commonMain/kotlin/Foo.kts"):
            with self.subTest(rel=rel):
                self.assertFalse(audited(self.root / rel))

    def test_check_file_fails_only_on_growth_over_baseline(self):
        path = self.write(FILE, "val a = Color.Red\nval b = Color.Blue\n")

        self.assertEqual(0, self.quiet(self.audit.check_file, str(path), {FILE: {"raw_color": 2}}))
        self.assertEqual(1, self.quiet(self.audit.check_file, str(path), {FILE: {"raw_color": 1}}))
        self.assertEqual(1, self.quiet(self.audit.check_file, str(path), {}))

    def test_diff_compares_by_module(self):
        self.write(FILE, "val a = Color(0xFF000000)\n")
        self.write("feature/foo/src/commonMain/kotlin/Bar.kt", "val c = Color.Red\n")
        findings = self.audit.run(["feature"])

        moved = {"feature/foo/src/commonMain/kotlin/Old.kt": {"hex_color": 1, "raw_color": 1}}
        self.assertEqual(0, self.quiet(self.audit.diff, findings, moved))
        self.assertEqual(1, self.quiet(self.audit.diff, findings, {FILE: {"hex_color": 1}}))
        self.assertEqual(0, self.quiet(self.audit.diff, findings, {FILE: {"hex_color": 5, "raw_color": 1}}))
