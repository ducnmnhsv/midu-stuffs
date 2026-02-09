# QA sessions

Tài liệu test/QA cho TradeX API, đồng bộ với Postman qua **TradeX QA session**.

## Sync với Postman

| Repo (tradex-monitoring) | Postman |
|---------------------------|---------|
| **QA sessions/** | Collection **TradeX QA session** (request test) + **TradeX API v2** (API main, tham chiếu) |

### Quy ước collection (QA agent)

| Collection | UID | Vai trò |
|------------|-----|--------|
| **TradeX API v2** (main) | `34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d` | API TradeX chính – tham chiếu; **không** tạo request test ở đây. |
| **TradeX QA session** | `34274942-8fe5bddd-fce2-4f76-bb6f-fb3f2760d40a` | Request test do QA agent tạo – **mỗi folder = một issue** (tên folder = mã issue, ví dụ `NHMTS-626`). |

- **Login (UAT)**: Request trong main collection – `POST /v1/login` → lấy `accessToken` → header `Authorization: jwt {{accessToken}}`.
- **QA sessions/** (repo): Tài liệu test – session notes, test plan, runbook; **Postman_Index.json** – index folder/request để tránh gọi getCollection nhiều lần (tiết kiệm quota/token).

### Index (tránh lãng phí token)

- **File**: [Postman_Index.json](Postman_Index.json) – lưu `main_collection`, `qa_collection`, `folders` (folder_id theo issue), `requests_by_issue`.
- Đọc index trước khi cần folderId/requestId; chỉ gọi getCollection khi cần refresh (ví dụ sau khi tạo folder mới trong Postman UI).

### Collection main (Environment)

- **TradeX API v2** (`34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d`): dùng **Environments** để đổi UAT/Production (`baseUrl`, `accessToken`).
- Environments: **TradeX UAT**, **TradeX Production**. Chi tiết: [Environment_Setup.md](Environment_Setup.md).

## Cấu trúc thư mục (gợi ý)

```
QA sessions/
├── README.md                    # File này
├── Environment_Setup.md         # Hướng dẫn TradeX API v2 + Environments
├── tradex-api-v2-flattened.json # Collection flatten (env-based) để import
├── scripts/
│   ├── flatten-collection-for-env.js  # Generate flattened collection
│   └── put-collection-v2.js           # Push collection lên Postman (cần API key)
├── Session_Notes/               # (tùy chọn)
├── Test_Plans/                  # (tùy chọn)
└── Runbooks/                    # (tùy chọn)
```

Khi mới bắt đầu có thể chỉ lưu file trực tiếp trong `QA sessions/`.

## Đặt tên file

- Session notes: `YYYY-MM-DD_Session_Notes.md` hoặc `YYYY-MM-DD_[Mô_tả].md`
- Test plan: `[Feature]_Test_Plan.md` hoặc `[TICKET]_Test_Plan.md`
- Runbook: `[API hoặc Feature]_Runbook.md`

## Agent

Agent **TradeX QA Postman** (`@tradex-qa-postman`) dùng Postman MCP để thao tác với folder **TradeX QA session** và lưu tài liệu vào đây. Kích hoạt bằng cách mention `@tradex-qa-postman` trong chat.
