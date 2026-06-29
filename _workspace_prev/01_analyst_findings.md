# Analyst Findings — Basis (`bs`) + Aggressive Volume (`asv`/`abv`) Fields

> **Scope:** Market quote API enhancement — basis for derivatives + accumulated active sell/buy volume.
> **Sources:** TradeX Knowledge (authoritative, Knowledge-First). FE codebase (`nhsv-mts-rn`) access was **denied** in this session, so FE references are unverified — flagged below.

---

## 0. TL;DR for the Creator

The fields the brief asks BE to add are **already named and documented** in the TradeX SymbolInfo field convention — they just aren't implemented in the live realtime model yet. The creator should write the issue as **"implement already-spec'd fields"**, not "design new fields". There is one **important naming mismatch** to resolve (brief says `asb`; convention says `abv`).

---

## 1. Field naming — CRITICAL: align with existing convention

Authoritative source: `TradeX Knowledge/System/symbolinfo-api-fields-guide.md` (§2.10, §3, §4).

| Brief term | Existing TradeX convention | Full name | Meaning | Status |
|------------|---------------------------|-----------|---------|--------|
| `bs` (basis) | **`bs`** | basis | Chênh lệch = Giá HĐTL − Spot (điểm) | ✅ name matches brief |
| `asv` (active sell vol) | **`asv`** | accumulatedSellVolume | Tổng KL khớp với **bên bán chủ động** trong phiên (aggressive sell) | ✅ name matches brief |
| `asb` (active buy vol) | **`abv`** | accumulatedBidVolume | Tổng KL khớp với **bên mua chủ động** trong phiên (aggressive buy) | ⚠️ **MISMATCH — brief calls it `asb`; convention calls it `abv`** |
| underlying index code | **`bc`** | baseCode | Mã chỉ số cơ sở, e.g. `"VN30"` | ℹ️ brief didn't name this; convention already has it |

**Action for creator:** Use `bs`, `asv`, `abv`, `bc` (the documented names). Add a note in the issue explicitly mapping the brief's `asb` → `abv` so FE/BE don't drift. Do NOT introduce a new `asb` field — it would duplicate `abv`.

### 1.1 Semantic direction sanity-check (matches brief)
The `mb` (matchedBy) field marks the **aggressor side**:
- `mb = ASK` → buyer lifted the ask → **active BUY** → accumulate into **`abv`** (accumulatedBidVolume).
- `mb = BID` → seller hit the bid → **active SELL** → accumulate into **`asv`** (accumulatedSellVolume).

This is consistent with the brief's logic. (Note the brief's prose labels are slightly tangled — "asv = active sell, aggressor is buyer" — but the `mb`→bucket mapping above is the correct, convention-aligned rule.)

---

## 2. Socket / data-source facts (`mb`, `mv`, `vo`, `ss`, `ep`, `c`)

Source: `market-data-channels.md` §3, §5; `symbolinfo-api-fields-guide.md` §6.

| Field | Meaning | Source topic |
|-------|---------|--------------|
| `c` | current/last price | Quote (`quoteUpdate`) |
| `mb` | matchedBy — aggressor side: `ASK`/`BID` | Quote (`quoteUpdate`) |
| `mv` | matchingVolume — KL of the **last** matched trade | Quote (`quoteUpdate`) |
| `vo` | volume — cumulative session traded volume | Quote (`quoteUpdate`) |
| `ss` | session — `ATO`/`LO`/`ATC`/`PLO`/`CLOSED` | BidOffer (`bidOfferUpdate`) |
| `ep` | expectedPrice — projected match price, ATO/ATC only | Extra (`extraUpdate`) |

**Lotte raw `mb` mapping** (collector): Lotte `parts[24]` → `"83"` = ASK, `"66"` = BID. Already parsed by `market-collector-lotte`.

**Pipeline:** `Lotte WS → market-collector-lotte (Java) → Kafka → realtime-v2 (Java, writes Redis) + ws-v2 (Node, compresses field names, publishes WS)`.
- REST `/api/v2/market/symbol/{code}/quote` (a.k.a. `/symbol/latest`) reads from Redis populated by `realtime-v2`.
- Socket `market.quote.{code}` is published by `ws-v2`, which **renames fields to the short form** in `ws-v2-main/parser.js` (`convertDataPublishV2Quote()`).

**Key consequence for both issues:** Adding `bs`/`asv`/`abv` requires changes in **two places** — (a) `realtime-v2` to compute & persist into the Redis SymbolInfo model, and (b) `ws-v2/parser.js` to include the short field name in the published payload. REST and socket both read the same realtime model, so doing it in `realtime-v2` gets both surfaces.

### 2.1 `market.quote.dr` (brief mentions this channel)
The brief references socket `market.quote.dr`. The Knowledge base documents the channel as `market.quote.{symbol}` (the `{symbol}` being e.g. `41I1G6000`); a literal `.dr` suffix is **not documented** in Knowledge. Likely the brief means the derivatives symbol channel (the `dr` may be a typo or an env/instance suffix). **Creator should flag this as a clarify-question to BE**, not assume.

---

## 3. Derivative symbol → underlying index mapping

Brief states:
| Prefix | Underlying |
|--------|-----------|
| `41I1xxxxx` | VN30 |
| `41I2xxxxx` | VN100 |

