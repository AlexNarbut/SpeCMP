#!/usr/bin/env python3
"""Компактное дерево экрана из uiautomator XML (Android) или AXe JSON (iOS; desktop — ax.swift в том же формате).

tree.py render <android|ios|desktop> <dump> <cache.json> [--all] [--screen WxH]  — печать дерева, индексы пишутся в cache.json
tree.py find   <android|ios|desktop> <dump> <query> [cache.json] [--screen WxH]  — "x y" центра совпадения, exit 1 если нет
tree.py state  <android|ios|desktop> <dump> <query> [--screen WxH]               — «kind\tlabel\tflags» элемента, exit 1 если нет
--all: и безымянные контейнеры с id, и элементы за пределами экрана.
query: @N (индекс из последнего render), #id (testTag / accessibilityIdentifier, допускает glob: #*back_button),
иначе текст.
"""
import fnmatch
import json
import re
import sys
import xml.etree.ElementTree as ET

MAX_LABEL = 80


class Node:
    def __init__(self, kind, label, node_id, frame, flags, children):
        self.kind = kind
        self.label = label
        self.node_id = node_id
        self.frame = frame  # x, y, w, h
        self.flags = flags
        self.children = children

    @property
    def center(self):
        x, y, w, h = self.frame
        return round(x + w / 2), round(y + h / 2)

    @property
    def interactive(self):
        return self.kind in ("btn", "input", "check", "switch", "slider", "scroll")


def parse_android(path):
    # `uiautomator dump /dev/tty` дописывает после XML строку «UI hierchary dumped to…».
    text = open(path, encoding="utf-8").read()
    root = ET.fromstring(text[: text.rfind("</hierarchy>") + len("</hierarchy>")])
    package = None

    def convert(el):
        nonlocal package
        a = el.attrib
        package = package or a.get("package")
        x1, y1, x2, y2 = map(int, re.findall(r"-?\d+", a.get("bounds", "[0,0][0,0]")))
        cls = a.get("class", "")
        if "EditText" in cls:
            kind = "input"
        elif a.get("checkable") == "true":
            kind = "switch" if "Switch" in cls else "check"
        elif a.get("clickable") == "true" or a.get("long-clickable") == "true":
            kind = "btn"
        elif a.get("scrollable") == "true":
            kind = "scroll"
        elif "Image" in cls:
            kind = "img"
        else:
            kind = "text"
        node_id = a.get("resource-id", "")
        node_id = "" if node_id.startswith("android:") else node_id.split(":id/")[-1]
        label = a.get("text") or a.get("content-desc") or ""
        if kind == "input" and not a.get("text") and a.get("hint"):
            label = ""
        flags = []
        if a.get("enabled") == "false":
            flags.append("disabled")
        if a.get("checked") == "true":
            flags.append("checked")
        if a.get("selected") == "true":
            flags.append("selected")
        if a.get("focused") == "true":
            flags.append("focused")
        if a.get("password") == "true":
            flags.append("password")
        if kind == "input" and a.get("hint"):
            flags.append(f'hint="{a["hint"]}"')
        children = [convert(c) for c in el if c.tag == "node"]
        return Node(kind, label, node_id, (x1, y1, x2 - x1, y2 - y1), flags, children)

    nodes = [convert(c) for c in root if c.tag == "node"]
    return nodes, package


IOS_KINDS = {
    "Button": "btn",
    "Link": "btn",
    "Cell": "btn",
    "TextField": "input",
    "SecureTextField": "input",
    "SearchField": "input",
    "TextView": "input",
    "TextArea": "input",
    "Switch": "switch",
    "CheckBox": "check",
    "Slider": "slider",
    "ScrollView": "scroll",
    "Image": "img",
}


