---
name: add-or-update-helper-functions-with-tests
description: Workflow command scaffold for add-or-update-helper-functions-with-tests in midu-stuffs.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /add-or-update-helper-functions-with-tests

Use this workflow when working on **add-or-update-helper-functions-with-tests** in `midu-stuffs`.

## Goal

Adds or modifies pure helper functions (date math, calculations, etc.) and ensures they are covered by tests.

## Common Files

- `Project management/ProjectDashboard.jsx`
- `Project management/scripts/helpers.test.js`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Edit Project management/ProjectDashboard.jsx to add or update helper functions.
- Update or add tests in Project management/scripts/helpers.test.js.
- Optionally update helper extraction/loading scripts.
- Commit with a 'feat:' or 'fix:' message referencing helpers and tests.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.