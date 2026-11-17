// SupportChatScreens — presentational support-chat screen for Pandora Spec,
// built ONLY from design-system Cmp* components (window.PandoraSpecDesignSystem_b8d6e1).
//
// The screen is a support conversation: top app bar + message history + composer.
// Incoming messages are labelled with the support specialist's EMAIL (specialists
// rotate, the email identifies who is answering right now). Outgoing messages
// carry delivery status: sent (✓) / read (✓✓) / pending (clock, offline queue).
//
// Bubble variants (host toggle): "tonal" (filled containers) | "flat" (outlined).
//
// Adaptivity — Window Size Classes by container width (CLAUDE.md):
//   • compact  (<600)   — full-width column, bubbles ≤78%.
//   • medium   (600–839)— column max-width 720, centered.
//   • expanded (≥840)   — column max-width 840, centered.
// Touch targets ≥48dp everywhere; correct in light and dark themes.

const SC_DS = window.PandoraSpecDesignSystem_b8d6e1;
const {
  CmpTopAppBar, CmpIconButton, CmpText, CmpIcon, CmpSurface,
  CmpLinearProgressIndicator,
} = SC_DS;

// ── Icons (Material Icons filled ligatures / catalog names) ──────────────────
const SC_ICON = {
  back: "ArrowBack",
  attachFile: "attach_file",
  attachPhoto: "image",
  send: "Send",
  doc: "description",
  photo: "image",
  pending: "Schedule",
  sent: "Done",
  read: "done_all",
  offline: "wifi_off",
  empty: "forum",
  close: "Close",
  support: "support_agent",
};

// ── Demo data ─────────────────────────────────────────────────────────────────
// Support side of the dialog: specialists rotate; each message carries the
// email of the person who wrote it.
const SC_AGENTS = {
  elena: "elena.support@pandora.pro",
  ivan: "ivan.support@pandora.pro",
};

// Flat list; rows are either { kind: "date", label } or message objects:
// { id, from: "me"|"support", author?, kind: "text"|"doc"|"image",
//   text?, file?: {name,size,ext}, image?: {name, caption?}, time, status? }
const SC_HISTORY = [
  { kind: "date", label: "12 июля" },
  { id: "h1", from: "me", kind: "text", text: "Здравствуйте! Pandora DX-4GS, Kia Sportage 2024. После монтажа не читается CAN-шина — статус «нет связи».", time: "14:02", status: "read" },
  { id: "h2", from: "me", kind: "doc", file: { name: "монтажная_схема_sportage.pdf", size: "2,4 МБ", ext: "PDF" }, time: "14:03", status: "read" },
  { id: "h3", from: "support", author: SC_AGENTS.elena, kind: "text", text: "Здравствуйте! Схему получила, посмотрю. Уточните, пожалуйста, куда подключали CAN-High — к разъёму OBD или к жгуту за блоком BCM?", time: "14:11" },
  { kind: "date", label: "Сегодня" },
  { id: "h4", from: "support", author: SC_AGENTS.ivan, kind: "text", text: "Добрый день, я продолжаю по вашей заявке. Подключение по схеме корректное. Пришлите, пожалуйста, фото точки подключения CAN-шины.", time: "09:40" },
  { id: "h5", from: "me", kind: "image", image: { name: "IMG_4312.jpg", caption: "Точка подключения за блоком BCM" }, time: "09:52", status: "read" },
  { id: "h6", from: "me", kind: "text", text: "Прикрепил фото. Провод — коричневый с белой полосой, скрутка пропаяна.", time: "09:53", status: "sent" },
];

// Cycled mock replies for the live prototype.
const SC_REPLIES = [
  "Принято, проверяю по схеме подключения. Ответ дам в течение 10 минут.",
  "Спасибо, информации достаточно. Передал вопрос инженеру, он подключится к чату.",
  "Проверьте, пожалуйста, версию прошивки базового блока — обновление вышло вчера.",
];

