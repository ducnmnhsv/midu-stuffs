---
name: tradex-orchestrator
description: "Điều phối toàn bộ công việc TradeX — tự động chạy pipeline analyst→creator→validator. Dùng khi: 'tạo spec', 'viết spec', 'API spec', 'Derivatives doc', 'FE issue', 'tạo tài liệu', 'phân tích API', 'trace API', 'cập nhật spec', 'sửa lại', 'làm lại'. Câu hỏi đơn giản ('field này là gì') thì trả lời trực tiếp."
---

# tradex-orchestrator

TradeX 작업을 Agent Team으로 실행한다: **analyst → creator → validator** 순서의 파이프라인 팀.

**실행 모드:** Agent Teams (TeamCreate + TaskCreate + SendMessage)
- 각 teammate가 자신의 context window에서 독립 실행
- 파일 기반 산출물 공유 (`_workspace/`)
- SendMessage로 handoff 통보

---

## Giai đoạn 0: Kiểm tra context

Trước khi tạo team, kiểm tra:

```
_workspace/ có tồn tại không?
├── CÓ + user yêu cầu sửa một phần → chạy lại 1 teammate (không tạo team mới)
├── CÓ + chủ đề hoàn toàn mới → đổi tên _workspace/ thành _workspace_prev/ rồi tạo team mới
└── KHÔNG → tạo team mới, chạy lần đầu
```

---

## Giai đoạn 1: Tạo Agent Team

```
TeamCreate(
  team_name: "tradex-pipeline",
  members: [
    { name: "analyst",   agentType: "tradex-analyst"   },
    { name: "creator",   agentType: "tradex-creator"   },
    { name: "validator", agentType: "tradex-validator" }
  ]
)
```

Sau đó tạo task list với dependency:

```
TaskCreate("Phân tích API/tính năng: [mô tả yêu cầu]",   assignee: "analyst")
TaskCreate("Tạo tài liệu dạng [loại]: [mô tả yêu cầu]",  assignee: "creator",   dependsOn: ["analyst task"])
TaskCreate("Kiểm tra và lưu tài liệu cuối cùng",          assignee: "validator", dependsOn: ["creator task"])
```

---

## Giai đoạn 2: Giám sát và tổng hợp

Lead theo dõi tiến độ team. Khi tất cả task hoàn thành:

1. Đọc `_workspace/03_validator_report.md`
2. Báo cáo kết quả cho user (xem format ở Giai đoạn 4)
3. Dọn dẹp team: `TeamDelete("tradex-pipeline")`

---

## Giai đoạn 3: Xử lý lỗi

| Tình huống | Xử lý |
|-----------|-------|
| Analyst không tìm được API | Creator nhận "analyst_missing", dùng template cơ bản |
| FE repo không truy cập được | Creator dùng placeholder, ghi rõ trong báo cáo |
| Validator FAIL | Lỗi rõ: validator tự sửa. Lỗi phức tạp: báo user sau khi team kết thúc |
| Teammate bị kẹt > 5 phút | Lead gửi SendMessage nhắc nhở; nếu vẫn kẹt thì spawn teammate thay thế |

---

## Giai đoạn 4: Báo cáo kết quả

```
✅ Hoàn thành

**File đã tạo:** {đường dẫn}
**Kết quả kiểm tra:** {PASS | PASS_WITH_WARNINGS | FAIL}

{Nếu có vấn đề:}
**Lưu ý:**
- {vấn đề 1}

**Có điểm nào cần cải thiện không?**
```

---

## Luồng dữ liệu

```
Lead tạo team & tasks
       │
       ▼
analyst  →  _workspace/01_analyst_findings.md
       │  SendMessage("creator", "Findings ready")
       ▼
creator  →  _workspace/02_creator_draft.md
         →  _workspace/02_creator_meta.md
       │  SendMessage("validator", "Draft ready")
       ▼
validator → _workspace/03_validator_report.md
          → Derivatives/Planning documentation/.../File.md
       │  SendMessage("lead", "Done: {result}")
       ▼
Lead tổng hợp → báo cáo user → TeamDelete
```

---

## Chạy lại một phần (không tạo team mới)

Khi user yêu cầu sửa một phần cụ thể:
- Gọi trực tiếp teammate liên quan bằng SendMessage hoặc spawn sub-agent đơn lẻ
- Không cần tạo lại toàn bộ team

---

## Kịch bản kiểm thử

### Luồng bình thường: Tạo API Spec
1. User: "Tạo spec cho API truy vấn danh sách lệnh Derivatives"
2. Lead tạo team tradex-pipeline với 3 task có dependency
3. analyst truy vết order-v2, lưu findings, SendMessage creator
4. creator tạo API Spec, lưu draft + meta, SendMessage validator
5. validator kiểm tra DTO naming, lưu file cuối, SendMessage lead
6. Lead đọc report, TeamDelete, báo cáo user

### Luồng lỗi: Yêu cầu không rõ
1. User: "Tạo spec cho tính năng X" (X mơ hồ)
2. analyst ghi "NOT FOUND" vào findings, SendMessage creator
3. creator tạo placeholder draft, SendMessage validator
4. validator FAIL, ghi "cần thêm thông tin", SendMessage lead
5. Lead TeamDelete, báo user cần cung cấp đường dẫn API hoặc tên tính năng rõ hơn
