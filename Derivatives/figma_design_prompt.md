# Prompt: Màn hình Trade Phái Sinh - NHSV Pro

> **Context:** Thiết kế màn hình giao dịch phái sinh (VN30F, VN100F) cho nền tảng NHSV Pro, kế thừa design system từ [NHSV Pro Figma](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=707-16721&t=Xp3Q7pDCLBFJiPPX-11)

---

## 1. DESIGN BRIEF

**Objective:** Tạo màn hình trade chuyên nghiệp cho derivatives với 3 mục tiêu:
1. **Tốc độ:** < 3 giây từ mở màn hình đến đặt lệnh
2. **Thông tin:** Hiển thị đầy đủ dữ liệu quan trọng không bị lộn xộn
3. **Chuyên nghiệp:** Ngang tầm Bloomberg/Interactive Brokers

**Target Users:** 
- Professional traders (scalpers, day traders)
- Retail investors mới vào phái sinh
- Devices: Desktop (primary) → Tablet → Mobile

---

## 2. COLOR SYSTEM

```css
/* Extend from NHSV Pro + Derivatives-specific */

/* Primary Actions */
--long-buy: #00C853;        /* Green - Mua/Long */
--short-sell: #FF1744;      /* Red - Bán/Short */
--neutral: #757575;         /* Gray - Không đổi */

/* Price Levels */
--ceiling: #9C27B0;         /* Purple - Giá trần (CE) */
--floor: #2196F3;           /* Blue - Giá sàn (FL) */
--reference: #FFA726;       /* Amber - Giá tham chiếu (RE) */

/* Order Status */
--pending: #2196F3;         /* Blue ⏳ */
--partial: #FF9800;         /* Orange ⚡ */
--filled: #4CAF50;          /* Green ✅ */
--cancelled: #9E9E9E;       /* Gray ❌ */
--rejected: #F44336;        /* Red 🚫 */

/* Backgrounds */
--bg-primary: #FFFFFF;
--bg-secondary: #F5F5F5;
--bg-elevated: #FAFAFA;
--bg-danger: #FFEBEE;       /* Light red */
--bg-success: #E8F5E9;      /* Light green */
--bg-warning: #FFF3E0;      /* Light amber */

/* Bid/Ask Highlights */
--bid-bg: #E8F5E9;          /* Light green for bid rows */
--ask-bg: #FFEBEE;          /* Light red for ask rows */
```

---

## 3. TYPOGRAPHY

```css
/* Font Family */
--font-primary: 'Inter', -apple-system, sans-serif;
--font-mono: 'JetBrains Mono', 'Consolas', monospace;

/* Sizes & Weights */
--h1: 24px / SemiBold;      /* Page title */
--h2: 18px / SemiBold;      /* Section headers */
--h3: 16px / Medium;        /* Component titles */
--body: 14px / Regular;     /* Default text */
--price-large: 20px / Bold; /* Main price display */
--price-medium: 16px / Medium;
--caption: 12px / Regular;
--micro: 11px / Regular;    /* Timestamps */

/* Price Formatting Rules */
- Monospace font cho tất cả số/giá
- Align decimal points
- Thousand separator: 1,200.5
- Right-align trong tables
```

---

## 4. DESKTOP LAYOUT (1920x1080)

### Grid Structure: 3 Columns

```
┌─────────────────────────────────────────────────────────────┐
│ HEADER (60px, sticky)                                       │
│ VN30F2401▼ | 1,200.5(+5) | CE:1,250 FL:1,150 RE:1,200     │
│ Vol:15.2M | BASIS:+5.2(+0.43%)▲ | 🔴ATC 14:40:25           │
├──────────┬────────────────────────────┬─────────────────────┤
│ LEFT     │ CENTER                     │ RIGHT               │
│ 240px    │ Flexible (min 600px)       │ 380px               │
│          │                            │                     │
│[BID/ASK] │ [CHART]                    │ [ACCOUNT SUMMARY]   │
│ LADDER   │ Candlestick + Volume       │ P&L + Position      │
│          │ Height: Fill space         │ 120px height        │
│ ASK(Red) │ Min 400px                  │───────────────────  │
│ 1,205|150│                            │ [ORDER ENTRY]       │
│ 1,204|200│ [Chart Controls]           │                     │
│ 1,203|180│ 1M 5M 15M 1H 4H 1D         │ [Tabs]              │
│──────────│                            │ Thường|Đặt trước|Stop│
│Spread:0.1│                            │                     │
│──────────│                            │ [Order Type]        │
│ BID(Grn) │                            │ ATO ATC LO MTL MAK  │
│ 1,202|220│                            │                     │
│ 1,201|190│                            │ [Price Input]       │
│ 1,200|250│                            │ [-] 1,200.5 [+] [RT]│
│          │                            │ 🟢Realtime          │
│[PRESSURE]│                            │ [Bid][Mid][Ask][Last]│
│🟢▓▓▓▓▓60%│                            │                     │
│          │                            │ [Volume]            │
│          │                            │ [1][5][10][20][___] │
│          │                            │                     │
│          │                            │ Est: 12M ₫          │
│          │                            │ Margin: 1.2M ₫      │
│          │                            │                     │
│          │                            │ [LONG ▲] [SHORT ▼] │
│          │                            │  56px height        │
├──────────┴────────────────────────────┴─────────────────────┤
│ BOTTOM PANEL (200px, collapsible)                          │
│ [Chờ khớp:3] [Đã khớp:12] [Lịch sử]                       │
│ Table: Order# | Time | Symbol | Side | Price | Vol | Status│
└─────────────────────────────────────────────────────────────┘
```