// Cycled mock attachments (the real app opens a file/photo picker).
const SC_MOCK_FILES = [
  { name: "схема_подключения.pdf", size: "1,8 МБ", ext: "PDF" },
  { name: "лог_ошибок_can.txt", size: "64 КБ", ext: "TXT" },
  { name: "акт_установки.docx", size: "312 КБ", ext: "DOCX" },
];
const SC_MOCK_PHOTOS = [
  { name: "IMG_4313.jpg" },
  { name: "IMG_4314.jpg" },
  { name: "IMG_4315.jpg" },
];

// ── Container-width hook → Window Size Class ─────────────────────────────────
function useChatMode() {
  const ref = React.useRef(null);
  const [w, setW] = React.useState(0);
  React.useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const remeasure = () => setW(el.offsetWidth);
    const ro = new ResizeObserver(remeasure);
    ro.observe(el);
    window.addEventListener("resize", remeasure);
    remeasure();
    return () => { ro.disconnect(); window.removeEventListener("resize", remeasure); };
  }, []);
  let mode = "compact";
  if (w >= 840) mode = "expanded";
  else if (w >= 600) mode = "medium";
  return [ref, mode];
}

function chatColumnMax(mode) {
  return mode === "expanded" ? 840 : mode === "medium" ? 720 : "none";
}

// ── Date divider ──────────────────────────────────────────────────────────────
function DateDivider({ label }) {
  return (
    <div style={{ display: "flex", justifyContent: "center", padding: "6px 0" }}>
      <div style={{ padding: "6px 14px", borderRadius: 999, background: "var(--surface-variant)" }}>
        <CmpText variant="labelMedium" color="var(--on-surface-variant)">{label}</CmpText>
      </div>
    </div>
  );
}

// ── Delivery status (outgoing only) ──────────────────────────────────────────
function MsgStatus({ status, tone = "var(--on-surface-variant)" }) {
  if (!status) return null;
  const icon = status === "read" ? SC_ICON.read : status === "pending" ? SC_ICON.pending : SC_ICON.sent;
  const color = status === "read" ? "var(--primary)" : tone;
  const label = status === "read" ? "Прочитано" : status === "pending" ? "Ожидает отправки" : "Отправлено";
  return <CmpIcon icon={icon} size={16} color={color} title={label} />;
}

// Time + status row, bottom-right inside a bubble.
function MetaRow({ time, status, mine, tone }) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "flex-end", gap: 4, marginTop: 2 }}>
      <CmpText variant="labelSmall" color={tone}>{time}</CmpText>
      {mine && <MsgStatus status={status} tone={tone} />}
    </div>
  );
}

// ── Attachments ───────────────────────────────────────────────────────────────
function DocAttachment({ file }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 12, minWidth: 200 }}>
      <div style={{ width: 44, height: 44, borderRadius: "var(--radius-s)", background: "color-mix(in srgb, currentColor 12%, transparent)", display: "grid", placeItems: "center", flex: "0 0 auto" }}>
        <CmpIcon icon={SC_ICON.doc} size={24} color="currentColor" />
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 2, minWidth: 0 }}>
        <CmpText variant="bodyMedium" weight={500} maxLines={1} color="currentColor" style={{ wordBreak: "break-all" }}>{file.name}</CmpText>
        <CmpText variant="labelSmall" color="color-mix(in srgb, currentColor 70%, transparent)">{file.size}{file.ext ? ` · ${file.ext}` : ""}</CmpText>
      </div>
    </div>
  );
}

// Image placeholder (real app renders the photo; mockup uses a striped slot).
function ImageAttachment({ image, width = 260, height = 170 }) {
  return (
    <div
      aria-label={`Фото ${image.name}`}
      style={{
        width, maxWidth: "100%", height, borderRadius: "var(--radius-m)",
        background: "repeating-linear-gradient(45deg, color-mix(in srgb, var(--on-surface) 7%, transparent) 0 12px, transparent 12px 24px), var(--surface-variant)",
        border: "1px solid var(--border-highlight)",
        display: "grid", placeItems: "center", boxSizing: "border-box",
      }}
    >
      <div style={{ display: "flex", alignItems: "center", gap: 6, padding: "6px 12px", borderRadius: 999, background: "color-mix(in srgb, var(--surface) 82%, transparent)" }}>
        <CmpIcon icon={SC_ICON.photo} size={16} color="var(--on-surface-variant)" />
        <CmpText variant="labelMedium" color="var(--on-surface-variant)" style={{ fontFamily: "var(--font-mono)" }}>{image.name}</CmpText>
      </div>
    </div>
  );
}

