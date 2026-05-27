#!/bin/bash
# Auto-save Claude sessions + commit + push (chạy tự động khi session kết thúc)
# Chỉ sync sessions có nội dung thực, dùng merge (không xóa sessions từ máy khác)

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_PROJECT_DIR="$HOME/.claude/projects/$(echo "$REPO_DIR" | sed 's|/|-|g')"
DEST="$REPO_DIR/.claude-sessions/sessions"

[ ! -d "$CLAUDE_PROJECT_DIR" ] && exit 0

mkdir -p "$DEST"

# Chỉ copy sessions có real messages (lọc sessions rỗng)
for f in "$CLAUDE_PROJECT_DIR"/*.jsonl; do
  [ -f "$f" ] || continue
  has_content=$(python3 -c "
import sys, json
for line in open('$f'):
    try:
        obj = json.loads(line)
        if obj.get('type') == 'user' and not obj.get('isMeta'):
            c = obj.get('message',{}).get('content','')
            text = c if isinstance(c, str) else next((x.get('text','') for x in c if isinstance(x,dict) and x.get('type')=='text'), '')
            if len(text) > 10 and 'command-name' not in text and 'local-command' not in text:
                print('yes'); exit()
    except: pass
print('no')
" 2>/dev/null)
  [ "$has_content" = "yes" ] && cp "$f" "$DEST/"
done

# Merge: copy memory folder (không --delete để giữ sessions từ máy khác)
[ -d "$CLAUDE_PROJECT_DIR/memory" ] && rsync -a "$CLAUDE_PROJECT_DIR/memory/" "$DEST/memory/" 2>/dev/null

# Commit nếu có thay đổi
cd "$REPO_DIR" || exit 0
git add .claude-sessions/ 2>/dev/null
git diff --staged --quiet && exit 0

git commit -m "chore: auto-sync claude sessions [skip ci]" --no-gpg-sign -q 2>/dev/null
git push --quiet 2>/dev/null || true
