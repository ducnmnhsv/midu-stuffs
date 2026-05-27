#!/bin/bash
# Restore Claude Code sessions từ repo → local

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_PROJECT_DIR="$HOME/.claude/projects/$(echo "$REPO_DIR" | sed 's|/|-|g')"
SRC="$REPO_DIR/.claude-sessions/sessions"

if [ ! -d "$SRC" ]; then
  echo "❌ Không có sessions trong repo. Chạy claude-save.sh trên máy kia trước."
  exit 1
fi

echo "📂 Source: $SRC"
echo "📦 Dest:   $CLAUDE_PROJECT_DIR"

# Cảnh báo nếu đã có sessions local
if [ -d "$CLAUDE_PROJECT_DIR" ] && [ "$(ls -A "$CLAUDE_PROJECT_DIR" 2>/dev/null)" ]; then
  echo ""
  echo "⚠️  Máy này đã có Claude sessions. Restore sẽ ghi đè."
  read -p "Tiếp tục? (y/N): " confirm
  [[ "$confirm" != "y" && "$confirm" != "Y" ]] && echo "Hủy." && exit 0
fi

mkdir -p "$CLAUDE_PROJECT_DIR"
rsync -av --delete "$SRC/" "$CLAUDE_PROJECT_DIR/"

echo ""
echo "✅ Restored. Mở lại Claude Code để load sessions."