// ── Message bubble ────────────────────────────────────────────────────────────
// variant: "tonal" (filled) | "flat" (outlined, transparent)
function MessageBubble({ msg, variant = "tonal" }) {
  const mine = msg.from === "me";
  const tonal = variant === "tonal";
  const isImage = msg.kind === "image";

  const bg = !tonal ? "transparent" : mine ? "var(--primary-container)" : "var(--background-surface-card)";
  const fg = tonal && mine ? "var(--on-primary-container)" : "var(--on-surface)";
  const metaTone = tonal && mine
    ? "color-mix(in srgb, var(--on-primary-container) 70%, transparent)"
    : "var(--on-surface-variant)";
  const border = !tonal
    ? `1px solid ${mine ? "color-mix(in srgb, var(--primary) 45%, transparent)" : "var(--outline-variant)"}`
    : (mine ? "none" : "1px solid var(--border-highlight)");

  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: mine ? "flex-end" : "flex-start", gap: 3 }}>
      {!mine && msg.author && (
        <div style={{ padding: "0 6px" }}>
          <CmpText variant="labelMedium" color="var(--primary)">{msg.author}</CmpText>
        </div>
      )}
      <div
        style={{
          maxWidth: "min(560px, 82%)",
          boxSizing: "border-box",
          padding: isImage ? "6px 6px 8px" : "10px 14px 8px",
          borderRadius: "var(--r-card-medium-radius)",
          borderBottomRightRadius: mine ? 4 : 16,
          borderBottomLeftRadius: mine ? 16 : 4,
          background: bg,
          color: fg,
          border,
        }}
      >
        {msg.kind === "text" && (
          <CmpText variant="bodyLarge" color="currentColor" style={{ textWrap: "pretty", overflowWrap: "anywhere" }}>{msg.text}</CmpText>
        )}
        {msg.kind === "doc" && <DocAttachment file={msg.file} />}
        {msg.kind === "image" && (
          <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
            <ImageAttachment image={msg.image} />
            {msg.image.caption && (
              <div style={{ padding: "0 8px" }}>
                <CmpText variant="bodyMedium" color="currentColor" style={{ textWrap: "pretty" }}>{msg.image.caption}</CmpText>
              </div>
            )}
          </div>
        )}
        <div style={{ padding: isImage ? "0 8px" : 0 }}>
          <MetaRow time={msg.time} status={msg.status} mine={mine} tone={metaTone} />
        </div>
      </div>
    </div>
  );
}

// ── Typing indicator — “<email> печатает…” + animated dots ───────────────────
function TypingIndicator({ author, variant = "tonal" }) {
  const tonal = variant === "tonal";
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-start", gap: 3 }}>
      <div style={{ padding: "0 6px" }}>
        <CmpText variant="labelMedium" color="var(--primary)">{author} · печатает…</CmpText>
      </div>
      <div
        aria-label={`${author} печатает сообщение`}
        style={{
          padding: "14px 18px",
          borderRadius: "var(--r-card-medium-radius)", borderBottomLeftRadius: 4,
          background: tonal ? "var(--background-surface-card)" : "transparent",
          border: "1px solid var(--border-highlight)",
          display: "flex", gap: 5, alignItems: "center",
        }}
      >
        {[0, 1, 2].map((i) => (
          <span key={i} className="sc-dot ps-anim" style={{ width: 8, height: 8, borderRadius: 999, background: "var(--on-surface-variant)", animationDelay: `${i * 0.18}s` }} />
        ))}
      </div>
    </div>
  );
}

