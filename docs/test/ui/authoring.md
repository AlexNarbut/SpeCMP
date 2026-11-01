# Устройство скилла и правки

## Источник и генерация

Скилл и субагенты ведутся в одном месте — `.ai/skills/ui-test/` — и раскладываются по форматам агентов
командой `.ai/generate.sh`. Сгенерированные файлы вручную не правятся.

```
.ai/skills/ui-test/
  SKILL.md                      оркестратор (инструмент-нейтральный)
  roles/planner.md              промпт ui-test-planner
  roles/runner.md               промпт ui-test-runner
  roles/reviewer.md             промпт ui-test-reviewer
  templates/                    RULES.md, plan.md, run.md, review.md, coder-task.md
  scripts/ui.sh, tree.py, ax.swift
  credentials.properties.example
```

| Результат генерации | Для кого |
|---|---|
| `.agents/skills/ui-test/` — полная копия | Codex, Kimi Code, Gemini CLI (открытый стандарт Agent Skills); runtime-путь для всех агентов |
| `.claude/skills/ui-test/SKILL.md` | Claude Code |
| `.claude/agents/ui-test-*.md` | субагенты Claude Code (`tools`, `model`) |
| `.codex/agents/ui-test-*.toml` | субагенты Codex (`developer_instructions`, `model_reasoning_effort`) |
| `.agents/agents/ui-test-*.md` | субагенты Kimi Code |
| `.gemini/agents/ui-test-*.md` | субагенты Gemini CLI (`max_turns`, `timeout_mins`) |

Все пути в промптах указывают на `.agents/skills/ui-test/…` — эта копия есть у любого агента.

Генератор владеет папками `.claude/skills`, `.claude/agents`, `.agents/skills`, `.agents/agents`, `.codex/agents`,
`.gemini/agents`: перед генерацией очищает их и печатает `- removed <путь>` для всего, чего нет в `.ai/skills`
(скилл или субагент в обход `.ai/skills` не переживёт генерацию). Локальные `*.properties` (креды `ui-test`,
`perf-bench`) не трогает. Непустые `.grok/skills` / `.grok/agents` — предупреждение: Grok читает `.claude/`.

pre-commit-хук `ai-generate` (`.pre-commit-config.yaml`) при изменениях в `.ai/`, сгенерированных папках или
`CLAUDE.md` / `AGENTS.md` / `GEMINI.md` запускает `.ai/generate.sh --check`: коммит падает, если сгенерированное
разошлось с `.ai/` (ручная правка, забыли перегенерировать) или новые сгенерированные файлы не добавлены в git.
Исправление — `.ai/generate.sh`, `git add` результата, повторить коммит.

## Frontmatter роли

```yaml
---
name: ui-test-runner
description: …                 # всем агентам
claude_model: haiku            # → model (Claude)
claude_tools: Bash, Read, …    # → tools (Claude)
codex_effort: low              # → model_reasoning_effort (Codex; модель наследуется)
gemini_max_turns: 150          # → max_turns (Gemini)
gemini_timeout_mins: 40        # → timeout_mins (Gemini)
---
```

## Как править

1. Изменить файлы в `.ai/skills/ui-test/`.
2. `.ai/generate.sh`.
3. Для `ui.sh` / `tree.py` / `ax.swift` — `bash -n`, `ui.sh doctor <platform>` и короткий прогон
   `tree` / `tap` / `expect` на каждой затронутой платформе.
4. Новые агенты подхватывают изменения после перезапуска.

Новый скилл — папка `.ai/skills/<skill>/` с `SKILL.md` (и при необходимости `roles/*.md`): генератор
обработает её так же.

## Адаптация под другой проект

Проектная специфика сосредоточена в `ui.sh` (app id, задачи сборки, `IOS_PRODUCT`, `DESKTOP_MAIN`), в путях к
коду и строкам в `roles/*.md` и в ключах `credentials.properties.example`.
