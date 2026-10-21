// Хелпер ui.sh для desktop (macOS): дерево доступности окна и ввод через Accessibility + CGEvent.
// Собирается ui.sh лениво (swiftc из Xcode), внешних зависимостей нет.
//
// ax trusted                         — есть ли разрешение Accessibility (exit 1, если нет)
// ax dump <pid>                      — дерево главного окна в JSON формата AXe (координаты от угла окна)
// ax tap|longtap <pid> <x> <y>       — клик по координатам окна
// ax type <pid>                      — текст из stdin: через буфер обмена + Cmd+V
// ax key <pid> enter|escape|tab|backspace|select-all
// ax scroll <pid> up|down|left|right — колесо в центре окна (up — контент едет вверх)
// ax window <pid>                    — CGWindowID окна (для screencapture -l)
import AppKit
import ApplicationServices

func fail(_ message: String) -> Never {
    FileHandle.standardError.write((message + "\n").data(using: .utf8)!)
    exit(1)
}

func attr(_ element: AXUIElement, _ name: String) -> AnyObject? {
    var value: AnyObject?
    return AXUIElementCopyAttributeValue(element, name as CFString, &value) == .success ? value : nil
}

func point(_ element: AXUIElement) -> CGPoint {
    var p = CGPoint.zero
    if let v = attr(element, kAXPositionAttribute) { AXValueGetValue(v as! AXValue, .cgPoint, &p) }
    return p
}

func size(_ element: AXUIElement) -> CGSize {
    var s = CGSize.zero
    if let v = attr(element, kAXSizeAttribute) { AXValueGetValue(v as! AXValue, .cgSize, &s) }
    return s
}

func mainWindow(_ pid: pid_t) -> AXUIElement {
    let app = AXUIElementCreateApplication(pid)
    if let w = attr(app, kAXMainWindowAttribute) { return w as! AXUIElement }
    if let ws = attr(app, kAXWindowsAttribute) as? [AXUIElement], let w = ws.first { return w }
    fail("нет окна у процесса \(pid)")
}

// Роли AX → типы AXe: tree.py разбирает desktop тем же кодом, что и iOS.
let kinds: [String: String] = [
    "AXButton": "Button", "AXMenuButton": "Button", "AXPopUpButton": "Button", "AXLink": "Link",
    "AXTextField": "TextField", "AXSecureTextField": "SecureTextField", "AXTextArea": "TextArea",
    "AXComboBox": "TextField", "AXSearchField": "SearchField",
    "AXCheckBox": "CheckBox", "AXRadioButton": "CheckBox", "AXSwitch": "Switch",
    // AXScrollArea — контейнер, а не элемент: иначе tree.py склеит в его подпись все строки списка.
    "AXSlider": "Slider", "AXImage": "Image",
]
let windowChrome: Set<String> = ["AXCloseButton", "AXMinimizeButton", "AXZoomButton", "AXFullScreenButton"]

func convert(_ e: AXUIElement, origin: CGPoint, depth: Int) -> [String: Any]? {
    guard depth < 60 else { return nil }
    let role = attr(e, kAXRoleAttribute) as? String ?? ""
    if windowChrome.contains(attr(e, kAXSubroleAttribute) as? String ?? "") { return nil }
    let secure = (attr(e, kAXSubroleAttribute) as? String) == "AXSecureTextField"
    var type = secure ? "SecureTextField" : (kinds[role] ?? "Other")
    let title = attr(e, kAXTitleAttribute) as? String ?? ""
    let desc = attr(e, kAXDescriptionAttribute) as? String ?? ""
    let rawValue = attr(e, kAXValueAttribute)
    var value: String? = rawValue.map { "\($0)" }
    var traits: [String] = []
    if type == "CheckBox" || type == "Switch" {
        if value == "1" { traits.append("checked") }
        value = nil
    }
    // Поле ввода Compose Desktop неотличимо от кликабельного текста: AXStaticText + AXPress, в AXValue —
    // плейсхолдер или введённый текст. В дереве оно — btn с этой подписью.
    if role == "AXStaticText" { type = "StaticText" }
    if (attr(e, kAXSelectedAttribute) as? Bool) == true { traits.append("selected") }
    if (attr(e, kAXFocusedAttribute) as? Bool) == true { traits.append("focused") }
    let actions: [String] = {
        var names: CFArray?
        return AXUIElementCopyActionNames(e, &names) == .success ? (names as? [String] ?? []) : []
    }()
    // Compose вешает AXPress только на кликабельные узлы (строки списков приходят как AXStaticText).
    if ["Other", "StaticText", "Image"].contains(type) && actions.contains("AXPress") { type = "Button" }
    var label = desc.isEmpty ? title : desc
    if label.isEmpty, role == "AXStaticText", let v = value { label = v }
    if !["TextField", "SecureTextField", "TextArea", "SearchField", "Slider"].contains(type) || value == label { value = nil }
    let p = point(e), s = size(e)
    let children = (attr(e, kAXChildrenAttribute) as? [AXUIElement] ?? []).compactMap {
        convert($0, origin: origin, depth: depth + 1)
    }
    var node: [String: Any] = [
        "type": type,
        "AXLabel": label,
        "enabled": (attr(e, kAXEnabledAttribute) as? Bool) ?? true,
        "traits": traits,
        "frame": ["x": p.x - origin.x, "y": p.y - origin.y, "width": s.width, "height": s.height],
        "children": children,
    ]
    if let v = value, !v.isEmpty { node["AXValue"] = v }
    if let id = attr(e, "AXIdentifier") as? String, !id.isEmpty { node["AXUniqueId"] = id }
    return node
}