**Spacing:**
- Content padding: 16px
- Component gaps: 12px
- Section gaps: 24px
- Border radius: 8px (cards), 4px (inputs)

---

## 5. KEY COMPONENTS

### 5.1 Header - Contract Info

```
Component: header-contract-info
Size: Full width × 60px
Layout: Horizontal auto-layout, gap: 16px
Padding: 12px 24px
Background: #FFFFFF
Border-bottom: 1px solid #E0E0E0
Box-shadow: 0 2px 4px rgba(0,0,0,0.05)

Elements:
├─ Symbol Dropdown: "VN30F2401" 16px Bold + chevron-down
├─ Divider (1px × 40px)
├─ Price Block:
│  ├─ Last: "1,200.5" 20px Bold (dynamic color)
│  ├─ Change: "+5.0" 16px Medium Green
│  └─ Percent: "(+0.42%)" 14px Regular Green
├─ Price Range:
│  "CE: 1,250" Purple | "FL: 1,150" Blue | "RE: 1,200" Amber
├─ Volume: "Vol: 15.2M" 14px
├─ BASIS: "BASIS: +5.2 (+0.43%) ▲" 14px Green
└─ Session: 🔴 "ATC 14:40:25" 14px Medium (right-aligned)

States: Default | Price updating (flash) | Session change
```

### 5.2 Bid/Ask Ladder

```
Component: ladder-panel
Size: 240px × Auto
Layout: Vertical auto-layout

[ASK Section - Red theme]
Header: "ASK (Bán)" 12px Bold #666
5 rows × ladder-row component:
  ├─ Price: 80px, 14px Mono Bold
  ├─ Volume: 80px, 14px Mono
  └─ Orders: 80px, 14px Mono (light text)
  Background: #FFEBEE (light red)
  Hover: #FFCDD2
  Height: 36px, Padding: 8px 12px

[Spread Indicator]
"Spread: 0.1" 12px Center
Background: #FFF3E0, Padding: 6px

[BID Section - Green theme]
5 rows × ladder-row component
Background: #E8F5E9 (light green)
Hover: #C8E6C9

[Pressure Bar]
Label: "Sức mua/bán" 12px
Progress: ▓▓▓▓▓▓▓░░░ 60%
Text: "Buy dominant" 11px Caption
Colors: Green (buy) vs Red (sell)

Interactions:
- Click row → Auto-fill price + highlight
- Hover → Tooltip with details
- Real-time update → Smooth transition (200ms)
```

### 5.3 Order Entry Panel

