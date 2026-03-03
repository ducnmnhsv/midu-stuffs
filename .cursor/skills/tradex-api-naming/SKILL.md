---
name: tradex-api-naming
description: Enforce TradeX API naming conventions for URLs, endpoints, request/response DTOs, and service methods. Use when creating new APIs, reviewing API designs, or when the user mentions API naming, endpoint naming, DTO naming, or request/response patterns.
---

# TradeX API Naming Conventions

## Quick Reference

| Element | Pattern | Example |
|---------|---------|---------|
| URL Base | `/api/v1/{resource}` | `/api/v1/stopOrder` |
| URL Action | `/api/v1/{resource}/{action}` | `/api/v1/stopOrder/modify` |
| Request DTO | `{Resource}{Action}Request` | `StopOrderPlaceRequest` |
| Response DTO | `{Resource}{Action}Response` | `StopOrderPlaceResponse` |
| Service Method | `{action}{Resource}` | `placeStopOrder` |

---

## URL Endpoint Conventions

### Base Structure

```
{method}:/api/v1/{resource}[/{action}]
```

- **Prefix**: Always use `/api/v1/`
- **Resource names**: camelCase (e.g., `stopOrder`, `trailingOrder`, `ocoOrder`)
- **Methods**: `get:`, `post:`, `put:`, `delete:`, `patch:` (defaults to GET if omitted)

### URL Patterns by Operation

| Operation | HTTP Method | URL Pattern | Example |
|-----------|-------------|-------------|---------|
| Create | POST | `/api/v1/{resource}` | `POST /api/v1/stopOrder` |
| Update | PUT | `/api/v1/{resource}/modify` | `PUT /api/v1/stopOrder/modify` |
| Delete | PUT | `/api/v1/{resource}/cancel` | `PUT /api/v1/stopOrder/cancel` |
| Batch Delete | PUT | `/api/v1/{resource}/cancel/multi` | `PUT /api/v1/stopOrder/cancel/multi` |
| Get History | GET | `/api/v1/{resource}/history` | `GET /api/v1/stopOrder/history` |
| Get Detail | GET | `/api/v1/{resource}/detail` | `GET /api/v1/ocoOrder/detail` |
| Get Today | GET | `/api/v1/{resource}/today` | `GET /api/v1/equity/order/today` |

### Path Parameters

Use `{id}`, `{scopeId}`, `{namespaceId}` for identifiers:

```
/api/v1/client/{id}
/api/v1/client/{id}/update
/api/v1/admin/scope/{scopeId}/update
```

### Nested Resources

For domain-specific resources:

```
/api/v1/{domain}/{resource}/{action}

Examples:
/api/v1/equity/order/history
/api/v1/equity/account/info
/api/v1/derivatives/order/place
```

---

## Request/Response DTO Naming

### Java Pattern

```
{Resource}{Action}Request.java
{Resource}{Action}Response.java
```

**Examples:**

| Purpose | Class Name |
|---------|------------|
| Place order | `StopOrderPlaceRequest`, `StopOrderPlaceResponse` |
| Modify order | `StopOrderModifyRequest`, `StopOrderModifyResponse` |
| Cancel order | `StopOrderCancelRequest`, `StopOrderCancelResponse` |
| Query history | `StopOrderHistoryRequest`, `StopOrderHistoryResponse` |
| Get detail | `OcoOrderDetailRequest`, `OcoOrderDetailResponse` |

### TypeScript Pattern

```
I{Resource}{Action}Request.ts
I{Resource}{Action}Response.ts
```

**Examples:**

| Purpose | Interface Name |
|---------|----------------|
| Create watchlist | `IWatchListRequest`, `IWatchListResponse` |
| Add symbol | `IAddSymbolWatchListRequest`, `IAddSymbolWatchListResponse` |
| Query data | `IQuoteRequest`, `IQuoteResponse` |

### Action Keywords

| Action | Use For | Examples |
|--------|---------|----------|
| `Place` | Creating orders | `StopOrderPlaceRequest` |
| `Add` | Creating entities | `TrailingOrderAddRequest` |
| `Create` | Creating resources | `CreateWatchListRequest` |
| `Modify` | Updating orders | `StopOrderModifyRequest` |
| `Update` | Updating entities | `ClientUpdateRequest` |
| `Cancel` | Canceling orders | `OcoOrderCancelRequest` |
| `Delete` | Deleting resources | `WatchListDeleteRequest` |
| `History` | Querying historical data | `StopOrderHistoryRequest` |
| `Detail` | Getting single item | `OcoOrderDetailRequest` |
| `Today` | Getting today's data | `OrderTodayRequest` |

---

## Service Method Naming

### Pattern

```
{action}{Resource}
```

### Method Naming by Operation

| Operation | Prefix | Example Methods |
|-----------|--------|-----------------|
| Create | `place*`, `add*`, `create*` | `placeStopOrder`, `addTrailingOrder`, `createWatchList` |
| Read | `query*`, `find*`, `get*` | `queryStopOrderHistory`, `findClientById`, `getAllResources` |
| Update | `modify*`, `update*` | `modifyStopOrder`, `updateClient` |
| Delete | `cancel*`, `delete*` | `cancelStopOrder`, `deleteWatchList` |

### Examples

```java
// Java
public StopOrderPlaceResponse placeStopOrder(StopOrderPlaceRequest request);
public StopOrderHistoryResponse queryStopOrderHistory(StopOrderHistoryRequest request);
public void cancelMultiStopOrders(StopOrderCancelRequest request);
```

```typescript
// TypeScript
async placeStopOrder(request: IStopOrderPlaceRequest): Promise<IStopOrderPlaceResponse>;
async queryStopOrderHistory(request: IStopOrderHistoryRequest): Promise<IStopOrderHistoryResponse>;
async cancelMultiStopOrders(request: IStopOrderCancelRequest): Promise<void>;
```

---

## Validation Checklist

When creating or reviewing new APIs, verify:

- [ ] URL uses `/api/v1/` prefix
- [ ] Resource name is camelCase
- [ ] HTTP method matches operation (POST=create, PUT=update/cancel, GET=read)
- [ ] Request DTO follows `{Resource}{Action}Request` pattern
- [ ] Response DTO follows `{Resource}{Action}Response` pattern
- [ ] Service method follows `{action}{Resource}` pattern
- [ ] TypeScript interfaces use `I` prefix

---

## Anti-Patterns to Avoid

| Avoid | Use Instead |
|-------|-------------|
| `/api/v1/stop-order` | `/api/v1/stopOrder` |
| `/api/v1/StopOrder` | `/api/v1/stopOrder` |
| `/api/v1/stopOrder/delete` | `/api/v1/stopOrder/cancel` |
| `StopOrderRequestPlace` | `StopOrderPlaceRequest` |
| `stopOrderPlace()` | `placeStopOrder()` |
| `WatchListRequest` (TS) | `IWatchListRequest` |

---

## Ecosystem Integration

This skill is part of the **TradeX Skill/Rule Ecosystem**. Always used as **Step 1** before:

| Next skill | When |
|------------|------|
| `derivatives-api-spec-format` | Creating API spec document |
| `tradex-order-api-response-standards` | Specifying Order API response |
| `tradex-knowledge` → `tradex-api-conventions` | Checking error format standards |

> **Orchestrator:** See `.cursor/rules/ecosystem-orchestrator.mdc` for full routing logic.