func dump(_ pid: pid_t) {
    let window = mainWindow(pid)
    let origin = point(window), s = size(window)
    let children = (attr(window, kAXChildrenAttribute) as? [AXUIElement] ?? []).compactMap {
        convert($0, origin: origin, depth: 1)
    }.filter { !(($0["type"] as? String) == "StaticText" && ($0["AXLabel"] as? String) == (attr(window, kAXTitleAttribute) as? String)) }
    let app: [String: Any] = [
        "type": "Application",
        "AXLabel": attr(window, kAXTitleAttribute) as? String ?? "",
        "frame": ["x": 0, "y": 0, "width": s.width, "height": s.height],
        "children": children,
    ]
    let data = try! JSONSerialization.data(withJSONObject: [app], options: [])
    FileHandle.standardOutput.write(data)
}

// Клик и клавиши уходят в активное окно — поднимаем его перед каждым действием.
func focus(_ pid: pid_t) -> AXUIElement {
    let window = mainWindow(pid)
    NSRunningApplication(processIdentifier: pid)?.activate(options: [])
    AXUIElementPerformAction(window, kAXRaiseAction as CFString)
    usleep(150_000)
    return window
}

func click(_ pid: pid_t, _ x: Double, _ y: Double, hold: useconds_t) {
    let o = point(focus(pid))
    let at = CGPoint(x: o.x + x, y: o.y + y)
    let src = CGEventSource(stateID: .hidSystemState)
    CGEvent(mouseEventSource: src, mouseType: .mouseMoved, mouseCursorPosition: at, mouseButton: .left)?.post(tap: .cghidEventTap)
    usleep(50_000)
    CGEvent(mouseEventSource: src, mouseType: .leftMouseDown, mouseCursorPosition: at, mouseButton: .left)?.post(tap: .cghidEventTap)
    usleep(hold)
    CGEvent(mouseEventSource: src, mouseType: .leftMouseUp, mouseCursorPosition: at, mouseButton: .left)?.post(tap: .cghidEventTap)
}

func press(_ code: CGKeyCode, _ flags: CGEventFlags = []) {
    let src = CGEventSource(stateID: .hidSystemState)
    for down in [true, false] {
        let e = CGEvent(keyboardEventSource: src, virtualKey: code, keyDown: down)
        e?.flags = flags
        e?.post(tap: .cghidEventTap)
        usleep(30_000)
    }
}

func typeText(_ pid: pid_t) {
    let text = String(data: FileHandle.standardInput.readDataToEndOfFile(), encoding: .utf8) ?? ""
    let board = NSPasteboard.general
    let saved = board.string(forType: .string)
    board.clearContents()
    board.setString(text, forType: .string)
    _ = focus(pid)
    press(9, .maskCommand) // V
    usleep(200_000)
    board.clearContents()
    if let s = saved { board.setString(s, forType: .string) }
}

func scroll(_ pid: pid_t, _ dir: String) {
    let w = focus(pid)
    let o = point(w), s = size(w)
    let center = CGPoint(x: o.x + s.width / 2, y: o.y + s.height / 2)
    let src = CGEventSource(stateID: .hidSystemState)
    CGEvent(mouseEventSource: src, mouseType: .mouseMoved, mouseCursorPosition: center, mouseButton: .left)?.post(tap: .cghidEventTap)
    let step = Int32(s.height * 0.3)
    let (dy, dx): (Int32, Int32) = switch dir {
    case "up": (-step, 0)
    case "down": (step, 0)
    case "left": (0, -step)
    case "right": (0, step)
    default: fail("scroll up|down|left|right")
    }
    CGEvent(scrollWheelEvent2Source: src, units: .pixel, wheelCount: 2, wheel1: dy, wheel2: dx, wheel3: 0)?.post(tap: .cghidEventTap)
}

func windowId(_ pid: pid_t) {
    let list = CGWindowListCopyWindowInfo([.optionOnScreenOnly, .excludeDesktopElements], kCGNullWindowID) as? [[String: Any]] ?? []
    let mine = list.filter { ($0[kCGWindowOwnerPID as String] as? pid_t) == pid && ($0[kCGWindowLayer as String] as? Int) == 0 }
    let area: ([String: Any]) -> Double = {
        let b = $0[kCGWindowBounds as String] as? [String: Double] ?? [:]
        return (b["Width"] ?? 0) * (b["Height"] ?? 0)
    }
    guard let w = mine.max(by: { area($0) < area($1) }), let id = w[kCGWindowNumber as String] as? Int else { fail("окно процесса \(pid) не найдено") }
    print(id)
}

let args = Array(CommandLine.arguments.dropFirst())
guard let cmd = args.first else { fail("ax trusted|dump|tap|longtap|type|key|scroll|window") }
if cmd == "trusted" {
    let ok = AXIsProcessTrusted()
    print(ok)
    exit(ok ? 0 : 1)
}
guard args.count > 1, let pid = pid_t(args[1]) else { fail("ax \(cmd) <pid> …") }
switch cmd {
case "dump": dump(pid)
case "tap", "longtap":
    guard args.count > 3, let x = Double(args[2]), let y = Double(args[3]) else { fail("ax \(cmd) <pid> <x> <y>") }
    click(pid, x, y, hold: cmd == "tap" ? 60_000 : 1_000_000)
case "type": typeText(pid)
case "key":
    _ = focus(pid)
    switch args.count > 2 ? args[2] : "" {
    case "enter": press(36)
    case "escape": press(53)
    case "tab": press(48)
    case "backspace": press(51)
    case "select-all": press(0, .maskCommand)
    default: fail("ax key <pid> enter|escape|tab|backspace|select-all")
    }
case "scroll": scroll(pid, args.count > 2 ? args[2] : "")
case "window": windowId(pid)
default: fail("неизвестная команда \(cmd)")
}