```
Component: order-entry-panel
Size: 380px × Auto
Padding: 16px

[Account Summary Card]
Background: #FAFAFA
Padding: 16px
Border-radius: 8px
Content:
├─ P&L Realized: "+2,500,000 ₫" 14px Green
├─ P&L Unrealized: "-500,000 ₫" 14px Red
├─ Position: "Long 10 @ 1,195" 14px
├─ Margin: "1.2M / 5M (24%)" 14px
└─ Progress bar: 24% filled (Blue)

[Tab Navigation]
Tabs: Thường | Đặt trước | Stop
Height: 40px
Active: #1976D2 background, White text, Bold
Border-bottom: 2px solid #1976D2

[Order Type - Segmented Control]
Component: order-type-selector
Layout: Horizontal, equal width
Buttons: ATO | ATC | LO | MTL | MAK | MOK
Size: Full width × 40px each
Active: #1976D2 background, White, Bold
Default: Transparent, #666 text
Hover: #E3F2FD background

[Smart Price Input]
Component: smart-price-input
Label: "Giá đặt lệnh" 12px #666
Input group:
  ├─ Button [-]: 40×40px, Icon 20px
  ├─ Value: "1,200.5" 18px Mono Bold, 160px width
  ├─ Button [+]: 40×40px
  └─ Button [🔄]: 36×36px (Realtime toggle)
Status: "🟢 Realtime" 11px Green (below input)
Quick buttons: [Bid] [Mid] [Ask] [Last]
  Size: 68×32px each, 12px text

States:
- Realtime: Green indicator + auto-update
- Paused: "⏱ 8s" countdown + orange
- Manual: Red indicator "🔴 Manual"

[Volume Input]
Label: "Khối lượng" 12px
Preset chips: [1] [5] [10] [20]
  Size: 48×40px, 14px Medium
Custom input: 80px width
Max label: "Max: 50 lots" 11px Caption

[Estimate Card]
Background: #F5F5F5
Padding: 12px
Border-radius: 4px
Content:
├─ "Giá trị: 12,000,000 ₫" 12px
└─ "Margin cần: 1,200,000 ₫" 12px

[Action Buttons]
Component: trade-buttons
Layout: Horizontal, 50% each
Size: Full width × 56px

Long button:
├─ Text: "LONG ▲" 16px Bold
├─ Background: #00C853
├─ Hover: #00E676 + shadow
└─ Active: Scale 0.98

Short button:
├─ Text: "SHORT ▼" 16px Bold
├─ Background: #FF1744
└─ Same hover/active effects

Validation borders:
- Valid: 2px Green
- Warning: 2px Yellow + ⚠️ icon
- Error: 2px Red + error message
```

### 5.4 Pending Orders Table

```
Component: pending-orders-table
Size: Full width × 200px (collapsible)
Background: #FFFFFF

[Tab Navigation]
Tabs: Chờ khớp (3) | Đã khớp (12) | Lịch sử
Badge: Blue circle with count

[Table Header]
Columns (fixed):
Order# | Time | Symbol | Side | Type | Price | Volume | Status | Actions
80px   | 80px | 100px  | 60px | 60px | 80px  | 120px  | 140px  | 80px

[Row Component: order-row]
Height: 48px
Padding: 8px 16px
Border-bottom: 1px #E0E0E0

Content:
├─ Order#: "#123456" 12px Mono
├─ Time: "09:35:12" 12px
├─ Symbol: "VN30F2401" 13px Bold
├─ Side: "Long ▲" with color + icon
├─ Type: "LO" 12px
├─ Price: "1,200.0" 13px Mono
├─ Volume: "10 | 3/7" (Total | Matched/Pending) 12px
├─ Status: Progress bar + "Chờ khớp 70%"
│  Bar: 8px height, rounded, colored by status
└─ Actions: [✏️] [❌] icon buttons (24×24px)

Hover state:
- Background: #F5F5F5
- Shadow: 0 2px 8px rgba(0,0,0,0.1)
- Show action buttons
- Scale: 1.01

Empty state:
- Icon: 📋 64×64px gray
- Text: "Chưa có lệnh chờ khớp" 14px
- Subtext: "Đặt lệnh mới để bắt đầu giao dịch" 12px
```

---

## 6. CHART COMPONENT

```
Component: trading-chart
Size: Flexible width × 400-800px height
Background: #FFFFFF
Padding: 16px

[Header Bar]
├─ Title: "VN30F2401" 14px Medium
├─ Timeframe selector:
│  [1M] [5M] [15M] [1H] [4H] [1D]
│  Active: #1976D2 background
│  Size: 40×32px each
└─ Tools: [Fullscreen] [Settings] icons

[Chart Area]
Type: Candlestick
Grid: Light gray (#F5F5F5)
Candles:
├─ Up: #00C853 (green)
├─ Down: #FF1744 (red)
└─ Wick: 1px width

Overlays:
├─ Price line: Last price (dashed, dynamic color)
├─ CE line: Purple dashed
├─ FL line: Blue dashed
└─ RE line: Amber dashed

[Volume Bar]
Height: 80px (bottom)
Bars: Same color as candle
Opacity: 0.6

Touch zones:
- Click price on ladder → Highlight on chart
- Hover candle → Show OHLC tooltip
- Drag → Pan chart
- Pinch/scroll → Zoom
```

---

## 7. MOBILE LAYOUT (375×812)

