---
name: html-plan
description: Create a self-contained HTML plan in the effective HTML style — pragmatic, visually organized, PM-readable. Use when the user wants a plan page, spec page, or any structured document in HTML format.
disable-model-invocation: true
---

# HTML Plan — Effective HTML Style Guide

Tham khảo các files trong `references/html-effectiveness/` để hiểu phong cách tổng thể.

## Design System

### Color Palette (effective-html)

```css
:root {
  --ivory:    #FAF9F5;  /* page background */
  --paper:    #FFFFFF;  /* card background */
  --slate:    #141413;  /* headings, dark text */
  --clay:     #D97757;  /* accent, links */
  --clay-d:   #B85C3E;  /* accent hover */
  --oat:      #E3DACC;  /* secondary accent */
  --olive:    #788C5D;  /* success/green */
  --g100:     #F0EEE6;  /* card bg, code bg */
  --g200:     #E6E3DA;  /* borders */
  --g300:     #D1CFC5;  /* borders, separators */
  --g500:     #87867F;  /* muted text */
  --g700:     #3D3D3A;  /* body text */
  --serif:    ui-serif, Georgia, "Times New Roman", Times, serif;
  --sans:     system-ui, -apple-system, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  --mono:     ui-monospace, "SF Mono", Menlo, Monaco, Consolas, monospace;
}
```

Có thể dùng palette thay thế (như biometric.html): navy/blue/teal/violet/green/amber/red phù hợp cho compliance/security docs.

### Dark Mode — BẮT BUỘC

Luôn include dark mode:
- CSS variables trên `:root` / `:root.dark` (hoặc `html.dark`)
- Theme toggle button (fixed top-right)
- `localStorage` persistence
- `<script>` apply-before-paint trong `<head>`, default `prefers-color-scheme`

```html
<script>
  (function() {
    const d = document.documentElement;
    const t = localStorage.getItem('theme');
    if (t === 'dark' || (!t && matchMedia('(prefers-color-scheme: dark)').matches)) {
      d.classList.add('dark');
    }
  })();
</script>
```

## Layout Components (Mẫu từ biometric.html)

### 1. Header — Doc Metadata

```html
<div class="doc-header">
  <div class="eyebrow">PROJECT · Initiative</div>
  <h1>Title — Mô tả ngắn</h1>
  <div class="subtitle">Context / motivation sentence</div>
  <div class="meta-row">
    <div class="meta-item"><strong>Version</strong> · v1.0</div>
    <div class="meta-item"><strong>Owner</strong> · PM · BE Lead</div>
    <div class="meta-item"><strong>Status</strong> · 🔄 In Progress</div>
    <div class="meta-item"><strong>Ngày</strong> · Tháng 6, 2026</div>
  </div>
</div>
```

### 2. Summary Bar — Key Metrics

5-column grid ở ngay dưới header, mỗi ô là 1 số lớn + label:

```html
<div class="summary-bar">
  <div class="sum-card"><div class="num blue">4</div><div class="lbl">Capability layers</div></div>
  <div class="sum-card"><div class="num teal">2</div><div class="lbl">User tracks</div></div>
  ...
</div>
```

### 3. Section Header — Icon + Title + Sub

```html
<div class="section-hdr">
  <div class="icon" style="background:#dbeafe">🧩</div>
  <h2>2. Phạm vi tổng quan</h2>
  <span class="sub">Subtitle ngắn gọn</span>
</div>
```

### 4. Card — Content Block

```html
<div class="card">
  <div class="card-title">📐 Tiêu đề card</div>
  <div class="card-body">
    <p>Content...</p>
  </div>
</div>
```

Có thể thêm `border-left: 4px solid var(--violet)` để phân biệt loại.

### 5. Callout — Highlighted Note

4 loại: info (blue), warn (amber), danger (red), good (green):

```html
<div class="callout">Default info callout</div>
<div class="callout warn">Warning</div>
<div class="callout danger">Critical</div>
<div class="callout good">Success / recommendation</div>
```

### 6. Data Table — Clean, Striped

```html
<table class="data-table">
  <thead><tr><th>Col 1</th><th>Col 2</th></tr></thead>
  <tbody>
    <tr><td class="mono">code</td><td>description</td></tr>
  </tbody>
</table>
```

Class `mono` cho cột chứa code/field names.
Add `risk-h` / `risk-m` / `risk-l` cho rows có màu risk.

### 7. Tags / Badges

