# TradeX Monitoring - Agent Quick Reference

## Current Agents (3)

| Agent | Activation | Purpose |
|-------|-----------|---------|
| **TradeX Analyst** | `@tradex-analyst` | API analysis, securities domain, system tracing |
| **Agile Developer** | `@agile-developer` | Agile workflows, BMAD framework, story development |
| **TradeX QA Postman** | `@tradex-qa-postman` | Test TradeX API via Postman MCP; requests in "TradeX QA session", docs in `QA sessions/` |

---

## Proposed New Agents (3)

| Agent | Activation | Primary Responsibility |
|-------|-----------|----------------------|
| **Orchestrator** | `@orchestrator` | Coordinate all agents, route tasks, manage workflows |
| **Documentation** | `@documentation-agent` | Technical docs, runbooks, guides |
| **Test** | `@test-agent` | Testing, QA automation, validation |

---

## Agent Interaction Examples

### Example 1: Create Documentation
```
User: "Create runbook for Kafka monitoring"
→ Orchestrator routes to:
  1. TradeX Analyst: Analyze Kafka monitoring requirements
  2. Documentation Agent: Create runbook
  3. Test Agent: Validate examples in runbook
```

### Example 2: Test New Feature
```
User: "Test new monitoring dashboard"
→ Orchestrator routes to:
  1. Test Agent: Create test cases
  2. Test Agent: Execute tests
  3. Documentation Agent: Document test results
```

### Example 3: Complete Feature Development
```
User: "Implement and document new monitoring feature"
→ Orchestrator routes in parallel:
  1. TradeX Analyst: Analyze requirements
  2. Agile Developer: Create stories
  3. Documentation Agent: Create initial docs
  4. Test Agent: Create test plan
```

---

## Implementation Priority

### Phase 1 (Week 1) - Critical
- ✅ Orchestrator Agent

### Phase 2 (Week 2) - High Priority
- ✅ Documentation Agent

### Phase 3 (Week 3) - High Priority
- ✅ Test Agent

---

## Full Documentation

Xem chi tiết tại: `.cursor/rules/agent-architecture-proposal.md`

---

**Last Updated**: January 26, 2026
