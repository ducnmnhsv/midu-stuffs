---
name: tradex-orchestrator
description: "TradeX 작업 오케스트레이터 — API 분석, Spec/문서/FE이슈 생성, 컨벤션 검증을 자동으로 파이프라인 실행. 'API spec 만들어', 'spec 작성', 'Derivatives doc', 'FE issue 만들어', 'API 분석해줘', 'TradeX API 추적', 'Derivatives 문서', '다시 만들어', '업데이트', '수정해줘', '재실행' 요청 시 반드시 이 스킬을 사용할 것. 단순 질문(예: '이 필드가 무엇인가')은 직접 응답 가능."
---

# tradex-orchestrator

Thực thi công việc TradeX theo pipeline 3 giai đoạn: **Analyst → Creator → Validator**

**Chế độ thực thi:** Sub-agent pipeline (mỗi agent lưu kết quả vào `_workspace/`, agent sau đọc lại)

## Giai đoạn 0: Kiểm tra context

Trước khi bắt đầu, kiểm tra trạng thái làm việc hiện tại:

```
_workspace/ có tồn tại không?
├── CÓ + user yêu cầu sửa một phần → Chạy lại một phần (chỉ giai đoạn cần thiết)
├── CÓ + chủ đề mới / tính năng khác → Đổi tên thành _workspace_prev/ rồi chạy mới
└── KHÔNG → Chạy lần đầu
```

Thư mục `_workspace/` tạo tại gốc project (`tradex-monitoring/`).

## Giai đoạn 1: Phân tích (tradex-analyst)

```
Agent(
  prompt: "Bạn là tradex-analyst. Hãy phân tích [yêu cầu user].
           Đọc .claude/agents/tradex-analyst.md để biết cách làm.
           Lưu kết quả vào _workspace/01_analyst_findings.md.",
  subagent_type: "general-purpose",
  model: "opus"
)
```

**Đầu vào:** Yêu cầu gốc của user
**Đầu ra:** `_workspace/01_analyst_findings.md`

**Có thể bỏ qua nếu:** Yêu cầu là viết tài liệu thuần túy (ví dụ: "Viết PM doc từ spec này") và API đã rõ ràng.

## Giai đoạn 2: Tạo tài liệu (tradex-creator)

```
Agent(
  prompt: "Bạn là tradex-creator. Tạo tài liệu dạng [loại] cho [yêu cầu user].
           Đọc .claude/agents/tradex-creator.md để biết cách làm.
           Đọc _workspace/01_analyst_findings.md trước (nếu không có thì tự phân tích cơ bản).
           Lưu nháp vào _workspace/02_creator_draft.md và meta vào _workspace/02_creator_meta.md.",
  subagent_type: "general-purpose",
  model: "opus"
)
```

**Đầu vào:** `_workspace/01_analyst_findings.md` + yêu cầu gốc
**Đầu ra:** `_workspace/02_creator_draft.md`, `_workspace/02_creator_meta.md`

## Giai đoạn 3: Kiểm tra (tradex-validator)

```
Agent(
  prompt: "Bạn là tradex-validator. Kiểm tra và lưu tài liệu cuối cùng.
           Đọc .claude/agents/tradex-validator.md để biết cách làm.
           Đọc _workspace/02_creator_draft.md và _workspace/02_creator_meta.md.
           Lưu báo cáo vào _workspace/03_validator_report.md.
           Lưu file cuối vào Target Path trong meta.",
  subagent_type: "general-purpose",
  model: "opus"
)
```

**Đầu vào:** `_workspace/02_creator_draft.md`, `_workspace/02_creator_meta.md`
**Đầu ra:** `_workspace/03_validator_report.md` + file cuối cùng tại Target Path

## Giai đoạn 4: Báo cáo kết quả

Sau khi pipeline hoàn tất, báo cáo cho user:

```
✅ Hoàn thành

**File đã tạo:** {đường dẫn cuối}
**Kết quả kiểm tra:** {PASS | PASS_WITH_WARNINGS | FAIL}

{Nếu PASS_WITH_WARNINGS hoặc FAIL:}
**Lưu ý:**
- {vấn đề 1}
- {vấn đề 2}

**Có điểm nào cần cải thiện không?**
```

## Chạy lại một phần

Khi user yêu cầu "sửa phần này", "đổi response format", v.v.:
- Giai đoạn 0 phát hiện `_workspace/` đã có
- Chỉ gọi lại agent cần thiết (creator hoặc validator)
- Đưa hướng dẫn sửa cụ thể vào prompt của agent

## Xử lý lỗi

| Tình huống | Xử lý |
|-----------|-------|
| Không có analyst findings | Creator dùng template cơ bản, ghi "analyst_missing" |
| Không truy cập được FE repo | Dùng placeholder, ghi rõ trong báo cáo |
| Validator FAIL | Lỗi rõ ràng: validator tự sửa. Lỗi phức tạp: báo user |
| Không có thư mục đích | Validator tạo thư mục rồi lưu |

## Luồng dữ liệu

```
Yêu cầu user
     │
     ▼
Giai đoạn 0: Kiểm tra _workspace/
     │
     ▼
Giai đoạn 1: tradex-analyst
     → _workspace/01_analyst_findings.md
     │
     ▼
Giai đoạn 2: tradex-creator
     → _workspace/02_creator_draft.md
     → _workspace/02_creator_meta.md
     │
     ▼
Giai đoạn 3: tradex-validator
     → _workspace/03_validator_report.md
     → Derivatives/Planning documentation/{Category}/{Folder}/{File}.md
     │
     ▼
Giai đoạn 4: Báo cáo cho user
```

## Kịch bản kiểm thử

### Luồng bình thường: Tạo API Spec
1. User: "Tạo spec cho API truy vấn danh sách lệnh Derivatives"
2. Giai đoạn 1: analyst → truy vết order-v2, xác nhận Lotte response
3. Giai đoạn 2: creator → tạo API Spec theo template chuẩn
4. Giai đoạn 3: validator → kiểm tra DTO naming, Order API format
5. Kết quả: `Derivatives/Planning documentation/Order management/Specifications/Order_Query_API_Spec.md`

### Luồng lỗi: Không tìm được API
1. User: "Tạo spec cho tính năng X" (X không rõ)
2. Giai đoạn 1: analyst → ghi "API NOT FOUND" vào findings
3. Giai đoạn 2: creator → tạo nháp với placeholder
4. Giai đoạn 3: validator → FAIL, yêu cầu user cung cấp thêm thông tin
