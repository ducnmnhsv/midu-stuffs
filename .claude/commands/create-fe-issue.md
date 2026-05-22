# Create FE Issue (Derivatives)

Tạo issue cho Frontend Derivatives dựa trên codebase nhsv-mts-rn (read-only).

## FE Repo

| Item | Value |
|------|-------|
| Path | `/Users/ducnguyen/Documents/project/nhsv-mts-rn` |
| Role | TradeX / NHSV Pro React Native app |
| Usage | **READ-ONLY** reference — KHÔNG sửa code |

## Workflow

**Step 1 — Read FE repo (nhsv-mts-rn)**
- Tìm màn hình/component liên quan Derivatives
- Xác định path file cần sửa hoặc thêm
- Đọc API client, types, navigation liên quan

**Step 2 — Write issue (trong tradex-monitoring)**
- Title: `[FE] {Feature/Fix description}`
- Description theo template bên dưới
- Save tại: `Derivatives/Planning documentation/{Category}/Issues/`

**Step 3 — KHÔNG chỉnh sửa nhsv-mts-rn**

## FE Repo Structure Reference

| Path | Content |
|------|---------|
| `src/screens/` | Màn hình (có thể có derivatives/, order/, market/) |
| `src/components/` | Components dùng chung hoặc theo feature |
| `src/reduxs/` | Redux slices, API calls |
| `src/hooks/` | Custom hooks |
| `src/interfaces/` | Types, API request/response |
| `src/utils/` | Helpers, format, config |

## Issue Template

```markdown
# [FE] {Feature Name} — Derivatives

## 📋 Executive Summary (PM READS THIS)

### Problem
{Business problem / user-facing issue}

### Impact
{Who is affected, how}

### Solution (HIGH-LEVEL)
{What needs to change in FE — no code}

---

## 🔍 Technical Details

### Affected Screens/Components
- `src/screens/{path}` — {what to change}
- `src/components/{path}` — {what to change}

### API Changes Required
| Endpoint | Change |
|----------|--------|
| `GET /api/v1/...` | Add field X to response |

### Acceptance Criteria
- [ ] {Screen} displays {data}
- [ ] {Action} triggers {result}
- [ ] No regression on {existing feature}

### Notes for Developer
{Context, edge cases, reference to BE spec}

---

**Document Status:** 📋 Draft
**For:** FE Developer
**Next Steps:** Review with FE team
```