**Knowledge-base status:**
- The `bc` (baseCode) field is **already defined** to carry the underlying index code (example value `"VN30"`). So the mapping output already has a home field.
- The prefix→index rule (`41I1`→VN30, `41I2`→VN100) is **NOT documented anywhere in TradeX Knowledge**. `init-job.md` confirms futures examples as `VN30F2401` etc., and that VN30 constituent lists come from `lotte-bridge` job (Kafka `indexStockListUpdate`) into `realtime-v2` `IndexStockListRepository`. But the specific `41Ix` numeric-prefix convention is **new information from the brief / FE spec** and is unverified against Knowledge or live code.

**Action for creator:** Treat the `41I1`/`41I2` prefix mapping as a **requirement input from the FE spec** that BE must confirm against the actual Lotte symbol scheme. Add as a clarify-question: "Is futures→underlying resolved by `41Ix` prefix, or should BE use an existing symbol-metadata field?" Note: VN100 as a derivative underlying is itself worth confirming — Knowledge only references VN30 futures.

---

## 4. Basis calculation during ATO/ATC

Brief's acceptance criterion: "Calculated correctly during ATO/ATC sessions (use correct reference price per session)."

**Knowledge facts relevant to the price-selection question:**
- During `ATO`/`ATC`, the futures **last/current price `c` is not the live matched price** — the meaningful price is the **`ep` (expectedPrice)** projected match price (`extraUpdate` topic). Outside ATO/ATC, `ep` is null and `c` is the live matched price.
- Session impact table (`market-data-channels.md` §5.3): `ep`/`exv`/`exc`/`exr` are populated only in ATO & ATC; null in LO/PLO/CLOSED.

**Recommended logic to put in the issue (as a proposed answer + clarify-question):**
- Continuous (LO): `basis = futures.c − underlying.c`
- ATO/ATC: `basis = futures.ep − underlying.ep` (use expected/projected price for **both legs** so they're consistent), falling back to `c` if `ep` unavailable.
- This directly answers the brief's open question "ATO/ATC: dùng `expectedPrice` hay `currentPrice`?" → **`expectedPrice` during ATO/ATC, `currentPrice` otherwise** — but mark it as needs-BE-confirmation since it's a design decision.

---

## 5. Answers to the brief's open questions

| Brief question | Finding |
|----------------|---------|
| Field name `bs` vs `basis`? | Short form **`bs`** in socket/compressed payload (full name `basis`). Convention already fixed this — §3 of fields guide. |
| Is `bs` in REST `/symbol/{code}/quote` or socket-only? | Both should carry it: REST and socket read the same `realtime-v2` Redis model. `bs` belongs in the SymbolInfo model → appears in both. Confirm REST currently exposes the derivatives field group. |
| ATO/ATC: `expectedPrice` or `currentPrice`? | Use **`expectedPrice` (`ep`) during ATO/ATC**, `currentPrice` (`c`) otherwise — see §4. Needs BE confirmation. |
| `asv`/`asb` field names? | `asv` correct; **`asb` should be `abv`** (accumulatedBidVolume) — see §1. |
| Source for asv/abv accumulation? | `mb` (aggressor) + `mv` (last-match volume), accumulated per session; reset at session start. Field guide §2.10 explicitly says these "có thể suy từ `mb` + `mv` tích lũy". |

---

## 6. Implementation surface (where BE changes land) — for Technical Details section

| Layer | Component (from Knowledge appendix) | Change |
|-------|-------------------------------------|--------|
| Collector | `market-collector-lotte` `WsConnection.java` `handleStockQuote()` | Already provides `mb`, `mv` — likely no change |
| Realtime | `realtime-v2` `QuoteUpdateHandler` / `QuoteService.updateQuote()` | Accumulate `asv`/`abv` per session from `mb`+`mv`; compute `bs` from futures vs `bc` underlying; persist to Redis SymbolInfo |
| Realtime | `realtime-v2` session/reset path (`MarketStatusService`) | Reset `asv`/`abv` to 0 at session start |
| Underlying lookup | `realtime-v2` `IndexStockListRepository` / symbol metadata | Resolve futures → underlying index (`bc`); fetch underlying live/expected price |
| WS publish | `ws-v2` `parser.js` `convertDataPublishV2Quote()` | Add short keys `bs`, `asv`, `abv` to published quote payload |
| REST | `market-query-v2` (`/symbol/{code}/quote`) | Ensure derivatives field group incl. `bs`, `asv`, `abv` is returned |

**Performance note (brief AC "no perf degradation"):** Both calcs are O(1) per quote tick (basis = one subtraction needing the cached underlying price; asv/abv = one accumulator add keyed off `mb`). The only cross-symbol dependency is basis needing the underlying index's latest price already cached in Redis — cheap. Session-reset is a one-time clear per session boundary.

---

## 7. Gaps / unverified — creator must surface as clarify-questions

1. **`asb` vs `abv` naming** — must be reconciled (recommend `abv`).
2. **`market.quote.dr` channel suffix** — not in Knowledge; confirm exact channel name.
3. **`41I1`/`41I2` → VN30/VN100 prefix rule** — FE-spec input, not verified against Lotte symbol scheme or live code.
4. **VN100 futures existence** — Knowledge only documents VN30 futures.
5. **FE codebase not inspected** — tool access to `nhsv-mts-rn` was denied this session; any claim about existing FE basis-calc code is unverified. The brief states FE currently self-calculates basis, which is plausible but unconfirmed.
6. **ATO/ATC price source for basis** — recommended `ep`, but it's a design decision for BE.

---

**Document Status:** ✅ Analysis complete (Knowledge-based; FE unverified)
**For:** creator (BE issue authoring), team-lead
**Next Steps:** Creator drafts `BE_Market_Quote_Fields_Enhancement.md` using §1 naming, §5 answers, §6 implementation surface, §7 clarify-questions.
