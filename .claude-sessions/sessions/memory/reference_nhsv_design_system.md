---
name: nhsv-pro-design-system
description: "NHSV Pro design system — colors, typography, spacing, components, figma-cli tokens. Use whenever designing anything for NHSV Pro / TradeX."
metadata: 
  node_type: memory
  type: reference
  originSessionId: d837fb9c-360c-40cb-847e-0fbe7c082eab
---

# NHSV Pro Design System

Design system bundle fetched from `https://api.anthropic.com/v1/design/h/XeI3TAhO9V0AnpKNRp3WkA`
Extracted to `/tmp/design_files/nhsv-pro-design-system/` (session-local, re-fetch if needed).

**Key files:**
- `project/colors_and_type.css` — all CSS tokens (import into Figma via `figma-cli import`)
- `project/fonts/` — Lato 300/400/600/700/900 + NHSV-Icons.ttf
- `project/assets/` — logo, banners, sparklines, ~65 SVG icons
- `project/ui_kits/nhsv-pro-mobile/` — components.jsx, screens.jsx, App.jsx

---

## Non-negotiables

1. **Font: Lato only.** Weights: 300/400/600/700/900. Never substitute.
2. **Page background: `#EBECF0`** (var:surface-2). Cards are white (#FFF) with 12px radius, no border, no shadow when on page bg.
3. **Floating/elevated elements** (tooltips, modals, sheets): `shadow: 0px 8px 24px 0px rgba(0,34,71,0.12)` + 12–16px radius.
4. **Header gradient** `#00A9B4 → #01B483` (horizontal) — ONLY for top of authenticated screens. Never as page bg or button fill.
5. **Market colors** (Vietnam standard): Up = `#07A461` green, Down = `#DA1004` red, Reference = `#FCAF17` yellow. Always pair with ▲/▼ arrow.
6. **Chart palette** (multi-series, use in order): `#23A3E9` blue, `#F672A7` pink, `#F7B84C` yellow, `#0AB39C` green, `#6559CC` purple, `#FF9B36` orange, `#275EDB` dark-blue.
7. **No emoji.** Use SVG icons from assets/icons/ or NHSV-Icons.ttf.
8. **Numbers: tabular.** `font-variant-numeric: tabular-nums` on all monetary/price columns.
9. **Vietnamese first:** thousands sep = `.`, decimal sep = `,`. English reverse.

---

## Core Color Tokens (figma-cli var: names)

| Token | Hex | Use |
|---|---|---|
| `var:nhsv-main` | `#028D96` | Primary teal — buttons, active tabs, accents |
| `var:nhsv-secondary-icon` | `#45DAA4` | Mint — icon highlights on gradient |
| `var:fg-title` | `#2F4A4B` | H1/H2 headings |
| `var:fg-content` | `#333333` | Body copy |
| `var:fg-table-title` | `#8691B3` | Metadata, table headers, muted labels |
| `var:fg-disabled` | `#808A9D` | Disabled states |
| `var:surface` | `#FFFFFF` | Card background |
| `var:surface-2` | `#EBECF0` | Page background |
| `var:header-table` | `#F2F6FB` | Table header rows |
| `var:not-active-btn` | `#EEF1F5` | Secondary/inactive button bg |
| `var:separator` | `#EEF1F4` | 1px divider lines |
| `var:stroke` | `#E9EDF7` | Borders, strokes |
| `var:market-green` | `#07A461` | Price up / Buy |
| `var:market-red` | `#DA1004` | Price down / Sell |
| `var:market-yellow` | `#FCAF17` | Reference price |
| `var:market-purple` | `#B413EC` | Ceiling price |
| `var:market-teal` | `#00B4D8` | Floor price |
| `var:chart-green` | `#0AB39C` | Chart series 1 (teal-green line) |
| `var:chart-yellow` | `#F7B84C` | Chart series 2 (amber line) |
| `var:chart-blue` | `#23A3E9` | Chart series 3 |
| `var:overlay` | `rgba(40,48,80,0.6)` | Bottom-sheet scrim |
| `var:func-success-bg` | `#E6F6EF` | Success state bg |
| `var:func-error-bg` | `#FFEBE6` | Error state bg |

---

## Typography Scale

| Class | Size/Weight | Use |
|---|---|---|
| `.h-xlarge` | 32/700 | Big numeric heroes |
| `.h-large` | 28/600 | Screen headings |
| `.h-medium` | 24/600 | Section titles |
| `.h-small` | 20/600 | Card titles |
| `.t-xl` / `.t-xl-700` | 18/400 or 700 | Modal/sheet titles |
| `.t-lg` / `.t-lg-700` | 16/400 or 700 | Primary body, buttons |
| `.t-base` / `.t-base-700` | 14/400 or 700 | Default body |
| `.t-sm` / `.t-sm-700` | 12/400 or 700 | Metadata, table columns |
| `.t-xs` / `.t-xs-700` | 10/400 or 700 | Footnotes, tags |

---

## Radii

| Token | Value | Use |
|---|---|---|
| `--r-sm` | 4px | Tags, dots, placeholders |
| `--r-md` | 8px | Buttons, inputs, nested cards |
| `--r-lg` | 12px | Cards, banners — default |
| `--r-xl` | 16px | Sheets, dialogs |
| `--r-sheet-top` | 21px | Bottom sheet top corners |

---

## Shadows

| Token | Value | Use |
|---|---|---|
| `--shadow-bottom-line` | `0px 1px 0px 0px #EDF0F4` | Row separator |
| `--shadow-separator` | `0px 8px 0px 0px #EBECF0` | Section gap |
| `--shadow-card` | `0px 4px 16px 0px rgba(0,34,71,0.10)` | Card elevation |
| `--shadow-elevated` | `0px 8px 24px 0px rgba(0,34,71,0.12)` | Tooltips, modals |

---

## Layout

- Design canvas: **375 × 812** (iPhone X)
- Page padding: 16dp left/right
- Card padding: 16dp
- Vertical rhythm: 8 / 12 / 16 / 24dp
- Section gap: 8dp

---

## Event Calendar screens (updated 2026-06-17)

4 screens on "Official - Light Mode" page (page 40009945):
- EC_01 `40009945:276173` — Tất cả tab, all-market events list
- EC_02 `40009945:276313` — Danh mục tab, portfolio events with "Đang nắm giữ" badge
- EC_03 `40009945:276429` — Event Detail screen (push navigation, full screen)
- EC_04 `40009945:276502` — Empty state ("Không có sự kiện cổ tức trong 14 ngày tới")

**Component specs applied:**
- Header: Background Header "Page" `#F2F6FB` (NOT gradient) + Status h=44 + NavBar h=52
- Secondary Tab: active `#028D96` 14px/700 + 3px underline; inactive `#2F4A4B` 14px/700
- Tertiary chips: active = white bg + `#028D96` border 1px, r=5; inactive = `#F2F6FB` bg, r=4
- Event row H64: dot 8×8 left (green=#07A461 cash, teal=#028D96 stock), code 14px/700, subtitle 12px/#8691B3
- Date label: "HÔM NAY" badge `#2F4A4B` bg + white text, r=4; other dates: muted day + date + "N ngày nữa"
- "HÔM NAY" row tag: `#FFEBE6` bg + `#DA1004` text, r=4
- Badge "Đang nắm giữ": `#E6F6EF` bg + `#07A461` text, r=4
- Bottom Menu: `#FFFFFF` h=80, top-line `#EDF0F4`, active tab `#028D96`
- Event Detail: push navigation (NOT bottom sheet per spec), `#2F4A4B` CTA button, disclaimer text above CTA

**Event type dot colors:** green=#07A461 (cổ tức tiền mặt), teal=#028D96 (cổ phiếu thưởng/cổ tức CP)

---

## Chart Tooltip (designed 2026-06-17)

Correct NHSV Pro chart tooltip for "Thanh khoản khớp lệnh" (2-line chart):
- Card: white `var:surface`, radius 12px, `shadow-elevated`
- Header: label `var:fg-table-title` 10px + time `var:fg-title` 11px semibold
- Divider: 1px `var:separator`
- Hôm nay dot: `var:chart-green` (#0AB39C)
- Phiên trước dot: `var:chart-yellow` (#F7B84C)
- Labels: `var:fg-table-title` 12px
- Values: `var:fg-content` 13px 700
- Crosshair dot: chart-color + 2px white stroke + shadow
- Dark mode variant uses `#171C28` bg + `#262A34` divider + `#C3C8D6` text

Design spec frame in Figma at node `40009937:243566`, position x=507 y=110 (next to `02_01 Watchlist` screen).