def parse_ios(path):
    data = json.load(open(path))
    app = None

    def convert(el):
        nonlocal app
        if el.get("type") == "Application":
            app = el.get("AXLabel")
        f = el.get("frame") or {}
        kind = IOS_KINDS.get(el.get("type"), "text")
        traits = el.get("traits") or []
        if kind == "text" and ("button" in traits or "link" in traits):
            kind = "btn"
        label = el.get("AXLabel") or ""
        value = el.get("AXValue")
        flags = []
        if kind == "input":
            if value not in (None, ""):
                label = str(value)
            if el.get("type") == "SecureTextField":
                flags.append("password")
        elif value not in (None, "") and str(value) != label:
            flags.append(f"value={value}")
        if not el.get("enabled", True):
            flags.append("disabled")
        # checked / focused приходят только с desktop (ax.swift): AXe их не отдаёт.
        for trait in ("checked", "selected", "focused"):
            if trait in traits:
                flags.append(trait)
        children = [convert(c) for c in el.get("children") or []]
        frame = (f.get("x", 0), f.get("y", 0), f.get("width", 0), f.get("height", 0))
        if el.get("type") == "Application":
            return Node("app", "", "", frame, [], children)
        return Node(kind, label, el.get("AXUniqueId") or "", frame, flags, children)

    return [convert(e) for e in data], app


def has_interactive(node):
    return any(c.interactive or has_interactive(c) for c in node.children)


def descendant_labels(node):
    out = []
    for c in node.children:
        if c.label:
            out.append(c.label)
        out.extend(descendant_labels(c))
    return out


def interactive_descendants(node):
    out = []
    for c in node.children:
        if c.interactive:
            out.append(c)
        out.extend(interactive_descendants(c))
    return out


def inside(inner, outer):
    ix, iy, iw, ih = inner.frame
    ox, oy, ow, oh = outer.frame
    return ix >= ox and iy >= oy and ix + iw <= ox + ow and iy + ih <= oy + oh


def inherit_ids(nodes):
    """testTag обёртки поля переносится на единственный интерактивный элемент в её границах.

    Compose кладёт тег на контейнер; на Android поле — его потомок, на iOS — соседний узел.
    """
    for n in nodes:
        if n.node_id and not n.interactive:
            inner = interactive_descendants(n) or [
                m for m in nodes if m is not n and m.interactive and inside(m, n)
            ]
            if len(inner) == 1 and not inner[0].node_id:
                inner[0].node_id = n.node_id
        inherit_ids(n.children)


def collect(nodes, screen, show_all):
    """Плоский список значимых элементов; тексты, уже вошедшие в подпись предка, отбрасываются."""
    sw, sh = screen
    result = []

    def visible(n):
        x, y, w, h = n.frame
        return w > 0 and h > 0 and x < sw and y < sh and x + w > 0 and y + h > 0

    def walk(n, ancestor_labels):
        label = n.label
        # Кликабельный контейнер с кнопками внутри (диалог, карточка) — не кнопка, а группа.
        if n.kind == "btn" and has_interactive(n):
            n.kind = "group"
        if n.interactive and not label:
            label = " · ".join(dict.fromkeys(descendant_labels(n)))
        # Иконки без подписи (на iOS id — имя SF-символа) и безымянные контейнеры только зашумляют дерево.
        bare_icon = n.kind == "img" and not label
        bare_container = n.kind in ("text", "group") and not label and not show_all
        meaningful = n.kind != "app" and not bare_icon and not bare_container and (label or n.node_id or n.interactive)
        duplicate = n.kind in ("text", "img") and label and not n.node_id and any(
            label in a for a in ancestor_labels
        )
        if meaningful and not duplicate and (show_all or visible(n)):
            result.append((n, label))
            ancestor_labels = ancestor_labels + [label]
        for c in n.children:
            walk(c, ancestor_labels)

    for n in nodes:
        walk(n, [])
    return result


def screen_size(nodes):
    w = max((n.frame[0] + n.frame[2] for n in nodes), default=0)
    h = max((n.frame[1] + n.frame[3] for n in nodes), default=0)
    for n in nodes:
        if n.kind == "app":
            sub = screen_size(n.children)
            w, h = max(w, sub[0]), max(h, sub[1])
    return w, h


def load(platform, dump, screen=None):
    nodes, app = parse_android(dump) if platform == "android" else parse_ios(dump)
    inherit_ids(nodes)
    return nodes, app, screen or screen_size(nodes)