```
┌─────────────────────────┐
│ [Compact Header]        │ 50px
│ VN30F2401▼ 1,200.5(+5)  │
│ Vol:15M BASIS:+5.2▲     │
├─────────────────────────┤
│                         │
│ [Mini Chart]            │ 320px
│ Tap = fullscreen        │
│                         │
├─────────────────────────┤
│ [Summary Bar]           │ 60px collapsible
│ P&L:+2M | Pos:L10       │ Swipe up for details
├─────────────────────────┤
│ [Bid/Ask Compact]       │ 180px
│ 3 levels each           │
│ 1,205 | 150             │
│ 1,204 | 200             │
│ 1,203 | 180             │
│ ─── Spread: 0.1 ───     │
│ 1,202 | 220             │
│ 1,201 | 190             │
│ 1,200 | 250             │
├─────────────────────────┤
│ [Order Entry - Sticky]  │ Bottom
│ [ATO][ATC][LO][MAK]...  │ Horizontal scroll
│ [-] 1,200.5 [+] [RT]    │
│ Vol: [1][5][10][__]     │
│ ┌─────────┬───────────┐ │
│ │ LONG ▲  │ SHORT ▼   │ │ 56px
│ └─────────┴───────────┘ │
└─────────────────────────┘
        ↓
    Swipe up
        ↓
┌─────────────────────────┐
│ [Bottom Sheet]          │
│ ━━━━━ Pull down ━━━━━  │ Handle
│                         │
│ [Chờ khớp: 3]           │
│ ┌─────────────────────┐ │
│ │ VN30F L 1,200       │ │ Swipeable
│ │ 3/7 | 09:35:12      │ │ ← Edit | Cancel →
│ └─────────────────────┘ │
│ ...more orders          │
└─────────────────────────┘

Mobile-specific:
- Touch targets: 44×44px minimum
- Swipe gestures: Left=Cancel, Right=Edit
- Bottom sheet: Pull up to expand
- Collapsible sections with accordions
- Sticky order entry at bottom
```

---

## 8. COMPONENT STATES & ANIMATIONS

### Button States

```css
/* Default */
background: solid color;
box-shadow: none;

/* Hover */
background: lighten(10%);
box-shadow: 0 4px 12px rgba(0,0,0,0.15);
transform: scale(1.02);
transition: all 150ms ease;

/* Active/Pressed */
background: darken(10%);
transform: scale(0.98);
transition: all 100ms ease;

/* Disabled */
opacity: 0.4;
cursor: not-allowed;
pointer-events: none;
```

### Input States

```css
/* Default */
border: 1px solid #E0E0E0;
background: #FFFFFF;

/* Focus */
border: 2px solid #1976D2;
box-shadow: 0 0 0 3px rgba(25,118,210,0.1);

/* Valid */
border: 2px solid #00C853;
/* Show ✓ icon */

/* Error */
border: 2px solid #FF1744;
background: #FFEBEE;
/* Show error message below */

/* Disabled */
background: #F5F5F5;
color: #999;
cursor: not-allowed;
```

### Price Update Animation

```css
@keyframes priceFlash {
  0%   { background: transparent }
  50%  { background: rgba(0,200,83,0.2) } /* Green if up */
  100% { background: transparent }
}
/* Duration: 300ms, trigger on WebSocket update */

/* Red flash for price down */
50%  { background: rgba(255,23,68,0.2) }
```

### Loading States

```
Skeleton screens for:
- Chart: Gray rectangle with shimmer
- Ladder rows: Animated gradient bars
- Order table: Pulsing rows

Shimmer animation:
@keyframes shimmer {
  0%   { background-position: -200% 0 }
  100% { background-position: 200% 0 }
}
background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
background-size: 200% 100%;
animation: shimmer 1.5s infinite;
```

---

## 9. INTERACTION FLOWS

### Flow 1: Quick Order (Target: 3 seconds)

```
Frame 1: Desktop-default
├─ Order type: LO (pre-selected)
├─ Price: Auto-filled from realtime
└─ Cursor on volume input

User action: Click volume preset [10]
↓ Instant (0ms)

Frame 2: Volume selected
└─ Volume: 10 lots highlighted

User action: Click [LONG ▲]
↓ Animation: Button press (150ms)

Frame 3: Order submitted
├─ Toast notification: "✅ Đặt lệnh thành công"
├─ New row appears in pending orders
└─ Form resets to default
↓ Smart animate (300ms)

Frame 4: Success state
└─ Order visible in table with "Pending" status
```

### Flow 2: Edit Order

```
Frame 1: Order row (default)
├─ Hover: Show [✏️] [❌] buttons
└─ Cursor on edit button

User action: Click [✏️]
↓ Slide up animation (200ms)

Frame 2: Edit modal overlay
├─ Background: Darken 40%
├─ Modal: Centered, 480px width
├─ Pre-filled: Price + Volume
└─ Warning: "⚠️ Will cancel & recreate order"

User action: Modify price, click Confirm
↓ Loading spinner (500ms)

Frame 3: Success
├─ Modal closes (fade out)
├─ Order row updates
└─ Toast: "✅ Lệnh đã cập nhật"
```

