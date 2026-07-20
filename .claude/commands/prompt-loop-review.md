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
