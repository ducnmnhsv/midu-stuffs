# Prompt Improvement Loop Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a project-scoped `UserPromptSubmit` hook + skill that proposes an improved
version of each user prompt (metaprompt technique), waits for confirmation before Claude
acts, and gets better over time from a committed, distilled preference log.

**Architecture:** A hook script (`on-submit.sh`) reads each submitted prompt, skips
trivial/opted-out messages, and otherwise injects an `additionalContext` instruction
(plus the current learned preferences) telling Claude to run the enhance-and-confirm
workflow documented in `SKILL.md`. Signals from user edits/outcomes are logged to a
local (gitignored) JSONL file; a manual slash command distills that log into a
committed Markdown preferences file, which the hook feeds back into future turns.

**Tech Stack:** Bash + Python 3 (hook script, matching the fail-open style already used
by `.claude/skills/ecc/continuous-learning-v2/hooks/observe.sh`), Markdown (skill +
command definitions), Claude Code hooks (`.claude/settings.json`).

## Global Constraints

- Scoped to this repo only (`tradex-monitoring`) — do not touch global `~/.claude/` config.
- Fail open: any hook error must never block the user's actual prompt (`exit 0` on any
  unexpected condition, matching `continuous-learning-v2/hooks/observe.sh`'s convention).
- No extra LLM API calls from the hook — enhancement is produced by Claude in-session.
- No background daemon/observer process — distillation is a manual/suggested slash command.
- Per this repo's CLAUDE.md rule "No auto-commit/push": nothing in this feature may run
  `git commit` on the user's behalf. Every task that touches `data/learnings.md` must
  present the change and tell the user to review + commit manually.
- `data/learnings.jsonl` and `data/disabled` are gitignored (may contain verbatim prompt
  text / are a local toggle). `data/learnings.md` is committed (syncs across devices).

---

### Task 1: Hook script — skip heuristics + additionalContext emission

**Files:**
- Create: `.claude/skills/prompt-loop/hooks/on-submit.sh`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: stdin JSON from Claude Code's `UserPromptSubmit` hook event — has at least a
  `prompt` string field.
- Produces: on stdout, either nothing (skip case, exit 0) or a JSON object
  `{"hookSpecificOutput": {"hookEventName": "UserPromptSubmit", "additionalContext": "<string>"}}`.
  Reads `.claude/skills/prompt-loop/data/learnings.md` (may not exist yet) and
  `.claude/skills/prompt-loop/data/disabled` (may not exist yet) — later tasks' SKILL.md
  and `/prompt-loop-review` command write/manage `learnings.md`; this task only reads it.

- [ ] **Step 1: Create the hook script**

```bash
mkdir -p /Users/nguyenduc/Personal/Repositories/tradex-monitoring/.claude/skills/prompt-loop/hooks
```

Write `.claude/skills/prompt-loop/hooks/on-submit.sh`:

```bash
#!/usr/bin/env bash
# Prompt Improvement Loop - UserPromptSubmit hook
#
# Proposes an improved version of the user's prompt (metaprompt technique) and
# waits for confirmation before Claude acts on it. Fails open: any error here
# must never block the user's actual prompt from going through.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SKILL_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DATA_DIR="${SKILL_ROOT}/data"

INPUT_JSON=$(cat)
[ -z "$INPUT_JSON" ] && exit 0

[ -f "${DATA_DIR}/disabled" ] && exit 0

PYTHON_CMD=""
if command -v python3 >/dev/null 2>&1; then
  PYTHON_CMD=python3
elif command -v python >/dev/null 2>&1; then
  PYTHON_CMD=python
fi
[ -z "$PYTHON_CMD" ] && exit 0

PROMPT=$(echo "$INPUT_JSON" | "$PYTHON_CMD" -c '
import json, sys
try:
    data = json.load(sys.stdin)
    print(data.get("prompt", ""))
except Exception:
    print("")
' 2>/dev/null || echo "")

[ -z "$PROMPT" ] && exit 0

TRIMMED=$(echo "$PROMPT" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')

# Skip: slash command
case "$TRIMMED" in
  /*) exit 0 ;;
esac

# Skip: explicit bypass marker
case "$TRIMMED" in
  '!raw'*) exit 0 ;;
esac

# Skip: short confirmation replies (case-insensitive exact match)
LOWER=$(echo "$TRIMMED" | tr '[:upper:]' '[:lower:]')
case "$LOWER" in
  ok|oke|okay|yes|y|ừ|"ok đi"|"đồng ý"|"dong y"|"tiếp tục"|"tiep tuc"|continue|được|duoc|thanks|"cảm ơn"|"cam on")
    exit 0
    ;;
esac

# Skip: very short prompts (fewer than 4 words) — likely a quick reply, not a task
WORD_COUNT=$(echo "$TRIMMED" | wc -w | tr -d ' ')
if [ "${WORD_COUNT:-0}" -lt 4 ]; then
  exit 0
fi

LEARNINGS_FILE="${DATA_DIR}/learnings.md"
LEARNINGS_CONTENT="(chưa có)"
if [ -f "$LEARNINGS_FILE" ]; then
  LEARNINGS_CONTENT=$(cat "$LEARNINGS_FILE")
fi

export LEARNINGS_CONTENT
"$PYTHON_CMD" -c '
import json, os

learnings = os.environ.get("LEARNINGS_CONTENT", "(chưa có)")

instruction = """[prompt-loop] Trước khi thực hiện yêu cầu bên dưới của user, áp dụng workflow enhance-và-chờ-confirm mô tả trong .claude/skills/prompt-loop/SKILL.md:
1. Nếu turn trước của bạn là một đề xuất enhance đang chờ xác nhận, coi tin nhắn này là phản hồi cho đề xuất đó — KHÔNG enhance lại, xử lý bình thường.
2. Nếu đây là yêu cầu mới: viết bản cải thiện (rõ mục tiêu, bổ sung ngữ cảnh còn thiếu dựa trên CLAUDE.md/Knowledge, cấu trúc lại) rồi hiển thị: Bản gốc / Bản đề xuất / Lý do (ngắn gọn). Hỏi user xác nhận, sửa, hay giữ nguyên bản gốc. Dừng lại, chờ phản hồi trước khi bắt đầu task thật.
3. Sau khi user phản hồi: append 1 dòng JSON vào .claude/skills/prompt-loop/data/learnings.jsonl (timestamp, original, ai_draft, user_final, edit_type), rồi mới thực hiện task với bản đã chốt.
4. Nếu trong lúc làm task user yêu cầu sửa lại/làm lại, append thêm 1 outcome note vào cùng file, tham chiếu lại entry ở bước 3.
5. Đếm số entries mới kể từ lần chưng cất gần nhất; khi đạt mốc 15, gợi ý user chạy /prompt-loop-review."""

context = instruction + "\n\nQuy tắc đã học được (từ data/learnings.md):\n" + learnings

print(json.dumps({
    "hookSpecificOutput": {
        "hookEventName": "UserPromptSubmit",
        "additionalContext": context
    }
}))
'
exit 0
```

- [ ] **Step 2: Make it executable**

```bash
chmod +x /Users/nguyenduc/Personal/Repositories/tradex-monitoring/.claude/skills/prompt-loop/hooks/on-submit.sh
```

- [ ] **Step 3: Verify skip cases**

Run each and confirm **no output** (exit 0, silent):

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
echo '{"prompt": "/cost-report"}' | .claude/skills/prompt-loop/hooks/on-submit.sh
echo '{"prompt": "ok"}' | .claude/skills/prompt-loop/hooks/on-submit.sh
echo '{"prompt": "!raw just do it exactly as written"}' | .claude/skills/prompt-loop/hooks/on-submit.sh
echo '{"prompt": "cảm ơn"}' | .claude/skills/prompt-loop/hooks/on-submit.sh
echo '{"prompt": "sửa giúp title"}' | .claude/skills/prompt-loop/hooks/on-submit.sh
```

Expected: all five commands print nothing to stdout.

- [ ] **Step 4: Verify the pass-through (enhance) case**

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
echo '{"prompt": "viết PRD cho tính năng thông báo margin call cho tài khoản phái sinh"}' | .claude/skills/prompt-loop/hooks/on-submit.sh
```

Expected: prints one JSON line containing
`"hookEventName": "UserPromptSubmit"` and `"additionalContext"` whose value includes the
text `Quy tắc đã học được` and `(chưa có)` (since `data/learnings.md` does not exist yet).
Pipe through `python3 -m json.tool` to confirm it parses as valid JSON:

```bash
echo '{"prompt": "viết PRD cho tính năng thông báo margin call cho tài khoản phái sinh"}' | .claude/skills/prompt-loop/hooks/on-submit.sh | python3 -m json.tool
```

Expected: pretty-printed JSON, no parse error.

- [ ] **Step 5: Add gitignore entries**

Append to `.gitignore`:

```
# Prompt Improvement Loop — raw signal log (may contain verbatim prompts) and local disable toggle
.claude/skills/prompt-loop/data/learnings.jsonl
.claude/skills/prompt-loop/data/disabled
```