### Flow 3: Ladder Price Selection

```
Frame 1: Ladder row (hover)
├─ Background: Highlight color
└─ Tooltip: "Click to use this price"

User action: Click price 1,200
↓ Flash animation (200ms)

Frame 2: Price auto-filled
├─ Price input: Value = 1,200
├─ Highlight: Green border
├─ Status: "⏱ Manual (10s)"
└─ Focus moves to volume input
```

---

## 10. RESPONSIVE BREAKPOINTS

```css
/* Desktop Large */
@media (min-width: 1920px) {
  Layout: 3-column (240px | flex | 380px)
  Ladder: 10 levels
  Chart: Full features
  All panels visible
}

/* Desktop */
@media (min-width: 1440px) {
  Layout: 3-column (220px | flex | 360px)
  Ladder: 5 levels
  Chart: Standard features
}

/* Tablet Landscape */
@media (min-width: 1024px) {
  Layout: 2-column (180px+chart | 340px)
  Ladder: Compact 3 levels
  Bottom panel: Collapsible
}

/* Tablet Portrait */
@media (min-width: 768px) {
  Layout: Single column stacked
  Chart: 50% height
  Order entry: Sticky bottom
}

/* Mobile */
@media (max-width: 767px) {
  Layout: Vertical stack
  Chart: 40% height, fullscreen on tap
  Ladder: 3 levels compact
  Order entry: Bottom sheet
  Bottom navigation tabs
}
```

---

## 11. ACCESSIBILITY

### Color Contrast (WCAG AA)

```
Text on white: #212121 (16:1 ratio)
Text on colored bg: Check contrast
Long button: White text on #00C853 (4.5:1 ✓)
Short button: White text on #FF1744 (4.5:1 ✓)

Don't rely on color alone:
- Long = ▲ icon + Green + "Long" text
- Short = ▼ icon + Red + "Short" text
- Status = Icon + Color + Text
```

### Keyboard Navigation

```
Tab order:
1. Symbol dropdown
2. Order type selector (arrow keys to switch)
3. Price input (up/down arrows adjust)
4. Volume input
5. Long button (Enter to submit)
6. Short button
7. Pending orders table (arrow keys navigate)

Shortcuts:
- Ctrl/Cmd + B: Select Long
- Ctrl/Cmd + S: Select Short
- Ctrl/Cmd + Enter: Submit order
- Esc: Cancel/Clear
```

### Screen Reader

```html
<!-- Example annotations -->
<button aria-label="Place Long order for VN30F at 1,200 with volume 10 lots">
  LONG ▲
</button>

<div role="status" aria-live="polite">
  Order #123456 filled. 10 lots at 1,200.5
</div>

<input 
  type="number" 
  aria-label="Order price"
  aria-describedby="price-help"
/>
<span id="price-help">Price must be between FL and CE</span>
```

### Touch Targets (Mobile)

```
Minimum: 44×44px (iOS guideline)
Preferred: 48×48px (Material Design)
Large actions: 56px height (Long/Short buttons)
Spacing: Min 8px between interactive elements
```

---

## 12. ERROR & EMPTY STATES

### Validation Errors

```
[Price Error]
Input: Red border (2px)
Icon: ⚠️ or ❌ (16px)
Message: "Giá vượt quá giá trần (CE: 1,250)" 12px Red
Position: Below input, left-aligned

[Margin Error]
Card: Light red background (#FFEBEE)
Icon: ⚠️ 20px
Title: "Không đủ margin" 14px Bold
Message: "Cần 1.2M ₫, còn 800K ₫" 12px
Action: "Nạp thêm" button (link to funding)

[Session Error]
Alert banner: Top of order entry
Background: #FFF3E0 (amber)
Icon: ⏰ 20px
Text: "Không thể đặt lệnh ATO ngoài phiên mở cửa (09:15-09:20)"
Dismiss: X button
```

### Empty States

```
[No Pending Orders]
Icon: 📋 64×64px Gray (#BDBDBD)
Title: "Chưa có lệnh chờ khớp" 16px Bold
Subtitle: "Đặt lệnh mới để bắt đầu giao dịch" 14px Gray
Action: "Đặt lệnh" button (scroll to order entry)
Illustration: Optional graphic

[No Chart Data]
Icon: 📊 64×64px Gray
Text: "Đang tải dữ liệu biểu đồ..." 14px
Spinner: 24px rotating

[Connection Lost]
Banner: Top of screen, full width
Background: #FFEBEE (red)
Icon: ⚠️ 20px
Text: "Mất kết nối. Đang kết nối lại..." 14px Bold
Action: "Thử lại" button
Auto-dismiss on reconnect
```

