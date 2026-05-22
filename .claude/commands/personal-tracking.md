# Personal Work Tracking

Hỗ trợ tổng hợp và theo dõi quá trình làm việc cá nhân. Ghi/đọc files trong Midu-path.

## Midu-path Repo

| Nội dung | Path |
|----------|------|
| Repo | `/Users/nguyenduc/Personal/Repositories/Midu-path` |
| Profile (SSOT năng lực) | `Career/Profile.md` |
| Work context (Jira/epic/daily) | `Career/Work_Context.md` |
| Work tracking (quarter) | `Career/Work_Tracking/YYYY-QN.md` |
| Metrics registry | `Career/Metrics_Registry.md` |
| CV baseline cũ | `Career/Archive/Baseline_CV_Source.md` |
| Learnings | `Knowledge/` |

**Main work repo:** tradex-monitoring (this workspace)

## Use Cases

### 1 — Tổng hợp tuần (Weekly Summary)

Khi user hỏi công việc trong tuần:
1. Hỏi để lấy Jira data (issue done, paste list hoặc filter URL — hỏi 1–2 ý mỗi lần)
2. Tổng hợp: **Công việc đã làm** | **Đã học được** (gợi ý file trong `Knowledge/` nếu phù hợp)
3. Gợi ý update `Career/Work_Tracking/{YYYY-QN}.md` (Done + ref Jira/tradex-monitoring)

Reference Jira project: **NHMTS**, epic: **NHMTS-682** (từ `Work_Context.md` khi user chưa paste filter)

### 2 — Đánh giá giá trị feature/project

Khi user hỏi về metric hoặc lợi ích của feature:
1. Đọc `Career/Metrics_Registry.md` trước
2. Nếu chưa có metric tương ứng → hỏi user (1–2 ý)
3. Ghi nhận lợi ích cho **công ty** + **metric** đã định nghĩa
4. Gợi ý update Metrics_Registry khi user đồng ý

Tiêu chí mặc định nếu chưa có metric: **released thành công**

### 3 — Cập nhật năng lực cá nhân

Khi user chia sẻ kỹ năng mới / công nghệ vừa học:
1. Đề xuất update `Career/Profile.md` (Skills hoặc Domain section)
2. So sánh vs baseline cũ (`Baseline_CV_Source.md`) khi user cần

### 4 — Tổng kết năm (Growth & Salary Review)

Khi user yêu cầu tổng kết 1 năm:
1. Đọc: `Work_Tracking/` (các quarters), `Metrics_Registry.md`, `Profile.md`, `Knowledge/`
2. Output format:
   - Feature/project released thành công
   - Metrics đã ghi nhận (có số liệu)
   - Kỹ năng mới & cách áp dụng
   - Công việc nổi bật (bullet, rõ ràng, có điểm nhấn cho review lương)

## Style

- Hỏi **đúng trọng tâm**: mỗi lần **1–2** thứ còn thiếu — không spam câu hỏi
- Tổng hợp: bullet, phân mục, tách **Công việc / Học tập / Metric**
- **Chủ động gợi ý:** sau thông tin mới → có muốn lưu vào Profile.md, Work_Tracking, hay Metrics_Registry không?
- Chỉ **đề xuất** content + patch; user review rồi commit (trừ khi user yêu cầu ghi trực tiếp)
- Dùng đường dẫn tuyệt đối khi nhắc user trong chat
