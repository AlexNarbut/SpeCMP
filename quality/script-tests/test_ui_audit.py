import contextlib
import importlib.util
import io
import shutil
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
            "val t = TextStyle(fontSize = size)": ["bare_text_style"],
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
            "val d = MyTextStyle(x)",
            "// val e = 12.sp",
            "import androidx.compose.foundation.layout.Row",
        ])

        self.assertEqual([], self.rules(text))

    def test_ignore_comments_on_line_previous_line_and_file(self):
        self.assertEqual([], self.rules("val s = 14.sp // ui-audit: ignore raw_sp — причина\n"))
        self.assertEqual([], self.rules("// ui-audit: ignore raw_sp — причина\nval s = 14.sp\n"))
        self.assertEqual(["raw_color"], self.rules("// ui-audit: ignore raw_sp — причина\nval c = Color.Red\n"))
        self.assertEqual([], self.rules("// ui-audit: ignore-file raw_sp,raw_color — причина\n\nval s = 1.sp\n"
                                        "val c = Color.Red\n"))

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
        path = self.write(FILE, "val a = 1.sp\nval b = 2.sp\n")

        self.assertEqual(0, self.quiet(self.audit.check_file, str(path), {FILE: {"raw_sp": 2}}))
        self.assertEqual(1, self.quiet(self.audit.check_file, str(path), {FILE: {"raw_sp": 1}}))
        self.assertEqual(1, self.quiet(self.audit.check_file, str(path), {}))

    def test_diff_compares_by_module(self):
        self.write(FILE, "val a = 1.sp\n")
        self.write("feature/foo/src/commonMain/kotlin/Bar.kt", "val c = Color.Red\n")
        findings = self.audit.run(["feature"])

        moved = {"feature/foo/src/commonMain/kotlin/Old.kt": {"raw_sp": 1, "raw_color": 1}}
        self.assertEqual(0, self.quiet(self.audit.diff, findings, moved))
        self.assertEqual(1, self.quiet(self.audit.diff, findings, {FILE: {"raw_sp": 1}}))
        self.assertEqual(0, self.quiet(self.audit.diff, findings, {FILE: {"raw_sp": 5, "raw_color": 1}}))