// ── Empty state ───────────────────────────────────────────────────────────────
// Ghost mini-dialog: a quiet preview of what the conversation will look like.
function GhostBubble({ mine = false, w = "62%", delay = 0 }) {
  return (
    <div className="sc-float ps-anim" style={{ display: "flex", justifyContent: mine ? "flex-end" : "flex-start", animationDelay: `${delay}s` }}>
      <div
        style={{
          width: w, height: 34, boxSizing: "border-box",
          borderRadius: "var(--r-card-small-radius)",
          borderBottomRightRadius: mine ? 4 : 14,
          borderBottomLeftRadius: mine ? 14 : 4,
          background: mine
            ? "color-mix(in srgb, var(--primary) 12%, transparent)"
            : "color-mix(in srgb, var(--on-surface) 7%, transparent)",
          border: `1px dashed ${mine ? "color-mix(in srgb, var(--primary) 35%, transparent)" : "var(--outline-variant)"}`,
          display: "flex", alignItems: "center", padding: "0 12px", gap: 6,
          color: "var(--on-surface)",
        }}
      >
        {[0.5, 0.3, 0.4].map((op, i) => (
          <span key={i} style={{ height: 6, borderRadius: 999, flex: `${op} 1 0`, background: "currentColor", opacity: 0.18 }} />
        ))}
      </div>
    </div>
  );
}

function EmptyHintChip({ icon, label }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 8, padding: "8px 14px", borderRadius: 999, background: "var(--background-surface-card)", border: "1px solid var(--border-highlight)" }}>
      <CmpIcon icon={icon} size={18} color="var(--primary)" />
      <CmpText variant="labelLarge" color="var(--on-surface-variant)">{label}</CmpText>
    </div>
  );
}

function EmptyChat() {
  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 22, padding: "40px 24px", textAlign: "center" }}>
      {/* Ghost preview of the future dialog */}
      <div aria-hidden="true" style={{ width: "min(300px, 82%)", display: "flex", flexDirection: "column", gap: 8, position: "relative", paddingBottom: 6 }}>
        <GhostBubble mine w="58%" delay={0} />
        <GhostBubble w="70%" delay={0.35} />
        <GhostBubble mine w="44%" delay={0.7} />
        <div
          className="sc-float ps-anim"
          style={{
            position: "absolute", right: "8%", bottom: -14, animationDelay: "0.5s",
            width: 56, height: 56, borderRadius: "var(--r-card-medium-radius)",
            background: "var(--primary-container)",
            border: "1px solid color-mix(in srgb, var(--primary) 25%, transparent)",
            boxShadow: "var(--elevation-2)",
            display: "grid", placeItems: "center",
          }}
        >
          <CmpIcon icon={SC_ICON.support} size={30} color="var(--on-primary-container)" />
        </div>
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 8, maxWidth: 340 }}>
        <CmpText variant="titleLarge" weight={600} align="center">Сообщений пока нет</CmpText>
        <CmpText variant="bodyMedium" color="var(--on-surface-variant)" align="center" style={{ textWrap: "pretty" }}>
          Опишите проблему — специалист техподдержки ответит в этом чате.
        </CmpText>
      </div>
      <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", gap: 8 }}>
        <EmptyHintChip icon={SC_ICON.attachFile} label="Документы" />
        <EmptyHintChip icon={SC_ICON.photo} label="Фото монтажа" />
      </div>
    </div>
  );
}

// ── History-loading skeleton ─────────────────────────────────────────────────
function BubbleSkeleton({ mine = false, w = 240, h = 64 }) {
  return (
    <div style={{ display: "flex", justifyContent: mine ? "flex-end" : "flex-start" }}>
      <div className="ps-skel" style={{ width: w, maxWidth: "78%", height: h, borderRadius: "var(--r-card-medium-radius)", borderBottomRightRadius: mine ? 4 : 16, borderBottomLeftRadius: mine ? 16 : 4 }} />
    </div>
  );
}

