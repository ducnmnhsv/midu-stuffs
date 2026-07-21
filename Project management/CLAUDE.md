# CLAUDE.md — Project Tracker Dashboard

Context file cho Claude Code khi tiếp tục implementation. Đọc file này trước khi động vào code.

## Mục tiêu ban đầu

Midu quản lý nhiều dự án (Derivatives, Smart OTP, Finlens, C06/Biometric, TT134 compliance...) trên
nhiều Google Sheet cấu trúc khác nhau, và mỗi tuần soạn tay 1 chat message cho sếp gồm: Done tuần
trước / Plan tuần tới / Issue-bottleneck / ảnh timeline. Mục tiêu là gộp việc này lại thành 1 chỗ
duy nhất thay vì rời rạc.

Đã cân nhắc 3 hướng (xem lại thread gốc nếu cần):
- **A. Master Google Sheet** — thấp effort, vẫn phải soạn tay report
- **B. Dashboard artifact độc lập** ← **đã chọn hướng này, là cái đang build**
- **C. Hybrid — Claude đọc trực tiếp Google Sheet mỗi tuần** — cân nhắc sau nếu B không tiện

## Trạng thái hiện tại

File `ProjectDashboard.jsx` là 1 React component single-file, chạy như Claude Artifact
(persistent storage qua `window.storage`, không phải app độc lập — xem "Giới hạn của môi trường
hiện tại" bên dưới). Đã seed data thật từ Google Sheet của Midu (đọc qua Google Drive connector,
không phải data giả).

### Data model

```
Project = {
  id, name,
  viewType: 'gantt' | 'checklist',   // quyết định cách render toàn bộ project
  weeks: string[],                    // chỉ dùng cho viewType='gantt'; rỗng cho 'checklist'
  currentWeekIndex: number,           // index vào weeks[] — cột được coi là "hôm nay"
  sections: [
    {
      id, name, pic?: string,        // pic optional, hiện chỉ TT134 dùng ở cấp section
      tasks: [
        // gantt task:
        { id, name, pic?, progress: 0-100, start: number|null, end: number|null }
        // checklist task:
        { id, name, no?: number, clause?: string, dueDate: 'YYYY-MM-DD' | 'TBD' | 'Done', progress }
      ]
    }
  ],
  report: {
    draft: { doneLastWeek, planNextWeek, issues },   // đang soạn dở, chưa "chốt"
    history: [
      { id, date: 'YYYY-MM-DD', doneLastWeek, planNextWeek, issues, overall, overdueSnapshot: string[] }
    ]
  }
}
```

Quyết định quan trọng: **`viewType` là per-project, không phải per-task.** Lý do: 3 sheet mẫu của
Midu có cấu trúc khác hẳn nhau — 2 cái là Gantt theo tuần (C06-RAR, Research team), 1 cái là
checklist theo Điều khoản + PIC + due date (TT134). Ép tất cả về 1 kiểu Gantt sẽ mất thông tin
(TT134 không có "tuần bắt đầu/kết thúc" thực sự có ý nghĩa, nó có deadline). Nên dashboard tự đổi
UI render theo `viewType` thay vì Midu phải tự quy đổi cấu trúc sheet cho khớp 1 khuôn.

### Overdue detection (tự động, không phải nhập tay)

`isTaskOverdue(task, project)`:
- `viewType='gantt'`: `task.end < project.currentWeekIndex` (và progress < 100)
- `viewType='checklist'`: `new Date(task.dueDate) < new Date()` thực tế (client clock), bỏ qua
  nếu `dueDate` là `'TBD'` hoặc `'Done'`

Dùng để: badge cảnh báo ở sidebar, gợi ý 1-click thêm vào ô "Issue" ở Weekly Report, và snapshot
vào `history` mỗi lần log.

### Report History (mới thêm ở lần làm việc gần nhất)

- `report.draft` là bản đang soạn — sửa thoải mái, không mất gì
- Bấm "Log tuần này" → `logReport()` chốt `draft` + `overallProgress()` hiện tại +
  `overdueSnapshot` hiện tại thành 1 entry trong `history`, khoá theo `date` (log 2 lần cùng
  ngày = ghi đè entry của ngày đó, không tạo trùng)
- Digest view có dropdown chọn ngày: `'latest'` = dùng draft+data sống; chọn 1 ngày cũ =
  `findEntryForDate()` tìm entry gần nhất **≤** ngày đó cho từng project (mỗi project có thể log
  lệch ngày nhau, không bắt buộc cùng lúc)
- **Chưa làm**: xoá 1 entry lịch sử, sửa lại 1 entry cũ, export lịch sử ra file riêng

### Storage

`window.storage` (Claude Artifacts persistent storage), key `'ptd:state:v2'`, `shared: false`
(chỉ Midu thấy). Đã bump từ `v1` → `v2` khi đổi schema `report` (thêm draft/history) — nếu đổi
schema lần nữa, **nhớ bump version key** để tránh app cũ đọc nhầm shape mới và crash.

## Nguồn data thật

