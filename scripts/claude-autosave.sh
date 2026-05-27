#!/bin/bash
# Auto-save Claude sessions + commit + push (chạy tự động khi session kết thúc)

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_PROJECT_DIR="$HOME/.claude/projects/$(echo "$REPO_DIR" | sed 's|/|-|g')"
DEST="$REPO_DIR/.claude-sessions/sessions"

[ ! -d "$CLAUDE_PROJECT_DIR" ] && exit 0

# Copy sessions vào repo
rsync -a --delete --exclude=".gitkeep" "$CLAUDE_PROJECT_DIR/" "$DEST/" 2>/dev/null

# Commit nếu có thay đổi
cd "$REPO_DIR" || exit 0
git add .claude-sessions/ 2>/dev/null
git diff --staged --quiet && exit 0

git commit -m "chore: auto-sync claude sessions [skip ci]" --no-gpg-sign -q 2>/dev/null
git push --quiet 2>/dev/null || true
