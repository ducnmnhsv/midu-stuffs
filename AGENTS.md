# AGENTS.md

**Development Guide for TradeX Monitoring - BMAD Framework v6.0.0-alpha.23**

---

## Build/Lint/Test Commands

### BMAD Framework Commands
This project uses the BMAD (Business Management and Development) framework with YAML-based workflows rather than traditional build systems.

**Core Workflow Execution:**
```bash
# BMAD workflows are executed via YAML configuration files
# Main config: BMAD/_bmad/bmm/config.yaml
# Core config: BMAD/_bmad/core/config.yaml
```

**Development Workflow Commands:**
```bash
# Story implementation
bmad-bmm-workflow-dev-story

# Code review
bmad-bmm-workflow-code-review

# Documentation
bmad-bmm-workflow-document-project

# Testing workflows
bmad-bmm-workflow-testarch-test-design
bmad-bmm-workflow-testarch-test-review
bmad-bmm-workflow-testarch-ci
bmad-bmm-workflow-testarch-automate
```

**Running Individual Tests:**
Test execution follows the test levels framework (unit/integration/E2E) with these patterns:

```yaml
# Unit tests: Fast, isolated logic testing
test_id_format: "{EPIC}.{STORY}-UNIT-{SEQ}"
naming_convention: "test_{component}_{scenario}"

# Integration tests: Service/database/API validation
test_id_format: "{EPIC}.{STORY}-INT-{SEQ}"
naming_convention: "test_{flow}_{interaction}"

# E2E tests: Complete user journeys
test_id_format: "{EPIC}.{STORY}-E2E-{SEQ}"
naming_convention: "test_{journey}_{outcome}"
```

**Test Execution Examples:**
```bash
# Unit test example - pure business logic
npm test -- --testPathPattern=price-calculator.test.ts

# Integration test example - API service layer
npm test -- --testPathPattern=user-service.spec.ts

# E2E test example - full user journey
npx playwright test tests/e2e/checkout-flow.spec.ts

# Component test example - isolated UI component
npx playwright test --config=playwright-ct.config.ts src/components/Button.cy.tsx
```

---

## Code Style Guidelines

### Project Structure & Conventions

**Directory Organization:**
- `BMAD/` - Main framework directory with agent/workflow definitions
- `NHMTS-642/` - TradeX stability monitor project documentation
- `_bmad-output/` - Generated planning and implementation artifacts
- `docs/` - Project knowledge and documentation

**Naming Conventions:**
- Directories: kebab-case (e.g., `tradex-monitoring`)
- Files: descriptive names with hyphens (e.g., `documentation-standards.md`)
- Components: PascalCase (e.g., `PriceCalculator`)
- Functions: camelCase (e.g., `calculateDiscount`)

### Documentation Standards (Critical)

**CommonMark Strict Compliance:**
- ATX-style headers ONLY: `# Title` (not Setext underlines)
- Single space after `#`: `# Title` (not `#Title`)
- No trailing `#`: `# Title` (not `# Title #`)
- Hierarchical order: Don't skip header levels

**Code Blocks:**
```markdown
```javascript
const example = 'code';
```
```
- Always use fenced blocks with language identifiers
- Never use indented code blocks

