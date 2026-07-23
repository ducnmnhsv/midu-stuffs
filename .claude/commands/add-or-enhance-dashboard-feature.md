---
name: add-or-enhance-dashboard-feature
description: Workflow command scaffold for add-or-enhance-dashboard-feature in midu-stuffs.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /add-or-enhance-dashboard-feature

Use this workflow when working on **add-or-enhance-dashboard-feature** in `midu-stuffs`.

## Goal

Implements a new feature or enhancement in the ProjectDashboard, such as new fields, UI components, or major interactions.

## Common Files

- `Project management/ProjectDashboard.jsx`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Edit or extend Project management/ProjectDashboard.jsx to implement the feature.
- Optionally update or add related helpers or constants within the same file.
- Commit with a 'feat:' message describing the new feature.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.