### Loading States

```
[Initial Load]
- Show skeleton screens
- Shimmer animation
- Progressive loading: Header → Ladder → Chart → Orders

[Action Loading]
Button:
├─ Disable interactions
├─ Show spinner (20px, replacing text)
├─ Opacity: 0.7
└─ Cursor: wait

Table row:
├─ Overlay with loading spinner
├─ Disable actions
└─ Opacity: 0.6
```

---

## 13. NOTIFICATIONS

### Toast Notifications

```
Position: Top-right corner
Width: 320px
Padding: 16px
Border-radius: 8px
Box-shadow: 0 4px 12px rgba(0,0,0,0.15)
Animation: Slide in from right (200ms)
Duration: 3s (success), 5s (error), persistent (warning)

[Success]
Background: #4CAF50
Icon: ✅ 24px White
Text: "Đặt lệnh thành công" 14px White Bold
Detail: "#123456 | VN30F Long 10 lots" 12px White
Action: "Xem" link (navigate to order)

[Error]
Background: #F44336
Icon: ❌ 24px White
Text: "Đặt lệnh thất bại" 14px White Bold
Detail: Error message from API 12px White
Action: "Thử lại" button

[Warning]
Background: #FF9800
Icon: ⚠️ 24px White
Text: Warning message
Action: "Đóng" X button

[Info]
Background: #2196F3
Icon: ℹ️ 24px White
Text: Info message
```

### In-line Notifications

```
[Order Filled]
Row highlight:
├─ Background: #E8F5E9 (green)
├─ Border-left: 4px solid #4CAF50
├─ Flash animation (500ms)
└─ Auto-fade to normal after 2s

[Order Rejected]
Row highlight:
├─ Background: #FFEBEE (red)
├─ Border-left: 4px solid #F44336
├─ Show reject reason tooltip
└─ Shake animation (300ms)
```

---

## 14. FIGMA ORGANIZATION

### Page Structure

```
📁 Derivatives Trading
│
├─ 📁 01_Design System
│  ├─ 🎨 Colors (Styles library)
│  ├─ 📝 Typography (Text styles)
│  ├─ 🔲 Icons (Component set)
│  ├─ 🔲 Buttons (Component set)
│  ├─ 🔲 Inputs (Component set)
│  └─ 📐 Grid & Spacing
│
├─ 📁 02_Components
│  ├─ 🔲 header-contract-info
│  ├─ 🔲 ladder-row (variants: bid, ask)
│  ├─ 🔲 smart-price-input
│  ├─ 🔲 order-type-selector
│  ├─ 🔲 volume-input
│  ├─ 🔲 trade-buttons
│  ├─ 🔲 order-row
│  ├─ 🔲 status-badge
│  ├─ 🔲 toast-notification
│  └─ 🔲 modal-overlay
│
├─ 📁 03_Desktop Screens
│  ├─ Frame: Desktop-1920x1080-Default
│  ├─ Frame: Desktop-1920x1080-Order-Pending
│  ├─ Frame: Desktop-1920x1080-Order-Filled
│  ├─ Frame: Desktop-1920x1080-Error-State
│  └─ Frame: Desktop-1920x1080-Loading
│
├─ 📁 04_Mobile Screens
│  ├─ Frame: Mobile-375x812-Default
│  ├─ Frame: Mobile-375x812-Bottom-Sheet
│  ├─ Frame: Mobile-375x812-Chart-Fullscreen
│  └─ Frame: Mobile-375x812-Order-Confirmation
│
├─ 📁 05_Prototypes
│  ├─ Flow: Quick Order (3 steps)
│  ├─ Flow: Edit Order (5 steps)
│  ├─ Flow: Ladder Selection (2 steps)
│  └─ Flow: Mobile Order Entry (4 steps)
│
└─ 📁 06_Documentation
   ├─ Cover page
   ├─ Component usage guide
   ├─ Interaction specs
   └─ Developer handoff notes
```

### Naming Conventions

```
Format: [category]-[name]-[variant]-[state]

Examples:
- derivatives-button-long-default
- derivatives-button-long-hover
- derivatives-button-long-active
- derivatives-button-long-disabled
- derivatives-input-price-default
- derivatives-input-price-focus
- derivatives-input-price-error
- derivatives-row-ladder-bid
- derivatives-row-ladder-ask
- derivatives-toast-success
- derivatives-modal-edit-order
```

### Component Properties

