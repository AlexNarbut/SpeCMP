import contextlib
import importlib.util
import io
import shutil
import tempfile
import unittest
from pathlib import Path

from support import ROOT


def load_generator():
    spec = importlib.util.spec_from_file_location("ds_tokens", ROOT / "scripts/ds-tokens/generate.py")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


M3 = "\n".join(f"  --{r}: #000000;" for r in load_generator().M3_ROLES)


def colors(root: str, dark: str = "") -> str:
    return f":root {{\n{M3}\n{root}\n}}\n[data-theme=\"dark\"] {{\n{dark}\n}}\n"


class DsTokensTest(unittest.TestCase):

    def setUp(self) -> None:
        self.gen = load_generator()

    def test_repository_tokens_are_in_sync_with_css(self):
        with contextlib.redirect_stderr(io.StringIO()) as err:
            code = self.gen.main(["--check"])
        self.assertEqual(0, code, err.getvalue())

    def test_resolves_alias_chain_and_alpha(self):
        out = self.gen.gen_colors(colors("  --brand-primary-default: #2F6BFF33;\n  --border-focus: var(--brand-primary-default);"))
        self.assertIn("borderFocus = Color(0x332F6BFF)", out)

    def test_dark_overrides_root_and_aliases_resolve_in_dark_scope(self):
        css = colors("  --border-light: #111111;\n  --border-focus: var(--border-light);", "  --border-light: #222222;")
        out = self.gen.gen_colors(css)
        dark = out[out.index("DarkCmpColors"):]
        self.assertIn("borderFocus = Color(0xFF222222)", dark)

    def test_color_mix_is_computed(self):
        css = colors("  --background-surface-card: color-mix(in srgb, #FFFFFF 50%, #000000);")
        self.assertIn("backgroundSurfaceCard = Color(0xFF808080)", self.gen.gen_colors(css))

    def test_three_digit_hex(self):
        self.assertIn("staticBlack = Color(0xFF000000)", self.gen.gen_colors(colors("  --static-black: #000;")))

    def test_m3_aliases_are_not_semantic_fields(self):
        out = self.gen.gen_colors(colors("  --border-light: #111111;"))
        self.assertNotIn("val primary:", out)
        self.assertIn("val borderLight: Color", out)

    def test_missing_m3_role_fails(self):
        with self.assertRaises(self.gen.TokenError):
            self.gen.gen_colors(':root { --primary: #000; }\n[data-theme="dark"] { }')

    def test_alias_cycle_fails(self):
        with self.assertRaises(self.gen.TokenError):
            self.gen.gen_colors(colors("  --border-light: var(--border-focus);\n  --border-focus: var(--border-light);"))

    def test_unknown_reference_fails(self):
        with self.assertRaises(self.gen.TokenError):
            self.gen.gen_colors(colors("  --border-light: var(--nope);"))

    def test_type_alias_must_point_to_scale(self):
        styles = "\n".join(f"  --t-{s}-size: 10px; --t-{s}-line: 12px;" for s in self.gen.TYPE_STYLES)
        roles = "\n".join(f"  --{r}-size: var(--t-title1-size);" for r in self.gen.M3_TYPE_ROLES)
        self.assertIn('"titleLarge" to CmpTypeScale.title1', self.gen.gen_type(f":root {{\n{styles}\n{roles}\n}}"))
        broken = roles.replace("var(--t-title1-size)", "15px", 1)
        with self.assertRaises(self.gen.TokenError):
            self.gen.gen_type(f":root {{\n{styles}\n{broken}\n}}")

    def test_media_query_gives_medium_screen_padding(self):
        css = (ROOT / "design/design-system/tokens/spacing.css").read_text(encoding="utf-8")
        css = css.replace("--screen-padding-h: 32px", "--screen-padding-h: 40px")
        self.assertIn("screenPaddingHMedium: Dp = 40.dp", self.gen.gen_dimens(css))

    def test_radius_reads_theme_and_holder_keeps_css_defaults(self):
        css = (ROOT / "design/design-system/tokens/spacing.css").read_text(encoding="utf-8")
        css = css.replace("--r-buttons-radius: 12px", "--r-buttons-radius: 10px")
        out = self.gen.gen_dimens(css)
        radius = out[out.index("object CmpRadius"):out.index("data class CmpRadii")]
        self.assertIn("    val buttons: Dp\n        @Composable @ReadOnlyComposable\n"
                      "        get() = LocalCmpRadii.current.buttons", radius)
        self.assertNotIn(".dp", radius)
        holder = out[out.index("data class CmpRadii"):]
        self.assertIn("    val buttons: Dp = 10.dp,", holder)
        self.assertIn("internal val LocalCmpRadii = staticCompositionLocalOf { CmpRadii() }", holder)
        spacing = out[out.index("object CmpSpacing"):out.index("object CmpRadius")]
        self.assertNotIn("@Composable", spacing)

    def test_check_mode_detects_stale_file(self):
        tmp = Path(tempfile.mkdtemp(prefix="ds-tokens-test-"))
        self.addCleanup(shutil.rmtree, tmp, ignore_errors=True)
        shutil.copytree(ROOT / "design/design-system/tokens", tmp / "design/design-system/tokens")
        self.gen.ROOT = tmp
        with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
            self.assertEqual(1, self.gen.main(["--check"]))
            self.assertEqual(0, self.gen.main([]))
            self.assertEqual(0, self.gen.main(["--check"]))
            css = tmp / "design/design-system/tokens/colors.css"
            css.write_text(css.read_text(encoding="utf-8").replace("#2F6BFF", "#2F6BFE"), encoding="utf-8")
            self.assertEqual(1, self.gen.main(["--check"]))


if __name__ == "__main__":
    unittest.main()