Đọc qua Google Drive connector, fileId spreadsheet:
`1Ce1IK5VosWP4ZVldfjFYpQnZcOmf-3XdX9vJpfdC0rE`. `read_file_content` trả về **toàn bộ workbook gộp
thành nhiều bảng markdown liên tiếp**, không tách theo tên tab — phải tự đối chiếu nội dung bảng
với tên tab mong muốn.

Đã map được 3 bảng khớp TT134 / Research team / C06-RAR. **Chưa xử lý 2 bảng khác** cũng có trong
file (một bảng đánh giá vendor VNPT/FPT + implementation VNPT IDCheck; một bảng "Lotte convert
library / MTS Derivative" theo ngày, có vẻ thuộc dự án Derivatives) — không rõ chúng thuộc tab nào
nên chưa đưa vào seed data. Nếu cần thêm, phải hỏi lại Midu bảng nào ứng với tab nào trước khi map.

**Giới hạn quan trọng**: vị trí thanh Gantt theo tuần (ô nào tô màu) là **tô màu nền cell**, không
phải giá trị — text export của Sheet không lấy được thông tin này. Span `start/end` trong seed
data hiện tại là ước lượng bằng mắt từ ảnh chụp Midu gửi, không phải số đọc trực tiếp từ Sheet.
Nếu Midu cập nhật sheet thật, số liệu `%progress`/PIC/due date sẽ đúng nhưng vị trí thanh Gantt có
thể lệch — cần Midu chỉnh tay trong app (kéo dropdown "Từ tuần / đến tuần" khi thêm task, hoặc sẽ
cần thêm UI edit span cho task đã có — xem "Việc chưa làm" bên dưới).

## Giới hạn của môi trường hiện tại (Claude Artifact, không phải app độc lập)

- Chạy trong iframe artifact của Claude.ai, không phải web app deploy riêng
- Không dùng được `localStorage`/`sessionStorage` — bắt buộc dùng `window.storage` (API riêng
  của Claude Artifacts, key-value, có `shared` flag)
- Không tự gọi ra ngoài (fetch tới API ngoài) — không thể tự sync với Google Sheet thật từ trong
  artifact. Muốn sync thật (hướng C đã bàn) thì phải làm ở lớp khác (vd. 1 backend nhỏ, hoặc để
  Claude Code lấy Sheet qua Google Drive rồi ghi lại vào file/DB mà artifact đọc được), không thể
  làm ngay trong file `.jsx` này
- Styling: chỉ dùng Tailwind core utility classes định sẵn (không có JIT compiler), nên toàn bộ
  màu sắc/kích thước tùy biến đang dùng **inline style**, không dùng class dạng `bg-[#xxxxxx]`
  (sẽ không compile được trong môi trường artifact)
- Icon: `lucide-react` — nếu thêm icon mới, kiểm tra tên tồn tại trong bộ icon chuẩn trước khi
  import

## Việc chưa làm / gợi ý bước tiếp theo

1. **Sync 2 chiều với Google Sheet thật** (thay vì seed 1 lần) — hướng khả thi nhất là 1 script
   Claude Code chạy định kỳ, đọc Sheet qua Google Drive API, ghi ra file JSON, rồi dashboard đọc
   file đó thay vì `window.storage` seed cứng — cần quyết định dashboard đọc JSON này bằng cách
   nào nếu vẫn muốn chạy trong Claude Artifact (artifact không tự fetch file ngoài được)
2. Nếu muốn thoát khỏi giới hạn artifact (fetch API thật, deploy riêng, nhiều người dùng) — nên
   tách thành ứng dụng độc lập (Next.js/Vite) thay vì tiếp tục trong 1 file `.jsx` artifact; lúc
   đó `window.storage` cần thay bằng backend/DB thật
3. UI edit lại `start/end` (tuần) của 1 task **đã có sẵn** — hiện chỉ chỉnh được lúc **thêm task
   mới**; task cũ muốn dời tuần phải xoá thêm lại
4. Xoá/sửa 1 entry trong `report.history` (hiện chỉ thêm được, không xoá/sửa)
5. Export lịch sử report ra file (CSV/PDF) để lưu trữ ngoài dashboard
6. Xác nhận lại với Midu 2 bảng chưa map (vendor VNPT/FPT, Lotte/MTS Derivative) thuộc tab nào —
   thêm vào nếu liên quan
7. Cân nhắc thêm PIC cho task ở "add task" form (hiện tạo task mới không có ô nhập PIC)

## Quy ước code trong file này

- Toàn bộ màu sắc định nghĩa 1 lần ở `COLORS` (đầu file) — sửa theme thì sửa ở đây, không rải màu
  hex khắp component
- Helper functions thuần (`overallProgress`, `isTaskOverdue`, `composeDigest`,...) tách riêng khỏi
  component, không phụ thuộc React — nếu viết test, test trực tiếp các hàm này (xem cách extract
  bằng bracket-counting trong lịch sử phiên làm việc nếu cần dựng lại test harness)
- Component chia: `ProjectDashboard` (state + orchestration) → `GanttView` / `ChecklistView`
  (render theo viewType) → `ReportForm` / `DigestView` (report + history + digest)
