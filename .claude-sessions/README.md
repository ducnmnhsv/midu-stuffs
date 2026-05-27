# Claude Session Sync

Folder này chứa Claude Code session history để sync giữa các máy qua GitHub.

## Cách dùng

```bash
# Lưu sessions từ máy hiện tại lên repo
./scripts/claude-save.sh

# Khôi phục sessions từ repo về máy hiện tại
./scripts/claude-restore.sh
```

## Workflow khi chuyển máy

**Trên máy cũ (trước khi rời):**
```bash
./scripts/claude-save.sh
git add .claude-sessions/
git commit -m "chore: sync claude sessions"
git push
```

**Trên máy mới (sau khi pull):**
```bash
git pull
./scripts/claude-restore.sh
```