- [ ] **Step 6: Commit**

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
git add .claude/skills/prompt-loop/hooks/on-submit.sh .gitignore
git commit -m "feat: add prompt-loop UserPromptSubmit hook script"
```

---

### Task 2: SKILL.md — enhance-and-confirm workflow definition

**Files:**
- Create: `.claude/skills/prompt-loop/SKILL.md`

**Interfaces:**
- Consumes: nothing programmatic — this is the instruction document that Claude reads
  (per the `additionalContext` reference from Task 1's hook) to know the exact
  enhance-and-confirm procedure, log line format, and distillation-suggestion trigger.
- Produces: the log line schema `{timestamp, original, ai_draft, user_final, edit_type}`
  written to `.claude/skills/prompt-loop/data/learnings.jsonl` — this exact schema is
  relied on by Task 3's `/prompt-loop-review` command.

- [ ] **Step 1: Write the skill doc**

Create `.claude/skills/prompt-loop/SKILL.md`:

```markdown
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
```

- [ ] **Step 2: Commit**

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
git add .claude/skills/prompt-loop/SKILL.md
git commit -m "docs: add prompt-loop SKILL.md enhance-and-confirm workflow"
```

---

### Task 3: `/prompt-loop-review` slash command

**Files:**
- Create: `.claude/commands/prompt-loop-review.md`

**Interfaces:**
- Consumes: `.claude/skills/prompt-loop/data/learnings.jsonl` lines in the schema defined
  in Task 2, Step 3/4 (`{timestamp, original, ai_draft, user_final, edit_type}` and
  `{timestamp, event: "outcome", note, ref_timestamp}`).
- Produces: an updated `.claude/skills/prompt-loop/data/learnings.md` (not committed by
  this command — per the Global Constraints, it only presents the change and tells the
  user to commit).

- [ ] **Step 1: Write the command**

Create `.claude/commands/prompt-loop-review.md`:

```markdown
---
description: Distill accumulated prompt-loop signals into updated preference rules (learnings.md). Does not auto-commit.
---

# Prompt Loop Review

1. Read `.claude/skills/prompt-loop/data/learnings.jsonl`. If it doesn't exist or is
   empty, tell the user there's nothing to review yet and stop here.
2. Read the current `.claude/skills/prompt-loop/data/learnings.md` if it exists
   (otherwise treat it as empty).
3. Go through every line of `learnings.jsonl`:
   - For `edit_type: "edited"` entries, compare `ai_draft` vs `user_final` and look for
     a recurring pattern across multiple entries (e.g. consistently shortened, always
     adds a Jira/ticket reference, always switches to Vietnamese, always adds acceptance
     criteria). A pattern needs at least 2 supporting entries to become a rule — a single
     occurrence is not enough signal.
   - For `edit_type: "kept_original"` entries, treat these as a signal that a particular
     kind of enhancement was unwanted — look for what the proposals in those entries had
     in common.
   - For `event: "outcome"` entries, treat them as evidence the corresponding enhancement
     missed something — factor that into the rule for the referenced pattern.
4. Merge findings into `data/learnings.md` as a flat bullet list under a
   `## Quy tắc đã học` heading, one line per rule, each phrased as an actionable
   instruction (e.g. "- Ưu tiên bản ngắn gọn, bỏ phần mở đầu trang trọng."). When a new
   finding supersedes or contradicts an existing bullet, replace the old one — don't
   keep both. Do not include verbatim customer data, secrets, or account numbers in any
   rule — abstract the pattern only.
5. Write the merged content to `data/learnings.md` (via the Write tool, full overwrite
   with the merged result — not an unbounded append).
6. Show the user a summary: how many entries were reviewed, what rules were
   added/changed/removed.
7. Per this repo's "No auto-commit/push" rule: do NOT run `git commit`. Tell the user
   the file is ready for review and suggest the exact command to run themselves:
   `git add .claude/skills/prompt-loop/data/learnings.md && git commit -m "chore: update prompt-loop learned preferences"`
