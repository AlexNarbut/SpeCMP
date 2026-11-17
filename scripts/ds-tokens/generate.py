#!/usr/bin/env python3
"""Kotlin-токены core/ui из CSS-токенов дизайн-системы (design/design-system/tokens/*.css).

Источник истины — CSS макетов; Kotlin-файлы в core/ui/.../theme/tokens/ генерируются и вручную не правятся.
  scripts/ds-tokens/generate.py           — перегенерировать
  scripts/ds-tokens/generate.py --check   — упасть, если сгенерированное разошлось с CSS (pre-commit хук ds-tokens)
Правила разбора и именования — docs/ui-kit.md, «Токены».
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TOKENS = Path("design/design-system/tokens")
OUT = Path("core/ui/src/commonMain/kotlin/ru/alarmtrade/specmp/core/ui/theme/tokens")
PACKAGE = "ru.alarmtrade.specmp.core.ui.theme.tokens"
HEADER = "// Сгенерировано scripts/ds-tokens/generate.py из {src} — не править вручную.\n"

M3_ROLES = [
    "primary", "on-primary", "primary-container", "on-primary-container",
    "inverse-primary",
    "secondary", "on-secondary", "secondary-container", "on-secondary-container",
    "tertiary", "on-tertiary", "tertiary-container", "on-tertiary-container",
    "background", "on-background", "surface", "on-surface", "surface-variant", "on-surface-variant",
    "surface-tint", "inverse-surface", "inverse-on-surface",
    "error", "on-error", "error-container", "on-error-container",
    "outline", "outline-variant", "scrim",
    "surface-bright", "surface-container", "surface-container-high", "surface-container-highest",
    "surface-container-low", "surface-container-lowest", "surface-dim",
]
SEMANTIC_PREFIXES = (
    "brand-", "on-brand", "foreground-", "background-", "border-", "success-", "warning-", "error-",
    "info-", "static-", "bus-",
)
NOT_SEMANTIC = set(M3_ROLES) | {"success", "seed", "shadow"}

TYPE_STYLES = [
    "accent1", "accent2", "accent3", "title1", "title2", "headline1", "headline2", "headline3",
    "paragraph1", "paragraph2", "paragraph3", "additional0", "additional1", "additional2",
]
M3_TYPE_ROLES = [
    "display-large", "display-medium", "display-small", "headline-large", "headline-medium", "headline-small",
    "title-large", "title-medium", "title-small", "body-large", "body-medium", "body-small",
    "label-large", "label-medium", "label-small",
]


class TokenError(Exception):
    pass


def camel(name: str) -> str:
    parts = [p for p in name.split("-") if p]
    return parts[0] + "".join(p[:1].upper() + p[1:] for p in parts[1:])


def strip_comments(css: str) -> str:
    return re.sub(r"/\*.*?\*/", "", css, flags=re.S)


def blocks(css: str) -> list[tuple[str, str]]:
    """Пары (селектор, тело) верхнего уровня; @media разворачивается в селектор с префиксом."""
    css = strip_comments(css)
    result, i = [], 0
    while True:
        start = css.find("{", i)
        if start < 0:
            return result
        selector = css[i:start].strip().split(";")[-1].strip()
        depth, j = 1, start + 1
        while depth:
            if j >= len(css):
                raise TokenError(f"незакрытый блок у «{selector}»")
            depth += {"{": 1, "}": -1}.get(css[j], 0)
            j += 1
        body = css[start + 1:j - 1]
        if selector.startswith("@media"):
            result += [(f"{selector} {inner_sel}", inner) for inner_sel, inner in blocks(body)]
        else:
            result.append((selector, body))
        i = j


def declarations(body: str) -> dict[str, str]:
    return {m.group(1): m.group(2).strip() for m in re.finditer(r"--([a-z0-9-]+)\s*:\s*([^;]+);", body)}


def scopes(css: str) -> dict[str, dict[str, str]]:
    result: dict[str, dict[str, str]] = {}
    for selector, body in blocks(css):
        result.setdefault(selector, {}).update(declarations(body))
    return result


def parse_hex(value: str) -> tuple[int, int, int, int]:
    h = value.lstrip("#")
    if len(h) in (3, 4):
        h = "".join(c * 2 for c in h)
    if len(h) == 6:
        h += "FF"
    if len(h) != 8 or not re.fullmatch(r"[0-9a-fA-F]{8}", h):
        raise TokenError(f"не цвет: {value}")
    return tuple(int(h[k:k + 2], 16) for k in range(0, 8, 2))  # type: ignore[return-value]


def resolve(name: str, scope: dict[str, str], seen: tuple[str, ...] = ()) -> str:
    if name in seen:
        raise TokenError("цикл ссылок: " + " → ".join(seen + (name,)))
    if name not in scope:
        raise TokenError(f"нет токена --{name}")
    value = scope[name]
    return re.sub(r"var\(--([a-z0-9-]+)\)", lambda m: resolve(m.group(1), scope, seen + (name,)), value)


def color(name: str, scope: dict[str, str]) -> tuple[int, int, int, int]:
    value = resolve(name, scope).strip()
    mix = re.fullmatch(r"color-mix\(in srgb,\s*(#[0-9a-fA-F]+)\s+([\d.]+)%,\s*(#[0-9a-fA-F]+)\)", value)
    if mix:
        a, p, b = parse_hex(mix.group(1)), float(mix.group(2)) / 100, parse_hex(mix.group(3))
        return tuple(round(x * p + y * (1 - p)) for x, y in zip(a, b))  # type: ignore[return-value]
    return parse_hex(value)


def kt_color(rgba: tuple[int, int, int, int]) -> str:
    r, g, b, a = rgba
    return f"Color(0x{a:02X}{r:02X}{g:02X}{b:02X})"


def px(name: str, scope: dict[str, str]) -> float:
    value = resolve(name, scope)
    m = re.fullmatch(r"(-?[\d.]+)px", value.strip())
    if not m:
        raise TokenError(f"--{name}: ожидались px, а не «{value}»")
    return float(m.group(1))


def num(v: float) -> str:
    return str(int(v)) if v == int(v) else str(v)


def color_scopes(css: str) -> tuple[dict[str, str], dict[str, str]]:
    sc = scopes(css)
    if ":root" not in sc or '[data-theme="dark"]' not in sc:
        raise TokenError("colors.css: нужны блоки :root и [data-theme=\"dark\"]")
    light = sc[":root"]
    return light, {**light, **sc['[data-theme="dark"]']}


def gen_colors(css: str) -> str:
    light, dark = color_scopes(css)
    semantic = [n for n in light if n.startswith(SEMANTIC_PREFIXES) and n not in NOT_SEMANTIC]
    missing = [r for r in M3_ROLES if r not in light]
    if missing:
        raise TokenError("colors.css: нет M3-ролей " + ", ".join(missing))
    out = [HEADER.format(src=TOKENS / "colors.css"), f"package {PACKAGE}\n",
           "import androidx.compose.material3.ColorScheme",
           "import androidx.compose.material3.darkColorScheme",
           "import androidx.compose.material3.lightColorScheme",
           "import androidx.compose.runtime.Immutable",
           "import androidx.compose.ui.graphics.Color\n",
           "/** Семантические цвета дизайн-системы: имя = CSS-переменная в camelCase (`--border-light` → `borderLight`). */",
           "@Immutable", "class CmpColors internal constructor("]
    out += [f"    val {camel(n)}: Color," for n in semantic]
    out.append(")\n")
    for title, scope in (("Light", light), ("Dark", dark)):
        out.append(f"internal val {title}CmpColors = CmpColors(")
        out += [f"    {camel(n)} = {kt_color(color(n, scope))}," for n in semantic]
        out.append(")\n")
    for title, scope, fn in (("Light", light, "lightColorScheme"), ("Dark", dark, "darkColorScheme")):
        out.append(f"internal val {title}CmpColorScheme: ColorScheme = {fn}(")
        out += [f"    {camel(r)} = {kt_color(color(r, scope))}," for r in M3_ROLES]
        out.append(")\n")
    return "\n".join(out).rstrip() + "\n"


def gen_type(css: str) -> str:
    root = scopes(css).get(":root") or {}
    out = [HEADER.format(src=TOKENS / "typography.css"), f"package {PACKAGE}\n",
           "import androidx.compose.ui.text.font.FontWeight",
           "import androidx.compose.ui.unit.TextUnit",
           "import androidx.compose.ui.unit.sp\n",
           "/** Размер, межстрочный и вес стиля шкалы. Вес: `paragraph*` — Medium, остальные — Bold (typography.css). */",
           "internal class CmpTypeSpec(val size: TextUnit, val lineHeight: TextUnit, val weight: FontWeight)\n",
           "/** Шкала стилей текста — единственные допустимые размеры текста в приложении. */",
           "internal object CmpTypeScale {"]
    for style in TYPE_STYLES:
        size, line = px(f"t-{style}-size", root), px(f"t-{style}-line", root)
        weight = "FontWeight.Medium" if style.startswith("paragraph") else "FontWeight.Bold"
        out.append(f"    val {style} = CmpTypeSpec({num(size)}.sp, {num(line)}.sp, {weight})")
    out.append("}\n")
    out.append("/** Роли Material3 → стиль шкалы (алиасы `--display-large-size` → `--t-*` в typography.css). */")
    out.append("internal val CmpM3TypeAliases: Map<String, CmpTypeSpec> = mapOf(")
    for role in M3_TYPE_ROLES:
        ref = root.get(f"{role}-size", "")
        m = re.fullmatch(r"var\(--t-([a-z0-9]+)-size\)", ref)
        if not m or m.group(1) not in TYPE_STYLES:
            raise TokenError(f"typography.css: --{role}-size должен ссылаться на стиль шкалы, а не «{ref}»")
        out.append(f'    "{camel(role)}" to CmpTypeScale.{m.group(1)},')
    out.append(")")
    return "\n".join(out) + "\n"


def gen_dimens(css: str) -> str:
    sc = scopes(css)
    root = sc.get(":root") or {}
    wide = next((v for k, v in sc.items() if k.startswith("@media") and "600px" in k), {})

    def group(title: str, doc: str, items: list[tuple[str, str]]) -> list[str]:
        lines = [f"/** {doc} */", f"object {title} {{"]
        lines += [f"    val {kt}: Dp = {num(px(css_name, root))}.dp" for kt, css_name in items]
        return lines + ["}\n"]

    def themed_group(title: str, holder: str, local: str, doc: str, items: list[tuple[str, str]]) -> list[str]:
        lines = ["/**", f" * {doc}", f" * Значения — из темы (`{local}`), CSS — по умолчанию.", " */", f"object {title} {{"]
        for kt, _ in items:
            lines += [f"    val {kt}: Dp", "        @Composable @ReadOnlyComposable", f"        get() = {local}.current.{kt}"]
        lines += ["}\n", f"/** Значения `{title}` в теме; по умолчанию — CSS-токены. */", "@Immutable",
                  f"data class {holder}("]
        lines += [f"    val {kt}: Dp = {num(px(css_name, root))}.dp," for kt, css_name in items]
        return lines + [")\n", f"internal val {local} = staticCompositionLocalOf {{ {holder}() }}\n"]

    spacing = [(camel(n), n) for n in root if re.fullmatch(r"space-\d+", n)]
    spacing += [(camel(n), n) for n in ("screen-padding-h", "screen-padding-top", "screen-padding-bottom")]
    radius = [(camel(n[2:-7]), n) for n in root if re.fullmatch(r"r-[a-z-]+-radius", n)]
    width = [(camel(n[2:-6]), n) for n in root if re.fullmatch(r"w-[a-z-]+-width", n)]
    size = [(camel(n), n) for n in root if re.fullmatch(r"icon-(s|m|l|x+l)", n)]
    size += [(camel(n[2:]), n) for n in root if re.fullmatch(r"h-[a-z-]+", n)]
    size += [("touchMin", "touch-min")]
    blur = [(camel(n[5:]), n) for n in root if re.fullmatch(r"blur-[a-z]+", n)]
    out = [HEADER.format(src=TOKENS / "spacing.css"), f"package {PACKAGE}\n",
           "import androidx.compose.animation.core.CubicBezierEasing",
           "import androidx.compose.runtime.Composable",
           "import androidx.compose.runtime.Immutable",
           "import androidx.compose.runtime.ReadOnlyComposable",
           "import androidx.compose.runtime.staticCompositionLocalOf",
           "import androidx.compose.ui.unit.Dp",
           "import androidx.compose.ui.unit.dp\n"]
    out += group("CmpSpacing", "Отступы (сетка 4). `screenPaddingH` — компактное окно, `screenPaddingHMedium` — от 600.",
                 spacing)
    out.insert(len(out) - 1, f"    val screenPaddingHMedium: Dp = {num(px('screen-padding-h', wide))}.dp")
    out += themed_group("CmpRadius", "CmpRadii", "LocalCmpRadii",
                        "Радиусы по типу компонента (`--r-<тип>-radius`); 9999 — пилюля.", radius)
    out += group("CmpBorderWidth", "Толщины рамок (`--w-<тип>-width`); 0 — рамку не рисовать.", width)
    out += group("CmpSize", "Иконки и высоты контролов (зона касания ≥ `touchMin`).", size)
    out += group("CmpBlur", "Радиусы блюра поверхностей.", blur)
    out += ["/** Движение: нажатие и стандартные переходы. */", "object CmpMotion {",
            f"    const val PRESS_SCALE: Float = {float(resolve('press-scale', root))}f"]
    for name in ("motion-standard", "motion-fast", "motion-press"):
        m = re.fullmatch(r"(\d+)ms cubic-bezier\(([^)]+)\)", resolve(name, root).strip())
        if not m:
            raise TokenError(f"--{name}: ожидалось «<N>ms cubic-bezier(a, b, c, d)»")
        key = camel(name[7:])
        args = ", ".join(f"{float(x)}f" for x in m.group(2).split(","))
        out.append(f"    const val {key.upper()}_MS: Int = {m.group(1)}")
        out.append(f"    val {key}Easing: CubicBezierEasing = CubicBezierEasing({args})")
    out.append("}")
    return "\n".join(out) + "\n"


GENERATORS = {
    "CmpColors.kt": ("colors.css", gen_colors),
    "CmpTypeTokens.kt": ("typography.css", gen_type),
    "CmpDimenTokens.kt": ("spacing.css", gen_dimens),
}


def render(root: Path) -> dict[Path, str]:
    return {root / OUT / kt: fn((root / TOKENS / css).read_text(encoding="utf-8"))
            for kt, (css, fn) in GENERATORS.items()}


def main(argv: list[str]) -> int:
    check = "--check" in argv
    try:
        files = render(ROOT)
    except (TokenError, OSError) as e:
        print(f"ds-tokens: {e}", file=sys.stderr)
        return 1
    stale = [p for p, text in files.items() if not p.exists() or p.read_text(encoding="utf-8") != text]
    if check:
        for p in stale:
            print(f"ds-tokens: {p.relative_to(ROOT)} расходится с CSS — запустите scripts/ds-tokens/generate.py",
                  file=sys.stderr)
        return 1 if stale else 0
    for p in stale:
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(files[p], encoding="utf-8")
        print(f"ds-tokens: обновлён {p.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
