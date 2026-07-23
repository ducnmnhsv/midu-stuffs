```markdown
# midu-stuffs Development Patterns

> Auto-generated skill from repository analysis

## Overview

This skill teaches you how to contribute effectively to the `midu-stuffs` JavaScript codebase. You'll learn the project's coding conventions, commit message patterns, and the main workflows for adding features, fixing bugs, updating helpers, and improving localization—especially in the Project Dashboard component. The repository uses conventional commits, relative imports, PascalCase file naming, and named exports, with a focus on maintainable, testable code.

## Coding Conventions

- **File Naming:**  
  Use PascalCase for component and script files.  
  _Example:_  
  ```
  ProjectDashboard.jsx
  Helpers.test.js
  ```

- **Import Style:**  
  Use relative imports for modules within the project.  
  _Example:_  
  ```js
  import { calculateProgress } from './helpers';
  ```

- **Export Style:**  
  Use named exports for functions and components.  
  _Example:_  
  ```js
  export function calculateProgress(tasks) { ... }
  ```

- **Commit Messages:**  
  Follow [Conventional Commits](https://www.conventionalcommits.org/) with these prefixes:  
    - `feat:` for new features or enhancements  
    - `fix:` for bug fixes  
    - `chore:` for maintenance  
    - `test:` for test-related changes  
    - `refactor:` for code restructuring  
  _Example:_  
  ```
  feat: add Vietnamese language toggle to dashboard
  fix: correct progress calculation for completed tasks
  ```

## Workflows

### Add or Enhance Dashboard Feature
**Trigger:** When you want to add a new capability or major UI feature to the project dashboard.  
**Command:** `/add-dashboard-feature`

1. Edit or extend `Project management/ProjectDashboard.jsx` to implement the new feature.
2. Optionally update or add related helpers or constants within the same file.
3. Commit your changes with a `feat:` message describing the new feature.

_Example:_
```js
// ProjectDashboard.jsx
export function ProjectDashboard() {
  // ...existing code
  // Add new UI component or field
}
```
```
git commit -m "feat: add project deadline field to dashboard"
```

---

### Add or Update Helper Functions with Tests
**Trigger:** When you want to introduce or change helper utilities for dashboard logic.  
**Command:** `/add-helper-with-tests`

1. Edit `Project management/ProjectDashboard.jsx` to add or update helper functions.
2. Update or add tests in `Project management/scripts/helpers.test.js`.
3. Optionally update helper extraction/loading scripts.
4. Commit with a `feat:` or `fix:` message referencing helpers and tests.

_Example:_
```js
// ProjectDashboard.jsx
export function calculateCompletionRate(tasks) { ... }

// helpers.test.js
import { calculateCompletionRate } from '../ProjectDashboard';

test('calculates completion rate correctly', () => {
  // ...test implementation
});
```
```
git commit -m "feat: add calculateCompletionRate helper and tests"
```

---

### Fix Dashboard Bug or Address Review Feedback
**Trigger:** When you want to resolve a bug or implement changes from code review.  
**Command:** `/fix-dashboard-bug`

1. Edit `Project management/ProjectDashboard.jsx` to fix the bug or apply review suggestions.
2. Optionally update related test files if the fix affects logic.
3. Commit with a `fix:` message referencing the issue or review finding.

_Example:_
```js
// ProjectDashboard.jsx
// Fix logic error in progress calculation
```
```
git commit -m "fix: correct progress calculation when no tasks"
```

---

### Add or Update Localization or Language Support
**Trigger:** When you want to add new language coverage or improve localization (e.g., VI/EN support).  
**Command:** `/add-localization`

1. Edit `Project management/ProjectDashboard.jsx` to add or update language toggles, dictionaries, or localized text.
2. Optionally update or add tests for language features in `helpers.test.js`.
3. Commit with a `feat:` or `fix:` message referencing language or localization.

_Example:_
```js
// ProjectDashboard.jsx
const labels = {
  en: { title: 'Project Dashboard' },
  vi: { title: 'Bảng Dự Án' }
};
```
```
git commit -m "feat: add Vietnamese translations for dashboard labels"
```

## Testing Patterns

- **Test File Naming:**  
  Test files follow the `*.test.*` pattern and are typically located in `scripts/` or alongside the code being tested.

- **Test Structure:**  
  Tests are written in JavaScript, using an unknown framework (likely Jest or similar).  
  _Example:_
  ```js
  // helpers.test.js
  import { someHelper } from '../ProjectDashboard';

  test('someHelper returns expected value', () => {
    expect(someHelper(...)).toBe(...);
  });
  ```

## Commands

| Command                | Purpose                                                      |
|------------------------|--------------------------------------------------------------|
| /add-dashboard-feature | Add or enhance a feature in the Project Dashboard            |
| /add-helper-with-tests | Add or update helper functions and their tests               |
| /fix-dashboard-bug     | Fix a bug or address code review feedback in the dashboard   |
| /add-localization      | Add or update localization/language support in the dashboard |
```