```

- [ ] **Step 2: Verify with seeded sample data**

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
mkdir -p .claude/skills/prompt-loop/data
cat > .claude/skills/prompt-loop/data/learnings.jsonl <<'EOF'
{"timestamp": "2026-07-01T09:00:00Z", "original": "viet issue cho tinh nang X", "ai_draft": "Viết FE Issue chi tiết cho tính năng X, bao gồm bối cảnh, acceptance criteria, và tham chiếu API liên quan theo chuẩn ISSUE_STANDARD.md, trình bày trang trọng đầy đủ mục lục.", "user_final": "Viết issue ngắn gọn cho tính năng X, chỉ cần acceptance criteria và API liên quan.", "edit_type": "edited"}
{"timestamp": "2026-07-05T09:00:00Z", "original": "sua PRD margin call", "ai_draft": "Cập nhật toàn diện PRD tính năng margin call, bổ sung mục lục đầy đủ, business context chi tiết, phân tích rủi ro, và các phần phụ lục liên quan.", "user_final": "Sửa ngắn phần margin call trong PRD, không cần thêm mục lục hay phụ lục.", "edit_type": "edited"}
EOF
```

Run `/prompt-loop-review` (as an actual Claude Code slash command in a session in this
repo). Expected: it reports 2 entries reviewed, creates
`.claude/skills/prompt-loop/data/learnings.md` with a `## Quy tắc đã học` section
containing a rule about preferring concise output over long/formal structure (the
pattern shared by both seeded entries), and explicitly tells you to run the `git add`/
`git commit` command yourself rather than committing on your own.

- [ ] **Step 3: Reset the seeded test data**

The seeded sample above is only for verifying the command works — it is not real
learned preference data and must not be committed.

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
rm -f .claude/skills/prompt-loop/data/learnings.jsonl
rm -f .claude/skills/prompt-loop/data/learnings.md
```

- [ ] **Step 4: Commit the command file only**

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
git add .claude/commands/prompt-loop-review.md
git commit -m "feat: add /prompt-loop-review distillation command"
```

---

### Task 4: Register the hook and verify end-to-end

**Files:**
- Modify: `.claude/settings.json`

**Interfaces:**
- Consumes: `.claude/skills/prompt-loop/hooks/on-submit.sh` from Task 1 (exact relative
  path referenced below).
- Produces: nothing further downstream — this is the final wiring task.

- [ ] **Step 1: Add the `UserPromptSubmit` hook entry**

Current `.claude/settings.json` `hooks` block has `Stop`, `PreToolUse`, `PostToolUse`
keys. Add a `UserPromptSubmit` key alongside them (do not remove or reorder the
existing three):

```json
{
  "env": {
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1",
    "ECC_GATEGUARD": "off"
  },
  "hooks": {
    "UserPromptSubmit": [
      {
        "matcher": "",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/skills/prompt-loop/hooks/on-submit.sh"
          }
        ]
      }
    ],
    "Stop": [
      {
        "matcher": "",
        "hooks": [
          {
            "type": "command",
            "command": "./scripts/claude-autosave.sh"
          }
        ]
      }
    ],
    "PreToolUse": [
      {
        "matcher": "*",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/skills/ecc/continuous-learning-v2/hooks/observe.sh pre"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "*",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/skills/ecc/continuous-learning-v2/hooks/observe.sh post"
          }
        ]
      }
    ]
  },
  "enabledPlugins": {
    "harness@harness-marketplace": true
  }
}
```

- [ ] **Step 2: Validate the JSON**

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
python3 -m json.tool .claude/settings.json > /dev/null && echo "settings.json OK"
```

Expected: prints `settings.json OK` with no error.

- [ ] **Step 3: End-to-end manual verification**

Start a new Claude Code session in this repo (or `/exit` and restart the current one so
the updated `settings.json` is loaded), then:

1. Send a real task prompt, e.g. `viết PRD ngắn cho một tính năng test bất kỳ`.
   Expected: Claude responds with the "Bản gốc / Đề xuất cải thiện / Lý do" format from
   `SKILL.md` and asks you to confirm/sửa/giữ nguyên, without having started the actual
   PRD yet.
2. Reply `giữ nguyên bản gốc` (or confirm/edit).
   Expected: Claude does NOT show another enhancement proposal for this reply: it
   proceeds to append a line to `.claude/skills/prompt-loop/data/learnings.jsonl` and
   then does the actual task.
3. Check the log:

```bash
cat /Users/nguyenduc/Personal/Repositories/tradex-monitoring/.claude/skills/prompt-loop/data/learnings.jsonl
```

Expected: one JSON line matching the Task 2 Step 3 schema.

4. Check git status:

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
git status --short
```

Expected: `.claude/skills/prompt-loop/data/learnings.jsonl` does NOT appear (gitignored).

- [ ] **Step 4: Commit**

```bash
cd /Users/nguyenduc/Personal/Repositories/tradex-monitoring
git add .claude/settings.json
git commit -m "feat: wire up prompt-loop UserPromptSubmit hook in settings.json"
```