**Lists:**
- Consistent markers: all `-` or all `*` (don't mix)
- Proper indentation for nested items (2 or 4 spaces, stay consistent)
- Blank line before/after lists

**Critical Rule: NO TIME ESTIMATES**
Never document time estimates, durations, or completion times for any workflow, task, or activity.

### Import & Code Organization

**Import Patterns:**
```typescript
// 1. Node.js built-ins
import { promises as fs } from 'fs';
import path from 'path';

// 2. External dependencies
import express from 'express';
import { test, expect } from '@playwright/test';

// 3. Internal modules (relative)
import { calculateDiscount } from './utils/price-calculator';
import { UserService } from '../services/user.service';
```

**File Structure:**
- Export main functionality first
- Helper functions after main exports
- Types/interfaces at bottom or in separate files
- Tests co-located or in dedicated test directory

### Error Handling Patterns

**Business Logic (Unit Tests):**
```typescript
// Pure functions - no side effects
export function calculateDiscount(price: number, discount: Discount): number {
  if (price <= 0) return 0;
  
  switch (discount.type) {
    case 'percentage':
      return Math.max(0, price * (1 - discount.value / 100));
    case 'fixed':
      return Math.max(0, price - discount.value);
    default:
      return price;
  }
}
```

**Service Layer (Integration Tests):**
```typescript
// API service with error handling
export class UserService {
  async createUser(userData: CreateUserRequest): Promise<User> {
    try {
      const response = await this.api.post('/users', { data: userData });
      if (response.status() !== 201) {
        throw new Error(`Failed to create user: ${response.status()}`);
      }
      return response.json();
    } catch (error) {
      throw new Error(`User creation failed: ${error.message}`);
    }
  }
}
```

**E2E Tests:**
```typescript
// Network-first approach to prevent flakiness
test('user can complete purchase', async ({ page }) => {
  const orderPromise = page.waitForResponse('**/api/orders');
  
  await page.goto('/checkout');
  await page.click('[data-testid="place-order"]');
  
  await orderPromise; // Wait for network response
  await expect(page.getByText('Order Confirmed')).toBeVisible();
});
```

### Testing Guidelines

**Test Level Selection:**
- **Unit Tests**: Pure functions, business logic, algorithms
- **Integration Tests**: API contracts, database operations, service interactions
- **E2E Tests**: Critical user journeys, cross-system workflows
- **Component Tests**: UI components in isolation (props, events, states)

**Anti-Patterns to Avoid:**
- E2E testing for business logic validation
- Unit testing framework behavior
- Integration testing third-party libraries
- Duplicate coverage across levels

**Best Practices:**
- Favor unit tests for complex business logic
- Use integration tests for persistence and service contracts
- Reserve E2E tests for revenue-critical user journeys
- Apply network-first testing for UI reliability

### BMAD-Specific Conventions

**Agent Activation Protocol:**
1. LOAD the full agent file from `@_bmad/bmm/agents/`
2. READ entire contents (persona, menu, instructions)
3. Execute ALL activation steps exactly as written
4. Stay in character throughout session
5. Follow agent persona and menu system precisely

**Workflow Execution:**
1. LOAD `@_bmad/core/tasks/workflow.xml`
2. READ entire contents (core OS for workflow execution)
3. Pass YAML workflow config as parameter
4. Follow workflow.xml instructions EXACTLY
5. Save outputs after EACH section

**Output Structure:**
- Planning artifacts: `/_bmad-output/planning-artifacts`
- Implementation artifacts: `/_bmad-output/implementation-artifacts`
- Project knowledge: `/docs`

### Development Workflow

**Story-Driven Development:**
1. Load and analyze story requirements
2. Design tests first (red-green-refactor)
3. Implement tasks/subtasks
4. Validate against acceptance criteria
5. Update story file with implementation details

**Documentation Requirements:**
- All outputs documented in Dev Agent Record
- Continuous documentation throughout development
- Strict CommonMark compliance
- No time estimates in any documentation

**Quality Gates:**
- Code review workflow completion
- Test coverage at appropriate levels
- Documentation standards compliance
- Acceptance criteria validation

---

## Technology Stack

**Core Framework:** BMAD v6.0.0-alpha.23
**Monitoring Platform:** Grafana-based
**Infrastructure:** Kafka, Redis, Database, Application Servers
**Testing:** Playwright (E2E/Component), Jest/Vitest (Unit), API testing
**Documentation:** CommonMark with Mermaid diagram support
**Configuration:** YAML-based workflow definitions

**Key Principles:**
- Agent-driven development with strict persona adherence
- Test-first approach with appropriate level selection
- Comprehensive documentation with CommonMark compliance
- Automated workflows through BMAD framework
- Continuous validation and quality assurance