---
name: prompt-loop
description: Enhance-and-confirm workflow for user prompts in this repo, plus a self-improving preference log. Triggered automatically via the UserPromptSubmit hook's additionalContext — you don't need to invoke this manually, just follow it when the hook's instruction appears in context.
---

# Prompt Improvement Loop

This repo auto-injects an enhance-and-confirm instruction on (most) user messages via
`.claude/skills/prompt-loop/hooks/on-submit.sh`. This file defines exactly what to do
when that instruction appears in your context.

Technique reference: metaprompting — https://claude.vn/articles/metaprompt-dung-claude-d%E1%BB%83-vi%E1%BA%BFt-prompt-cho-claude

## Step 1 — Is this a fresh prompt or a reply to a pending proposal?

Look at your own previous turn in this conversation.

- If your previous message ended with an enhancement proposal that was waiting for
  confirmation (i.e. you asked "xác nhận / sửa / giữ nguyên bản gốc?" and had not yet
  started the real task) — then this new user message is the **reply** to that
  proposal. Do NOT enhance it again. Go straight to Step 3 using this message as the
  user's final decision.
- Otherwise, this is a **fresh prompt**. Continue to Step 2.

## Step 2 — Propose an enhanced version

Using the metaprompt technique, produce an improved version of the user's prompt:
clarify the goal, fill in context the user likely assumed (project conventions from
CLAUDE.md / `Knowledge/TradeX/`, the relevant feature area), and tighten the structure
(what/why/scope/done-criteria) — without changing the user's actual intent.

Present it in this format, then **stop and wait** — do not start the real task yet:

```
**Bản gốc:** <nguyên văn prompt của user>

**Đề xuất cải thiện:**
<bản đã enhance>

**Lý do:**
- <bullet ngắn 1>
- <bullet ngắn 2>

Xác nhận dùng bản này, sửa lại, hay giữ nguyên bản gốc?
```

Keep "Lý do" to 1-3 short bullets. If the original prompt is already clear and specific,
it's fine to say so and propose using it as-is — don't force a rewrite for its own sake.

## Step 3 — Log the signal and proceed

Once the user responds (confirm / edited version / "giữ nguyên bản gốc"):

1. Determine `edit_type`: `"accepted"` (used the proposal as-is), `"edited"` (user
   changed it), or `"kept_original"` (user chose the original wording).
2. Ensure the data directory exists: `mkdir -p .claude/skills/prompt-loop/data`
3. Append one line to `.claude/skills/prompt-loop/data/learnings.jsonl`:

```json
{"timestamp": "<ISO 8601 UTC, e.g. date -u +%Y-%m-%dT%H:%M:%SZ>", "original": "<bản gốc>", "ai_draft": "<bản đề xuất>", "user_final": "<bản user chốt>", "edit_type": "accepted|edited|kept_original"}
```

Use the Bash tool to append (e.g. `printf '%s\n' '<json>' >> .claude/skills/prompt-loop/data/learnings.jsonl`), not the Write tool (which would overwrite the file).

4. Proceed with the actual task using `user_final`.

## Step 4 — Track outcomes

If, later in the *same* task, the user asks you to redo/correct the work in a way that
suggests the enhanced prompt missed something, append one more line to the same
`learnings.jsonl` file:

```json
{"timestamp": "<ISO 8601 UTC>", "event": "outcome", "note": "<1 câu mô tả điều bị thiếu/sai>", "ref_timestamp": "<timestamp của entry gốc ở Step 3>"}
```

## Step 5 — Suggest distillation periodically

Keep a rough mental count of new `learnings.jsonl` lines since the last time
`data/learnings.md` was updated (check its content/timestamp if present). Once roughly
15 new lines have accumulated, mention to the user: "Đã tích được ~15 tín hiệu mới, bạn
muốn chạy `/prompt-loop-review` để chưng cất thành quy tắc không?" Don't run the
distillation yourself outside of that command — keep the log format and the
distillation step decoupled.

## Sensitive data

Never copy verbatim customer data, secrets, or account numbers into `data/learnings.md`
during distillation (that file is committed to git). Abstracted preference statements
only (e.g. "user thường rút gọn phần bullet trang trọng"), never the literal sensitive
prompt content.

## Opting out

- A single message: prefix with `!raw` to skip enhancement for just that message.
- Repo-wide, temporarily: `touch .claude/skills/prompt-loop/data/disabled` (delete the
  file to re-enable).
