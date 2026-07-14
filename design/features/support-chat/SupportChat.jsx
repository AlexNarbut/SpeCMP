// SupportChat.jsx — Pandora Spec support-chat controller.
//   • SupportChatApp — live, interactive screen. Owns the message list, draft,
//     pending attachments, delivery statuses (sent → read), the "specialist is
//     typing…" simulation and the offline queue. Presentational blocks come
//     from SupportChatScreens.jsx.
//
// scenario prop (host toggle):
//   "dialog"  — full demo history, live sending.
//   "empty"   — no messages yet (empty state), first send starts the dialog.
//   "loading" — history skeleton ~1.8s, then the dialog appears.
//   "offline" — no-connection banner; sends queue as "pending" until Повторить.

const SCS = window.SupportChatScreens;
const { ChatScreen, SC_HISTORY, SC_AGENTS, SC_REPLIES, SC_MOCK_FILES, SC_MOCK_PHOTOS } = SCS;

function scNow() {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function SupportChatApp({
  variant = "tonal",
  scenario = "dialog",
  platform = "mobile",
  loadMs = 1800,
}) {
  const { useState, useRef, useEffect } = React;
  const [rows, setRows] = useState(scenario === "empty" ? [] : SC_HISTORY);
  const [loading, setLoading] = useState(scenario === "loading");
  const [offline, setOffline] = useState(scenario === "offline");
  const [typing, setTyping] = useState(null);
  const [draft, setDraft] = useState("");
  const [attachments, setAttachments] = useState([]);
  const seq = useRef(0);
  const replyIdx = useRef(0);
  const fileIdx = useRef(0);
  const photoIdx = useRef(0);
  const timers = useRef([]);
  const later = (fn, ms) => { const t = setTimeout(fn, ms); timers.current.push(t); return t; };
  useEffect(() => () => timers.current.forEach(clearTimeout), []);

  // "loading" — simulate history fetch, then reveal the dialog.
  useEffect(() => {
    if (scenario !== "loading") return;
    later(() => setLoading(false), loadMs);
  }, [scenario, loadMs]);

  const nextId = () => `m${++seq.current}`;
  const setStatus = (ids, status) =>
    setRows((rs) => rs.map((r) => (ids.includes(r.id) ? { ...r, status } : r)));

  // Support side: typing indicator with the specialist's email, then a reply.
  const scheduleReply = () => {
    const author = SC_AGENTS.ivan;
    later(() => setTyping(author), 900);
    later(() => {
      setTyping(null);
      const text = SC_REPLIES[replyIdx.current++ % SC_REPLIES.length];
      setRows((rs) => [...rs, { id: nextId(), from: "support", author, kind: "text", text, time: scNow() }]);
    }, 3200);
  };

  const deliver = (ids) => {
    later(() => setStatus(ids, "sent"), 500);
    later(() => setStatus(ids, "read"), 1700);
    scheduleReply();
  };

  const onSend = () => {
    const text = draft.trim();
    if (!text && attachments.length === 0) return;
    const time = scNow();
    const status = offline ? "pending" : undefined; // undefined → "sent" after delivery tick
    const out = [];
    attachments.forEach((att) => {
      out.push(att.kind === "image"
        ? { id: nextId(), from: "me", kind: "image", image: { name: att.name }, time, status: status || "sent" }
        : { id: nextId(), from: "me", kind: "doc", file: { name: att.name, size: att.size, ext: att.ext }, time, status: status || "sent" });
    });
    if (text) out.push({ id: nextId(), from: "me", kind: "text", text, time, status: status || "sent" });
    setRows((rs) => [...rs, ...out]);
    setDraft("");
    setAttachments([]);
    if (!offline) deliver(out.map((m) => m.id));
  };

  // Offline → Повторить: connection restored, queued messages get delivered.
  const onRetry = () => {
    setOffline(false);
    const ids = [];
    setRows((rs) => {
      rs.forEach((r) => { if (r.status === "pending") ids.push(r.id); });
      return rs;
    });
    later(() => {
      setRows((rs) => {
        const pend = rs.filter((r) => r.status === "pending").map((r) => r.id);
        if (pend.length) {
          later(() => setStatus(pend, "read"), 1400);
          scheduleReply();
          return rs.map((r) => (r.status === "pending" ? { ...r, status: "sent" } : r));
        }
        return rs;
      });
    }, 600);
  };

  // Mock pickers (the real app opens system file / photo pickers).
  const onAttachFile = () => {
    const f = SC_MOCK_FILES[fileIdx.current++ % SC_MOCK_FILES.length];
    setAttachments((a) => [...a, { id: `a${Date.now()}${a.length}`, kind: "doc", ...f }]);
  };
  const onAttachPhoto = () => {
    const p = SC_MOCK_PHOTOS[photoIdx.current++ % SC_MOCK_PHOTOS.length];
    setAttachments((a) => [...a, { id: `a${Date.now()}${a.length}`, kind: "image", ...p }]);
  };
  const onRemoveAttachment = (id) => setAttachments((a) => a.filter((x) => x.id !== id));

  const onBack = () => {
    const key = platform === "desktop" ? "home-desktop" : platform === "tablet" ? "home-tablet" : "home";
    if (window.psHas && window.psHas(key)) window.psGo(key);
  };

  return (
    <ChatScreen
      variant={variant}
      rows={rows}
      typing={typing}
      loading={loading}
      offline={offline}
      onRetry={onRetry}
      onBack={onBack}
      draft={draft}
      onDraft={setDraft}
      onSend={onSend}
      attachments={attachments}
      onAttachFile={onAttachFile}
      onAttachPhoto={onAttachPhoto}
      onRemoveAttachment={onRemoveAttachment}
      isDesktop={platform === "desktop"}
    />
  );
}

window.SupportChatApp = SupportChatApp;
