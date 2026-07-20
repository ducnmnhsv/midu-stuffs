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
' || exit 0
exit 0