def fmt(i, n, label):
    # uiautomator отдаёт значение поля пароля открытым текстом, а iOS (Compose) не помечает поле секретным вовсе.
    if label and n.kind == "input" and ("password" in n.flags or "password" in n.node_id.lower()):
        label = "•" * len(label)
    s = f"[{i}] {n.kind}"
    if label:
        short = label.replace("\n", " ")
        s += f' "{short[:MAX_LABEL]}{"…" if len(short) > MAX_LABEL else ""}"'
    if n.node_id:
        s += f" #{n.node_id}"
    x, y = n.center
    s += f" @{x},{y}"
    if n.flags:
        s += f" ({', '.join(n.flags)})"
    return s


def render(platform, dump, cache, show_all, screen, keyboard=False):
    nodes, app, screen = load(platform, dump, screen)
    items = collect(nodes, screen, show_all)
    if platform == "ios":
        # При подключённой аппаратной клавиатуре симулятора экранная висит за пределами экрана.
        keyboard = any(n.node_id in ("Return", "shift", "delete") for n, _ in collect(nodes, screen, False))
    print(f"screen {screen[0]}x{screen[1]} {platform} {app or ''}".rstrip())
    if keyboard:
        print("keyboard: открыта и может перекрывать элементы — key hide-keyboard перед тапом вне поля")
    entries = []
    seen = set()
    for n, label in items:
        key = (n.kind, label, n.node_id, n.center)
        if key in seen:
            continue
        seen.add(key)
        print(fmt(len(entries) + 1, n, label))
        entries.append({"label": label, "id": n.node_id, "center": n.center})
    json.dump(entries, open(cache, "w"), ensure_ascii=False)


def find_node(platform, dump, query, cache=None, screen=None):
    """(node, label) лучшего совпадения или None."""
    nodes, _, screen = load(platform, dump, screen)
    # testTag ищется и среди контейнеров без подписи (корень экрана и т.п.).
    items = collect(nodes, screen, query.startswith("#"))
    if query.startswith("@") and query[1:].isdigit() and cache:
        # Индекс валиден, только пока экран тот же, что при последнем render.
        entries = json.load(open(cache))
        idx = int(query[1:]) - 1
        if not 0 <= idx < len(entries):
            return None
        wanted = entries[idx]
        for n, label in items:
            if label == wanted["label"] and n.node_id == wanted["id"]:
                return n, label
        print(f"экран изменился с последнего tree: {query} не найден — вызови tree", file=sys.stderr)
        return None
    if query.startswith("#"):
        matches = [(n, label) for n, label in items if fnmatch.fnmatchcase(n.node_id, query[1:])]
        # Тег обёртки унаследован полем — нужно само поле, а не контейнер.
        matches.sort(key=lambda m: not m[0].interactive)
    else:
        q = query.casefold()
        exact = [(n, label) for n, label in items if label.casefold() == q]
        partial = [(n, label) for n, label in items if q in label.casefold()]
        # Кликабельный элемент предпочтительнее текста внутри него.
        matches = sorted(exact or partial, key=lambda m: not m[0].interactive)
    return matches[0] if matches else None


def find(platform, dump, query, cache=None, screen=None):
    found = find_node(platform, dump, query, cache, screen)
    return found[0].center if found else None


def state(platform, dump, query, screen=None):
    """Строка «kind<TAB>label<TAB>flags» для expect."""
    found = find_node(platform, dump, query, screen=screen)
    if not found:
        return None
    n, label = found
    return f"{n.kind}\t{label}\t{','.join(n.flags)}"


def main():
    args = sys.argv[1:]
    screen = None
    if "--screen" in args:
        i = args.index("--screen")
        screen = tuple(int(v) for v in args[i + 1].split("x"))
        del args[i : i + 2]
    show_all = "--all" in args
    keyboard = "--keyboard" in args
    args = [a for a in args if a not in ("--all", "--keyboard")]
    cmd, platform, dump = args[:3]
    if cmd == "render":
        render(platform, dump, args[3], show_all, screen, keyboard)
    elif cmd == "state":
        line = state(platform, dump, args[3], screen)
        if line is None:
            sys.exit(1)
        print(line)
    elif cmd == "find":
        cache = args[4] if len(args) > 4 else None
        point = find(platform, dump, args[3], cache, screen)
        if not point:
            sys.exit(1)
        print(*point)


if __name__ == "__main__":
    main()
