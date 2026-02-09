# TradeX Postman – Dùng Environment (đã tối ưu)

Collection **TradeX API v2** dùng **Postman Environments** thay vì tách folder UAT/Production. Một bộ request, chuyển môi trường bằng cách đổi Environment.

## Collection & Environments

| Loại | ID / Tên | Ghi chú |
|------|----------|--------|
| **Collection** | **TradeX API v2** | `34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d` |
| **Environment** | **TradeX UAT** | `34274942-8cc25725-7327-4ee1-9f95-1c8bea6d562c` |
| **Environment** | **TradeX Production** | `34274942-2bf14cd1-dcc2-46d1-9266-5f49633d0d3e` |

## Biến Environment

Cả hai environment đều có:

| Biến | Ý nghĩa | UAT | Production |
|------|--------|-----|------------|
| `baseUrl` | Base URL API | `https://tnhsvpro.nhsv.vn` | `https://nhsvpro.nhsv.vn` |
| `accessToken` | JWT (set sau khi gọi Login) | (để trống, Login sẽ set) | (để trống, Login sẽ set) |

## Cách dùng

1. Chọn Environment: **TradeX UAT** hoặc **TradeX Production** (góc phải Postman).
2. Chạy request **Login** (folder Login) để lấy token → script Test sẽ set `accessToken` vào environment.
3. Các request cần auth dùng header: `Authorization: jwt {{accessToken}}`.
4. Mọi request dùng URL: `{{baseUrl}}/rest/api/v1/...` (tự đổi theo environment).

## Cấu trúc collection

- **Một cấp folder**: mỗi folder = một category nghiệp vụ (Login, eKYC, Account, Market, Order, …).
- **Không còn subfolder Prod**: cùng một request, đổi environment là đổi UAT/Production.

## Điền nội dung collection (cần làm một lần)

Collection **TradeX API v2** đã có trong workspace nhưng có thể đang trống. Để đưa toàn bộ request (22 folder, env-based) vào:

- **Cách 1 – Script + API Key** (khuyến nghị):
  ```bash
  cd "QA sessions/scripts"
  POSTMAN_API_KEY=your_postman_api_key node put-collection-v2.js ../tradex-api-v2-flattened.json
  ```
- **Cách 2 – Import trong Postman**: Vào Postman → Import → File → chọn `QA sessions/tradex-api-v2-flattened.json`. Nếu tạo collection mới thì xóa collection "TradeX API v2" trống và đổi tên collection vừa import thành **TradeX API v2**.

## So với collection cũ (TradeX API)

| | TradeX API (cũ) | TradeX API v2 |
|---|-----------------|----------------|
| Collection ID | `34274942-d3eefba9-8d3a-4d26-bedd-bd4c23096666` | `34274942-d349da1f-7f4f-4182-b16b-1cacba636b5d` |
| UAT/Prod | Request UAT trong folder, request Prod trong subfolder "Prod" | Một request, đổi Environment |
| URL | `{{nhsv-uat}}` / `{{nhsv-prod}}` | `{{baseUrl}}` |
| Token | `{{accessToken}}` (UAT), `{{accessToken-Prod}}` (Prod) | `{{accessToken}}` (một biến, set bởi Login) |

Collection cũ giữ nguyên; dùng **TradeX API v2** khi muốn workflow Environment.
