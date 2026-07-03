# Business Requirements Document (BRD)

**Document Type:** Business Requirements Document  
**Module:** Derivatives Market Data  
**Audience:** PM, BA, Stakeholders  
**Last Updated:** February 3, 2026

---

## Executive Summary

### Business Goal

Enable derivatives (phái sinh) market data display in NHSV Pro App to support futures trading features.

### Target Users

- Existing users who want to trade derivatives
- New users specifically for derivatives trading
- Institutional clients requiring derivatives data

### Expected Benefits

1. **Revenue:** Enable derivatives trading commission
2. **User Retention:** Keep users who need derivatives on platform
3. **Competitive Advantage:** Match competitors' offerings
4. **Market Share:** Capture derivatives trading segment

---

## Business Requirements

### BR-1: Real-time Derivatives Quotes

**As a** trader  
**I want to** see real-time price updates for derivatives symbols  
**So that** I can make informed trading decisions

**Acceptance Criteria:**
- Real-time quotes for VN30F futures
- Update frequency: < 1 second
- Same UI/UX as equity quotes

---

### BR-2: Derivatives Chart Data

**As a** trader  
**I want to** view price charts for derivatives  
**So that** I can analyze price trends and patterns

**Acceptance Criteria:**
- Support D, W, M timeframes
- Support 1, 5, 15, 30, 60 min intraday
- Same charting library as equity

---

### BR-3: Symbol Information

**As a** trader  
**I want to** see derivatives symbol details (expiry, base code, category, etc.)  
**So that** I can understand what I'm trading and the app can show the correct index name

**Acceptance Criteria:**
- Display expiry date
- Display base index (e.g. VN30) or underlying for bond futures
- Display contract specifications
- **Field `m` (market):** Với phái sinh dùng **cùng field `m`** như cơ sở: `m` = **"INDEX"** hoặc **"BOND"** (suy từ 41I/41B) để app hiển thị đúng tên chỉ mục (INDEX → PS/DR, BOND → TPCP/GB). Init job và API SymbolInfo/symbol_static/symbol/latest đều trả `m` = INDEX | BOND cho mã phái sinh.

---

### BR-4: Market Status

**As a** trader  
**I want to** see derivatives market status (open, closed, ATO, ATC)  
**So that** I know when I can trade

**Acceptance Criteria:**
- Real-time session status
- Match official exchange hours
- Clear visual indicators

---

## Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| User Adoption | 10% of active users view derivatives | Analytics |
| Data Accuracy | 99.9% match with exchange | Comparison test |
| System Uptime | 99.5% during trading hours | Monitoring |
| Response Time | < 500ms for API calls | Performance test |

---

## Scope

### In Scope
- VN30F futures display
- Real-time quotes
- Historical charts
- Symbol information

### Out of Scope
- Derivatives trading (separate project)
- Derivatives account management
- P&L calculation
- Portfolio tracking

---

## Constraints

1. **Timeline:** Must complete before Q2 2026
2. **Budget:** Use existing infrastructure
3. **Resources:** No additional servers
4. **Compliance:** Follow VSD/HNX regulations

---

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Data accuracy issues | High | Automated testing, monitoring |
| Performance degradation | Medium | Isolation, caching strategy |
| User confusion | Low | Clear UI labels, help docs |

---

## Stakeholder Sign-off

| Role | Name | Approved | Date |
|------|------|----------|------|
| Product Manager | | | |
| Business Analyst | | | |
| Technical Lead | | | |

---

## See Also

- **Technical Requirements:** [03_Technical_Requirements.md](./03_Technical_Requirements.md)
- **Integration Plan:** [01_Integration_Plan.md](./01_Integration_Plan.md)

---

Document Status: 📋 | For: PM/Dev | Next Steps: Review nội dung, cập nhật status trên Tracking/tasks.js
