#!/bin/bash
# Chạy 1 lần trên mỗi máy sau khi clone repo

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# Dùng .githooks/ folder thay vì .git/hooks/
git -C "$REPO_DIR" config core.hooksPath .githooks
chmod +x "$REPO_DIR/.githooks/"*

echo "✅ Git hooks đã được cài đặt từ .githooks/"
echo "   - post-merge: auto-restore Claude sessions sau git pull"