```html
<span class="tag tag-be">BE</span>
<span class="tag tag-mob">Mobile</span>
<span class="tag tag-pm">PM</span>
<span class="tag tag-required">Required</span>
<span class="tag tag-blocking">Blocking</span>
<span class="tag tag-optional">Optional</span>
<span class="tag tag-good">Done</span>
```

### 8. Flow Box — ASCII Flow / Sequence

```html
<div class="flow-box">Login → Home
 ↓
[Step 1] ...
[Step 2] ...
 ↓
✅ Pass</div>
```

### 9. Grid Layouts

```html
<!-- 2 columns -->
<div class="col-2">...</div>
<!-- 3 columns -->
<div class="col-3">...</div>
```

### 10. Track Header + Body (cho multi-track features)

```html
<div class="track-hdr">
  <div class="t-num">A</div>
  <div>
    <h3>Track A — New users</h3>
    <div class="t-sub">Description</div>
  </div>
</div>
<div class="track-body">...</div>
```

Dùng `.track-hdr.b` cho track thứ 2 (màu violet).

### 11. Phase Tiles

```html
<div class="phase-row">
  <div class="phase-tile">
    <div class="p-num">Phase 1</div>
    <div class="p-name">🟢 Soft launch</div>
    <div class="p-desc">Description...</div>
    <div class="p-time">~3 tháng</div>
  </div>
  ...
</div>
```

### 12. Sprint Timeline

```html
<div class="sprint-grid">
  <div class="sprint-cell h">Workstream</div>
  <div class="sprint-cell h">Week 1-2</div>
  <div class="sprint-cell h">Week 3-4</div>
  <div class="sprint-cell ws">WS1 · Name</div>
  <div class="sprint-cell">Deliverable</div>
  <div class="sprint-cell">Deliverable</div>
  ...
</div>
```

### 13. Architecture Diagram

```html
<div class="arch"><pre>
           ┌──────────────┐
           │   Service    │
           └──────┬───────┘
                  ↓
           ┌──────────────┐
           │   Database   │
           └──────────────┘
</pre></div>
```

### 14. Open Questions

```html
<div class="oq-card">
  <div class="oq-num">1</div>
  <div class="oq-content">
    <div class="oq-q">[BLOCKING] Question title</div>
    <div class="oq-note">Context / resolution note</div>
  </div>
</div>
```

### 15. Risk Table Rows

```css
.risk-h { background: #fef2f2 !important; }  /* High */
.risk-m { background: #fffbeb !important; }  /* Medium */
.risk-l { background: #f0fdf4 !important; }  /* Low */
```

### 16. Footer

```html
<div class="doc-footer">
  <div>Project · Document · v1.0</div>
  <div>Status: ... | For: ... | Next Steps: ...</div>
</div>
```

## Cấu trúc HTML đề xuất (thứ tự sections)

1. **Dark mode script** (trong `<head>`, apply-before-paint)
2. **Header** — eyebrow + title + subtitle + metadata
3. **Summary bar** — key metrics (nếu phù hợp)
4. **Content** (`.content` wrapper)
   - Section 1: Context / Background / Regulatory
   - Section 2: Scope / Capabilities / Modules
   - Section 3-N: Chi tiết từng module / flow / track
   - Section N+1: Architecture
   - Section N+2: Phasing / Timeline
   - Section N+3: Risks / Open Questions
5. **Footer** — status + next steps

## Nguyên tắc

- **Pragmatic**: Không over-engineer CSS, dùng variables tái sử dụng
- **PM-readable**: Flow overview trước, technical detail sau, dùng tiếng Việt cho narrative
- **Self-contained**: 1 file .html duy nhất, không CDN (trừ Google Fonts), không dependencies
- **Dark mode**: BẮT BUỘC, hand-rolled CSS variables + theme toggle + localStorage
- **In đẹp**: `@media print` — ẩn theme toggle, white background, page-break-inside: avoid cho cards
- **Responsive**: Grid chuyển 1 cột trên mobile
- **Accessible**: Đủ contrast, semantic headings (h1→h2→h3)

## Tham khảo

- `references/html-effectiveness/` — 20 examples from Anthropic về effective HTML
- `TT134 - UBCK/Biometric System/biometric.html` — Mẫu plan HTML với đầy đủ components
- `TT134 - UBCK/Session Management/Session_Management_Spec.html` — Mẫu API spec HTML với dark mode + tables
- `TT134 - UBCK/Device Fingerprinting/Device_Fingerprinting_Spec.html` — Mẫu spec kết hợp narrative + technical
- `TT134 - UBCK/Order 2FA/Order_2FA_Spec.html` — Mẫu spec với flow + BE/FE split