```
smart-price-input:
├─ value: number
├─ mode: realtime | paused | manual
├─ disabled: boolean
└─ error: string | null

trade-buttons:
├─ side: long | short
├─ loading: boolean
├─ disabled: boolean
└─ size: large | medium | small

order-row:
├─ order: OrderData
├─ selected: boolean
├─ hover: boolean
└─ status: pending | filled | cancelled

ladder-row:
├─ type: bid | ask
├─ price: number
├─ volume: number
├─ orders: number
└─ active: boolean
```

---

## 15. DESIGN CHECKLIST

### Before Design Phase

- [ ] Review NHSV Pro design system
- [ ] Reuse existing components where possible
- [ ] Set up color styles from this spec
- [ ] Set up typography styles
- [ ] Create 8px grid system
- [ ] Import icons (trading-specific)

### During Design

- [ ] Create component library first (atoms → molecules)
- [ ] Design desktop layout (primary)
- [ ] Add all component states (hover, active, disabled)
- [ ] Design error states and validations
- [ ] Design loading states and skeletons
- [ ] Design empty states
- [ ] Add micro-interactions
- [ ] Design mobile layout (responsive)
- [ ] Create prototypes for key flows
- [ ] Check color contrast (WCAG AA)
- [ ] Verify touch targets (44×44px min)
- [ ] Add annotations for developers

### Before Handoff

- [ ] All variants completed
- [ ] All states designed
- [ ] Component properties set up
- [ ] Prototypes working
- [ ] Assets exported (icons, images)
- [ ] Design tokens documented
- [ ] Developer notes added
- [ ] Accessibility specs documented
- [ ] User testing completed
- [ ] Stakeholder approval received

---

## 16. PLUGINS & TOOLS

**Recommended Figma Plugins:**
1. **Stark** - Accessibility checker (contrast, focus order)
2. **Autoflow** - Create user flow diagrams
3. **Content Reel** - Generate realistic trading data
4. **Contrast** - Quick contrast ratio check
5. **Unsplash** - Stock images (if needed)
6. **Iconify** - Icon library (trading icons)
7. **Anima** - Export to React/Vue code
8. **Figma to React** - Component code generation

**External Tools:**
- **TradingView Charting Library** - For chart inspiration
- **Coolors.co** - Color palette generator
- **Type Scale** - Typography scale calculator
- **8pt Grid Calculator** - Spacing helper

---

## 17. HANDOFF SPECIFICATIONS

### Export for Development

```json
{
  "design_tokens": {
    "colors": {
      "long": "#00C853",
      "short": "#FF1744",
      "neutral": "#757575",
      "ceiling": "#9C27B0",
      "floor": "#2196F3",
      "reference": "#FFA726"
    },
    "spacing": {
      "xs": "4px",
      "sm": "8px",
      "md": "12px",
      "lg": "16px",
      "xl": "24px"
    },
    "typography": {
      "h1": { "size": "24px", "weight": 600 },
      "h2": { "size": "18px", "weight": 600 },
      "body": { "size": "14px", "weight": 400 },
      "price": { "size": "20px", "weight": 700, "family": "monospace" }
    },
    "borderRadius": {
      "sm": "4px",
      "md": "8px",
      "lg": "12px"
    },
    "shadows": {
      "sm": "0 2px 4px rgba(0,0,0,0.05)",
      "md": "0 4px 8px rgba(0,0,0,0.1)",
      "lg": "0 4px 12px rgba(0,0,0,0.15)"
    }
  },
  "components": {
    "smart-price-input": {
      "props": ["value", "mode", "onChange", "onPause"],
      "states": ["default", "focus", "error", "disabled"]
    },
    "trade-buttons": {
      "props": ["side", "disabled", "loading", "onClick"],
      "states": ["default", "hover", "active", "disabled"]
    }
  },
  "breakpoints": {
    "mobile": "0-767px",
    "tablet": "768-1023px",
    "desktop": "1024-1919px",
    "desktop-large": "1920px+"
  }
}
```

### Assets Export

```
Icons:
- Format: SVG
- Sizes: 16×16, 20×20, 24×24, 32×32
- Export: @1x (web), @2x, @3x (mobile)
- Naming: icon-[name]-[size].svg

Images:
- Format: PNG (with transparency) or WebP
- Export: @2x, @3x for retina
- Optimize: TinyPNG or ImageOptim

Fonts:
- Inter: 400, 500, 600, 700
- JetBrains Mono: 400, 500, 700
- Format: WOFF2 (web)
```

---

## 18. SUCCESS CRITERIA

### Design Quality

