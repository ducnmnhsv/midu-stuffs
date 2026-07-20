# Prompt Improvement Loop — Design

**Date:** 2026-07-20
**Scope:** tradex-monitoring repo only (project-scoped)
**Status:** Approved by Midu, ready for implementation planning

## Purpose

Midu writes prompts to Claude Code in this repo (PM/BA-style requests: specs, issues,
PRD edits). This feature automatically proposes an improved version of each prompt
(clearer goal, filled-in context, better structure — per the metaprompt technique:
https://claude.vn/articles/metaprompt-dung-claude-d%E1%BB%83-vi%E1%BA%BFt-prompt-cho-claude),
shows it for confirmation/editing before Claude acts on it, and gets better at proposing
enhancements over time based on how Midu edits or corrects the results.

## Non-goals

- Not a general Claude Code feature — scoped to this repo only.
- Not calling an external LLM API from the hook (no extra cost/latency per message,
  no API key management). The enhancement itself is produced by Claude in-session,
  guided by injected instructions.
- Not a background daemon/observer process. Learning distillation is a slash command,
  run manually or on Claude's suggestion — no PID/process lifecycle to manage.
- Does not touch or extend the existing `continuous-learning-v2` skill/hook pipeline
  (it observes tool calls, not raw prompt text, and changing its invariants is out of
  scope for this feature).

## Architecture

```
User gõ message
   → UserPromptSubmit hook (on-submit.sh)
       - đọc prompt + cwd từ stdin JSON
       - skip nếu: slash command / quá ngắn (một vài từ xác nhận) / có tiền tố !raw
                   / data/disabled tồn tại / thiếu python interpreter
       - đọc data/learnings.md (nếu có) → nhúng vào additionalContext
       - output: hookSpecificOutput.additionalContext = hướng dẫn enhance-và-chờ-confirm
                 + nội dung learnings.md hiện tại
   → Claude nhận prompt gốc + additionalContext
       - Nếu turn trước là một đề xuất enhance đang chờ confirm → coi message này là
         phản hồi cho đề xuất đó, KHÔNG enhance lại (dựa trên đọc lại lịch sử hội thoại,
         không phải state machine trong hook)
       - Ngược lại → áp dụng kỹ thuật metaprompt: hiển thị bản gốc / bản đề xuất / lý do
         enhance, hỏi Midu xác nhận / sửa / giữ nguyên bản gốc
   → Midu xác nhận / sửa / giữ nguyên
       - Claude append 1 dòng JSON vào data/learnings.jsonl:
         {timestamp, original, ai_draft, user_final, edit_type}
       - Tiến hành thực hiện task thật với bản đã chốt
   → (nếu trong task Midu phải yêu cầu sửa lại / làm lại) → Claude append một outcome
     note tham chiếu lại entry ở trên trong data/learnings.jsonl
   → Khi learnings.jsonl tích ~15 entries mới kể từ lần chưng cất trước → Claude chủ
     động gợi ý chạy /prompt-loop-review
       - đọc toàn bộ jsonl, rút ra quy tắc bền vững (vd: "rút ngắn phần bullet trang
         trọng", "luôn thêm mã Jira liên quan"), merge vào data/learnings.md (không
         append vô hạn — loại bỏ quy tắc cũ mâu thuẫn với quy tắc mới hơn)
       - commit data/learnings.md
```

## Components

### `.claude/skills/prompt-loop/SKILL.md`

Documents the enhance-and-confirm workflow for Claude to follow when the hook's
`additionalContext` instruction is present:

1. Detect whether this message is a fresh prompt or a reply to a pending enhancement
   proposal (via conversation history, not hook state).
2. For a fresh prompt: produce an improved version (clarify goal, surface missing
   context using known project conventions from CLAUDE.md/Knowledge, tighten structure).
   Present as: bản gốc / bản đề xuất / lý do (short bullets). Ask Midu to confirm, edit,
   or keep original. Wait — do not proceed to the actual task yet.
3. On response: log to `data/learnings.jsonl`, then proceed with the task using the
   finalized prompt.
4. If Midu later requests a redo/correction within the same task, append an outcome
   note to the same log referencing the earlier entry.
5. Track a running count since last distillation; when it crosses ~15, suggest running
   `/prompt-loop-review`.
6. Never copy verbatim sensitive data (customer info, secrets) from prompts into
   `data/learnings.md` during distillation — only the abstracted preference pattern.

### `.claude/skills/prompt-loop/hooks/on-submit.sh`

`UserPromptSubmit` hook script, modeled on the fail-open style of
`continuous-learning-v2/hooks/observe.sh`:

- Reads stdin JSON (prompt text, cwd, session_id).
- Resolves a python interpreter the same way `observe.sh` does; if none found, `exit 0`
  silently (never blocks the user's prompt).
- Skip conditions (exit 0, no additionalContext) — checked in order:
  - `data/disabled` marker file exists
  - prompt starts with `/` (slash command)
  - prompt starts with `!raw` (explicit bypass for this one message; strip the marker
    is not needed since we just skip enhancement and let the raw prompt through)
  - prompt is very short / a known confirmation word list (ok, yes, đồng ý, tiếp tục,
    etc.) — heuristic, not exhaustive
- Otherwise, reads `data/learnings.md` (if present) and emits JSON with
  `hookSpecificOutput.additionalContext` containing the enhance-and-confirm instruction
  plus the current learned preferences.

### `.claude/commands/prompt-loop-review.md`

Slash command: reads `data/learnings.jsonl`, asks Claude to distill recurring
edit/outcome patterns into an updated `data/learnings.md` (merge, dedupe, drop
superseded rules), then reports a summary of what changed.

### Data files

- `.claude/skills/prompt-loop/data/learnings.jsonl` — raw signal log. **Gitignored.**
  May contain verbatim prompt text.
- `.claude/skills/prompt-loop/data/learnings.md` — distilled, human-readable preference
  rules. **Committed to the repo** so they carry across devices.
- `.claude/skills/prompt-loop/data/disabled` — optional marker file to pause the hook
  entirely. Gitignored (local toggle).

### Registration

Hook added to `.claude/settings.json` under `UserPromptSubmit` (new entry, alongside
existing `Stop`/`PreToolUse`/`PostToolUse` entries — additive, does not touch existing
hooks). `.gitignore` gets an entry for
`.claude/skills/prompt-loop/data/learnings.jsonl` and
`.claude/skills/prompt-loop/data/disabled`.

## Error handling & edge cases

- **Hook failure / missing python**: fail-open, `exit 0`, never blocks the user's
  prompt — same principle as `continuous-learning-v2`.
- **Double-enhancement loop** (hook fires again on the confirmation reply): relying on
  Claude's own reading of conversation history to recognize a pending proposal. If it
  still misfires, `!raw` is the manual escape hatch.
- **learnings.md growing unbounded / self-contradictory**: `/prompt-loop-review` merges
  and dedupes on each run rather than appending indefinitely; newer rules supersede
  older conflicting ones.
- **Sensitive data in learnings**: `learnings.jsonl` (verbatim) stays local only;
  `learnings.md` (committed) must only ever contain abstracted preference statements,
  never verbatim customer/secret data — enforced via SKILL.md instruction during
  distillation.
- **Multi-device sync**: only `learnings.md` is committed; a device with a stale pull
  simply uses the previous version of the learned rules until the next pull — no
  conflict-resolution mechanism needed beyond normal git merge (single author).

## Testing / validation

No unit test framework covers shell hooks in this repo; validate manually:

- Send a slash command → confirm hook skips (no additionalContext / no enhancement
  shown).
- Send a short confirmation word ("ok") → confirm hook skips.
- Send a real task prompt → confirm Claude shows bản gốc/đề xuất/lý do and waits for
  confirmation before starting the task.
- Confirm the enhanced prompt → confirm `learnings.jsonl` gets one new line, and that
  the actual task then proceeds using the finalized prompt.
- Run `git status` after a few turns → confirm `learnings.jsonl` and `disabled` are
  ignored, `learnings.md` is not (if it exists and was intentionally updated).
- Run `/prompt-loop-review` with a handful of manually-seeded log lines → confirm
  `learnings.md` is created/updated with sensible merged rules and no verbatim
  sensitive content leaks in.

## Related

- Metaprompt article (source technique): https://claude.vn/articles/metaprompt-dung-claude-d%E1%BB%83-vi%E1%BA%BFt-prompt-cho-claude
- Existing pattern reference (fail-open hook style, disabled-marker convention):
  `.claude/skills/ecc/continuous-learning-v2/hooks/observe.sh`,
  `.claude/skills/ecc/continuous-learning-v2/config.json`
