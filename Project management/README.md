# Project Tracker Dashboard

Dashboard tập trung theo dõi tiến độ nhiều dự án (thay cho việc quản lý rời rạc trên nhiều Google
Sheet), soạn sẵn nội dung báo cáo tuần gửi sếp, và lưu lịch sử các lần báo cáo.

## Chạy thử

Mở `ProjectDashboard.jsx` trong Claude.ai (paste vào 1 chat mới, hoặc dùng trực tiếp làm Artifact)
— component tự seed data mẫu (data thật từ 3 project: C06-RAR, TT134, Research team) nếu chưa có
gì lưu trước đó.

## Tính năng

- **Tổng quan đa dự án**: sidebar liệt kê tất cả project, % tiến độ, số task quá hạn — không cần
  mở từng sheet
- **2 kiểu hiển thị theo cấu trúc thật của từng dự án**:
  - `gantt`: thanh tiến độ theo tuần, có đường "hôm nay" chạy xuyên timeline
  - `checklist`: theo Điều khoản / PIC / hạn chót (cho dự án dạng compliance)
- **Tự phát hiện task quá hạn** — không cần Midu tự rà lại
- **Weekly Report + History**: soạn Done/Plan/Issue cho từng dự án, "Log tuần này" để lưu vào
  lịch sử có ngày tháng (không ghi đè), xem lại các lần log trước
- **Weekly Digest**: gộp report của nhiều/tất cả dự án thành 1 message duy nhất để copy gửi chat,
  có thể xem lại digest của 1 ngày log cũ
- Sửa progress, thêm/xoá task/section/project ngay trong dashboard — không cần quay lại Sheet

## Cấu trúc file

```
project-tracker-dashboard/
├── README.md              ← file này
├── CLAUDE.md              ← context đầy đủ cho Claude Code (đọc trước khi sửa code)
└── ProjectDashboard.jsx   ← component chính, single-file
```

## Giới hạn cần biết trước khi tiếp tục

- Đây là Claude Artifact (chạy trong Claude.ai), dùng `window.storage` để lưu — **không phải**
  app độc lập, không tự kết nối Google Sheet thật (data hiện là seed 1 lần, không sync 2 chiều)
- Vị trí thanh Gantt theo tuần là ước lượng từ ảnh chụp màn hình, không phải đọc trực tiếp từ màu
  ô trong Sheet (giới hạn kỹ thuật khi export Sheet ra text)
- Chi tiết đầy đủ, data model, và roadmap gợi ý → xem `CLAUDE.md`