- [ ] Matches NHSV Pro brand identity
- [ ] Follows Vietnamese market conventions
- [ ] Professional trader-grade interface
- [ ] Consistent color usage (green=long, red=short)
- [ ] Clear visual hierarchy
- [ ] Proper white space and balance
- [ ] Perfect alignment (use grids)

### UX Quality

- [ ] Quick order flow achievable in < 5 seconds
- [ ] Critical information always visible
- [ ] Actions have clear affordances
- [ ] Immediate feedback for all interactions
- [ ] Error prevention mechanisms in place
- [ ] Graceful error handling
- [ ] Loading states prevent confusion

### Technical Quality

- [ ] All components use auto-layout
- [ ] Responsive constraints set correctly
- [ ] Layer naming consistent
- [ ] No absolute positioning (use auto-layout)
- [ ] Component structure optimized (max 3 levels nesting)
- [ ] Design scales properly across breakpoints
- [ ] Exports work without manual cleanup

---

## 19. TIMELINE & DELIVERABLES

### Week 1: Foundation (40 hours)
- [ ] Component library setup (16h)
- [ ] Desktop layout - Default state (16h)
- [ ] Desktop layout - Order states (8h)

### Week 2: Expansion (40 hours)
- [ ] Mobile layout (16h)
- [ ] Responsive breakpoints (8h)
- [ ] Error & empty states (8h)
- [ ] Loading states (8h)

### Week 3: Polish (40 hours)
- [ ] Prototypes - Key flows (16h)
- [ ] Micro-interactions (8h)
- [ ] Accessibility review (8h)
- [ ] User testing prep (8h)

### Week 4: Handoff (40 hours)
- [ ] User testing & iteration (16h)
- [ ] Documentation (8h)
- [ ] Asset exports (8h)
- [ ] Developer handoff meeting (8h)

**Total: 160 hours (~4 weeks for 1 designer)**

### Final Deliverables

1. ✅ Figma file với complete screens & states
2. ✅ Interactive prototype (key flows)
3. ✅ Component library (reusable)
4. ✅ Design tokens (JSON export)
5. ✅ Asset package (icons, images)
6. ✅ Design specification doc (this file)
7. ✅ Developer handoff notes
8. ✅ User testing report (optional)

---

## 20. REFERENCES & INSPIRATION

**Industry Leaders:**
- **Interactive Brokers TWS** - Professional desktop trading
- **TradingView** - Chart UI excellence
- **Binance Futures** - Derivatives UX patterns
- **Bloomberg Terminal** - Information density
- **Robinhood** - Mobile simplicity

**Vietnamese Market:**
- **HOSE Trading Rules:** https://www.hsx.vn/
- **HNX Derivatives:** https://www.hnx.vn/
- **VSD Regulations:** https://www.vsd.vn/

**Design Resources:**
- **Existing Design:** [NHSV Pro](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=707-16721&t=Xp3Q7pDCLBFJiPPX-11)
- **IA Document:** [Trade Screen IA](./trade_screen_ia.md)
- **Material Design:** For component patterns
- **iOS Human Interface Guidelines:** For mobile standards

---

## 📝 PROMPT SUMMARY

**Copy this to start design in Figma:**

> Design a professional derivatives trading screen for NHSV Pro platform. Target: VN30F/VN100F futures on Vietnamese market.
>
> **Layout:** 3-column desktop (Ladder 240px | Chart flex | Order Entry 380px) + responsive mobile
>
> **Key Colors:** Long #00C853, Short #FF1744, CE #9C27B0, FL #2196F3, RE #FFA726
>
> **Core Components:** Contract header, Bid/Ask ladder (5 levels), Smart price input with realtime toggle, Segmented order type selector (ATO/ATC/LO/MTL/MAK/MOK), Volume presets, Large action buttons (LONG/SHORT 56px), Pending orders table
>
> **Critical Features:** Sub-3-second order flow, Price auto-fill from ladder, 10s pause mechanism on manual edit, Real-time validation, Progress bars for order status
>
> **Mobile:** Vertical stack, compact 3-level ladder, bottom sheet for orders, swipe gestures, 44px touch targets
>
> **States:** Include hover, active, disabled, error, loading, empty states
>
> **Reference:** Extend [NHSV Pro design system](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=707-16721&t=Xp3Q7pDCLBFJiPPX-11) + Follow [IA spec](./trade_screen_ia.md)
>
> **Priority:** Speed & information density over visual decoration. Bloomberg/IB level professionalism.

---

**Document Version:** 1.0  
**Created:** 2026-01-21  
**Author:** Senior Product Designer  
**Status:** Ready for Figma Design  
**Estimated Design Time:** 4 weeks (1 designer)