function ChatSkeleton() {
  return (
    <div aria-busy="true" style={{ display: "flex", flexDirection: "column", gap: 12, paddingTop: 8 }}>
      <div style={{ display: "flex", justifyContent: "center" }}>
        <div className="ps-skel" style={{ width: 96, height: 28, borderRadius: 999 }} />
      </div>
      <BubbleSkeleton mine w={280} h={72} />
      <BubbleSkeleton mine w={220} h={56} />
      <BubbleSkeleton w={320} h={88} />
      <BubbleSkeleton mine w={260} h={180} />
      <BubbleSkeleton w={300} h={72} />
    </div>
  );
}

// ── Offline banner ────────────────────────────────────────────────────────────
function OfflineBanner({ onRetry }) {
  return (
    <div role="status" style={{ flex: "0 0 auto", display: "flex", alignItems: "center", gap: 12, padding: "10px 16px", background: "var(--error-container)", color: "var(--on-error-container)" }}>
      <CmpIcon icon={SC_ICON.offline} size={22} color="currentColor" />
      <div style={{ flex: 1, minWidth: 0 }}>
        <CmpText variant="bodyMedium" weight={500} color="currentColor" style={{ textWrap: "pretty" }}>
          Нет соединения. Сообщения будут отправлены при подключении.
        </CmpText>
      </div>
      {onRetry && (
        <CmpSurface
          onClick={onRetry}
          color="color-mix(in srgb, currentColor 12%, transparent)"
          contentColor="currentColor"
          radius={999}
          className="ps-focusable"
          role="button"
          tabIndex={0}
          style={{ minHeight: 44, display: "flex", alignItems: "center", padding: "0 18px", cursor: "pointer", flex: "0 0 auto" }}
        >
          <CmpText variant="labelLarge" weight={600} color="var(--on-error-container)">Повторить</CmpText>
        </CmpSurface>
      )}
    </div>
  );
}

// ── Pending attachment chip (above the input row) ────────────────────────────
function PendingAttachment({ att, onRemove }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "6px 6px 6px 12px", borderRadius: 999, background: "var(--surface-variant)", maxWidth: "100%", boxSizing: "border-box" }}>
      <CmpIcon icon={att.kind === "image" ? SC_ICON.photo : SC_ICON.doc} size={20} color="var(--on-surface-variant)" />
      <div style={{ minWidth: 0 }}>
        <CmpText variant="labelLarge" maxLines={1} style={{ wordBreak: "break-all" }}>{att.name}</CmpText>
      </div>
      {att.size && <CmpText variant="labelMedium" color="var(--on-surface-variant)" style={{ flex: "0 0 auto" }}>{att.size}</CmpText>}
      <CmpIconButton icon={SC_ICON.close} contentDescription={`Убрать ${att.name}`} onClick={onRemove} style={{ width: 40, height: 40 }} />
    </div>
  );
}

