#!/bin/bash
# Merge Claude sessions từ repo → local (không xóa sessions local)

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_PROJECT_DIR="$HOME/.claude/projects/$(echo "$REPO_DIR" | sed 's|/|-|g')"
SRC="$REPO_DIR/.claude-sessions/sessions"

if [ ! -d "$SRC" ]; then
  echo "❌ Không có sessions trong repo."
  exit 1
fi

mkdir -p "$CLAUDE_PROJECT_DIR"

# Merge: copy từ repo vào local, không xóa sessions local hiện có
rsync -av "$SRC/" "$CLAUDE_PROJECT_DIR/"

echo ""
echo "✅ Sessions merged. Restart Claude Code để load."
