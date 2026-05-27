#!/bin/bash
# Sync Claude Code sessions từ local → repo

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_PROJECT_DIR="$HOME/.claude/projects/$(echo "$REPO_DIR" | sed 's|/|-|g')"
DEST="$REPO_DIR/.claude-sessions"

if [ ! -d "$CLAUDE_PROJECT_DIR" ]; then
  echo "❌ Không tìm thấy Claude session dir: $CLAUDE_PROJECT_DIR"
  exit 1
fi

echo "📂 Source: $CLAUDE_PROJECT_DIR"
echo "📦 Dest:   $DEST"

# Copy tất cả session files và memory
rsync -av --delete \
  --exclude=".gitkeep" \
  "$CLAUDE_PROJECT_DIR/" "$DEST/sessions/"

echo ""
echo "✅ Saved. Giờ commit và push:"
echo "   git add .claude-sessions/"
echo "   git commit -m \"chore: sync claude sessions\""
echo "   git push"