// ── Composer: photo + file buttons, auto-growing input, 48dp send ────────────
function Composer({
  value, onChange, onSend, onAttachFile, onAttachPhoto,
  attachments = [], onRemoveAttachment = () => {},
  disabled = false, maxW = "none", isDesktop = false,
}) {
  const taRef = React.useRef(null);
  const canSend = !disabled && (value.trim().length > 0 || attachments.length > 0);

  const autoGrow = () => {
    const ta = taRef.current;
    if (!ta) return;
    ta.style.height = "auto";
    ta.style.height = Math.min(ta.scrollHeight, 120) + "px";
  };
  React.useEffect(autoGrow, [value]);

  const onKeyDown = (e) => {
    // Desktop: Enter sends, Shift+Enter — new line. Mobile keeps Enter as newline.
    if (isDesktop && e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      if (canSend) onSend();
    }
  };

  return (
    <div style={{ flex: "0 0 auto", borderTop: "1px solid var(--border-highlight)", background: "var(--surface)" }}>
      <div style={{ maxWidth: maxW, margin: "0 auto", padding: "8px 8px 10px", boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 8 }}>
        {attachments.length > 0 && (
          <div style={{ display: "flex", flexWrap: "wrap", gap: 8, padding: "2px 4px 0" }}>
            {attachments.map((att) => (
              <PendingAttachment key={att.id} att={att} onRemove={() => onRemoveAttachment(att.id)} />
            ))}
          </div>
        )}
        <div style={{ display: "flex", alignItems: "flex-end", gap: 2 }}>
          <CmpIconButton icon={SC_ICON.attachFile} contentDescription="Прикрепить документ" onClick={onAttachFile} disabled={disabled} />
          <CmpIconButton icon={SC_ICON.attachPhoto} contentDescription="Прикрепить фото" onClick={onAttachPhoto} disabled={disabled} />
          <div style={{ flex: 1, minWidth: 0, display: "flex", alignItems: "center", background: "var(--background-surface-highlight)", borderRadius: "var(--r-card-medium-radius)", padding: "11px 16px", boxSizing: "border-box", minHeight: 48 }}>
            <textarea
              ref={taRef}
              rows={1}
              value={value}
              disabled={disabled}
              placeholder="Сообщение…"
              aria-label="Текст сообщения"
              onChange={(e) => onChange(e.target.value)}
              onKeyDown={onKeyDown}
              style={{
                flex: 1, minWidth: 0, resize: "none", border: "none", outline: "none",
                background: "transparent", color: "var(--on-surface)",
                fontFamily: "var(--font-sans)", fontSize: "var(--body-large-size)",
                lineHeight: "var(--body-large-line)", padding: 0, maxHeight: 120,
              }}
            />
          </div>
          <button
            type="button"
            onClick={canSend ? onSend : undefined}
            disabled={!canSend}
            aria-label="Отправить сообщение"
            className="ps-focusable"
            style={{
              width: 48, height: 48, flex: "0 0 auto", marginLeft: 4,
              border: "none", borderRadius: 999, cursor: canSend ? "pointer" : "default",
              background: canSend ? "var(--primary)" : "color-mix(in srgb, var(--on-surface) 10%, transparent)",
              color: canSend ? "var(--on-primary)" : "color-mix(in srgb, var(--on-surface) 38%, transparent)",
              display: "grid", placeItems: "center", transition: "background var(--motion-fast)",
            }}
          >
            <CmpIcon icon={SC_ICON.send} size={22} color="currentColor" />
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Wallpaper tile: tiny keys scattered at random angles ─────────────────
// Used as a CSS mask, so the tint comes from theme tokens (color-mix below).
// One open-end wrench silhouette in the reference style (round C-head + long
// rounded handle), drawn as two thick strokes; reused 6× per tile.
const SC_KEYS_TILE = `url("data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' width='240' height='240'><defs><g id='k' fill='none' stroke='black' stroke-linecap='round'><line x1='3' y1='10' x2='17' y2='10' stroke-width='5'/><path d='M27.2 6.5 A5.5 5.5 0 1 0 27.2 13.5' stroke-width='4.5'/></g></defs><use href='%23k' transform='translate(22 26) rotate(28)'/><use href='%23k' transform='translate(148 40) rotate(-38)'/><use href='%23k' transform='translate(66 106) rotate(82)'/><use href='%23k' transform='translate(188 128) rotate(-64)'/><use href='%23k' transform='translate(38 184) rotate(12)'/><use href='%23k' transform='translate(136 200) rotate(-18)'/></svg>")`;

// ── Full screen ───────────────────────────────────────────────────────────────
function ChatScreen({
  variant = "tonal",
  rows = [],                 // [{kind:"date"...} | message]
  typing = null,             // email of the specialist typing, or null
  loading = false,
  offline = false,
  onRetry,
  onBack = () => {},
  // composer
  draft = "", onDraft = () => {}, onSend = () => {},
  attachments = [], onAttachFile = () => {}, onAttachPhoto = () => {}, onRemoveAttachment = () => {},
  isDesktop = false,
}) {
  const [rootRef, mode] = useChatMode();
  const listRef = React.useRef(null);
  const maxW = chatColumnMax(mode);
  const pad = mode === "compact" ? 12 : mode === "medium" ? 24 : 32;
  const empty = !loading && rows.length === 0;

  // Keep the newest message in view (plain scrollTop — never scrollIntoView).
  React.useEffect(() => {
    const el = listRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [rows.length, typing, loading]);

  return (
    <div ref={rootRef} data-screen-label="Чат техподдержки" style={{ position: "relative", height: "100%", width: "100%", display: "flex", flexDirection: "column", overflow: "hidden", background: "var(--background)", color: "var(--on-background)" }}>
      <style>{`
        @keyframes scTyping { 0%, 60%, 100% { opacity: .35; transform: translateY(0); } 30% { opacity: 1; transform: translateY(-3px); } }
        @keyframes psShimmer { from { background-position: 100% 0; } to { background-position: 0 0; } }
        .sc-dot { animation: scTyping 1.2s ease-in-out infinite; }
        @keyframes scFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } }
        .sc-float { animation: scFloat 4.5s ease-in-out infinite; }
        .ps-skel { background: linear-gradient(90deg, var(--surface-variant) 25%, var(--surface-container-highest) 37%, var(--surface-variant) 63%); background-size: 400% 100%; animation: psShimmer 1.4s ease infinite; }
        .ps-focusable:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; }
        @media (prefers-reduced-motion: reduce) { .ps-anim, .sc-dot, .ps-skel { animation: none !important; } }
      `}</style>

      <CmpTopAppBar
        centered={isDesktop}
        title="Техподдержка"
        style={{ flex: "0 0 auto" }}
        navigationIcon={<CmpIconButton icon={SC_ICON.back} contentDescription="Назад" onClick={onBack} />}
      />
      {loading && <CmpLinearProgressIndicator />}
      {offline && <OfflineBanner onRetry={onRetry} />}

      <div style={{ flex: 1, minHeight: 0, position: "relative" }}>
        {/* Wallpaper: near-transparent keys, chaotic angles; tint from tokens via mask */}
        <div
          aria-hidden="true"
          style={{
            position: "absolute", inset: 0, pointerEvents: "none",
            background: "color-mix(in srgb, var(--on-surface) 60%, var(--primary))",
            opacity: 0.05,
            WebkitMaskImage: SC_KEYS_TILE, maskImage: SC_KEYS_TILE,
            WebkitMaskSize: "240px 240px", maskSize: "240px 240px",
          }}
        />
        <div
          aria-hidden="true"
          style={{
            position: "absolute", inset: 0, pointerEvents: "none",
            background: "radial-gradient(120% 70% at 50% -20%, color-mix(in srgb, var(--primary) 6%, transparent), transparent 70%)",
          }}
        />
        <div ref={listRef} style={{ position: "absolute", inset: 0, overflow: "auto", display: "flex", flexDirection: "column" }}>
          <div style={{ maxWidth: maxW, width: "100%", margin: "0 auto", padding: `${pad}px ${pad}px 16px`, boxSizing: "border-box", display: "flex", flexDirection: "column", gap: 10, flex: empty ? 1 : "0 0 auto" }}>
            {loading && <ChatSkeleton />}
            {empty && <EmptyChat />}
            {!loading && rows.map((row, i) =>
              row.kind === "date"
                ? <DateDivider key={`d${i}`} label={row.label} />
                : <MessageBubble key={row.id} msg={row} variant={variant} />
            )}
            {!loading && typing && <TypingIndicator author={typing} variant={variant} />}
          </div>
        </div>
      </div>

      <Composer
        value={draft}
        onChange={onDraft}
        onSend={onSend}
        onAttachFile={onAttachFile}
        onAttachPhoto={onAttachPhoto}
        attachments={attachments}
        onRemoveAttachment={onRemoveAttachment}
        disabled={loading}
        maxW={maxW}
        isDesktop={isDesktop}
      />
    </div>
  );
}

window.SupportChatScreens = {
  useChatMode, ChatScreen, Composer, MessageBubble, TypingIndicator,
  DateDivider, MsgStatus, DocAttachment, ImageAttachment,
  EmptyChat, ChatSkeleton, OfflineBanner, PendingAttachment,
  SC_ICON, SC_AGENTS, SC_HISTORY, SC_REPLIES, SC_MOCK_FILES, SC_MOCK_PHOTOS,
};
