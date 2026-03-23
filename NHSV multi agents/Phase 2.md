## 1. Bối cảnh & Mục tiêu

Sau khi MVP hoàn thành và được vận hành thành công với bộ 6 skill cốt lõi (Jira, Confluence, log monitoring, code search, memory, onboarding), hệ thống đã chứng minh giá trị. Tuy nhiên, vẫn tồn tại các rủi ro:

* **Agent có quyền truy cập trực tiếp vào hệ thống** (đọc file, chạy lệnh) mà chưa có lớp kiểm soát.
* **Chưa có khả năng phát hiện hành vi bất thường** hay ghi nhận đầy đủ lịch sử hoạt động để phục vụ compliance.
* **Các skill và lõi agent vẫn chạy với đặc quyền cao**, tiềm ẩn nguy cơ nếu skill bị lợi dụng.

**Mục tiêu của Phase 2**:

* **Giảm thiểu rủi ro bảo mật** bằng cách đưa AI Agent vào môi trường sandbox với các guardrails rõ ràng.
* **Xây dựng audit trail** để ghi nhận toàn bộ hành động của agent phục vụ điều tra và tuân thủ.
* **Củng cố độ ổn định** cho hệ thống, chuẩn bị cho mở rộng quy mô sử dụng.

## 2. Phạm vi Phase 2

* **NemoClaw** làm lớp bảo vệ cho OpenFang (sandbox, guardrails, OpenShell).
* **Basic audit trail** – ghi log mọi hành động của agent vào cơ sở dữ liệu (SQLite hoặc PostgreSQL).
* **Nâng cấp skill execution** – tất cả skill được thực thi qua NemoClaw thay vì trực tiếp.
* **Giám sát & cảnh báo** khi có vi phạm guardrail.
* **Tài liệu vận hành** cho quản trị viên.

## 3. Kiến trúc nâng cấp

```txt
flowchart TB
    subgraph INTERNAL_NETWORK [Internal Network - Air-gapped]
        subgraph SECURE_ZONE [Sandbox do NemoClaw quản lý]
            A[OpenFang Agent]
            B[Skills]
        end
        C[NemoClaw Security Layer]
        D[Audit Database]
        E[Internal Systems<br/>Logs, Configs, Code Repos]
    end

    F[Teams / Bridge Server] --> A
    A --> C
    C --> E
    C --> D
    C -- "Chặn nếu vi phạm guardrail" --> G[Alert]
```

### 3.1 Vai trò của NemoClaw

* **Sandbox**: OpenFang và các skill chạy trong môi trường cách ly, không thể truy cập trực tiếp vào hệ thống.
* **OpenShell**: Cửa sổ giao dịch duy nhất – mọi yêu cầu đọc file, chạy lệnh, gọi API đều phải qua OpenShell.
* **Guardrails**: Tập luật do quản trị viên định nghĩa, xác định những hành động được phép. Ví dụ:
  * Được phép đọc file trong `/var/log/myapp/`, `/repos/*`
  * Được phép chạy lệnh `curl` đến các health endpoint nội bộ
  * **Không** được phép thực thi lệnh `rm`, `sudo`, `chmod`
  * **Không** được phép truy cập `/etc/shadow`, `/root/`
* **Audit tích hợp**: Mọi hành động (dù thành công hay bị từ chối) đều được NemoClaw ghi lại và gửi đến Audit Database.

### 3.2 Audit Trail

* **Lưu trữ**: PostgreSQL (hoặc SQLite nâng cấp) trong internal network.
* **Nội dung mỗi bản ghi**:
  * Timestamp
  * User ID (Teams user) – nếu có
  * Skill được gọi
  * Hành động (đọc file, chạy lệnh, gọi API)
  * Đường dẫn / command / payload
  * Kết quả (thành công / bị chặn)
  * Guardrail trigger (nếu bị chặn)
* **API truy vấn audit**: Dành cho quản trị viên để tra cứu lịch sử (không public ra Teams).

### 3.3 Luồng xử lý mới

1. User chat qua Teams → OpenFang nhận.
2. OpenFang xác định skill cần chạy.
3. Skill đưa ra yêu cầu (ví dụ: đọc file `/var/log/payment/error.log`).
4. Yêu cầu được gửi đến **OpenShell** của NemoClaw.
5. NemoClaw kiểm tra guardrail:
   * Nếu được phép → thực thi, trả kết quả cho skill.
   * Nếu không được phép → từ chối, ghi audit, có thể gửi cảnh báo riêng.
6. Skill xử lý kết quả, trả về OpenFang.
7. OpenFang gửi phản hồi qua Teams.

## 4. Use Cases bổ sung

### UC‑B01: Guardrail Violation Alert

**Mô tả**: Khi một skill cố gắng thực hiện hành động bị cấm, NemoClaw ghi nhận và gửi cảnh báo đến kênh admin (có thể là Teams channel riêng).

**Ví dụ**:

* Một skill code explorer vô tình yêu cầu đọc `/etc/passwd` → bị chặn.
* Admin nhận alert:

```txt
🚨 Guardrail Violation: Skill "code_search" attempted to read /etc/passwd. Denied. Timestamp: ...
```

### UC‑B02: Audit Trail Truy Vấn

**Mô tả**: Quản trị viên có thể hỏi agent về lịch sử hoạt động. (Tùy chọn, có thể implement ở phase 2 hoặc để phase sau)

**Ví dụ**:

```
Admin: "Xem audit của skill log_scanner trong 24h qua"
Agent: "Có 47 lần thực thi, trong đó 3 lần bị chặn do guardrail. Chi tiết: ..."
```
## 5. Kế hoạch triển khai Phase 2 (3 tuần)
### Tuần 1: Cài đặt & Tích hợp NemoClaw
- Cài đặt NemoClaw trên Ops Server.
- Viết guardrails cơ bản cho các skill hiện có.
- Sửa lại các skill để gọi OpenShell thay vì trực tiếp.
- Chạy thử nghiệm với các skill quan trọng (Jira, log scanner).
### Tuần 2: Audit Trail & Giám sát
- Thiết lập PostgreSQL (hoặc nâng cấp SQLite) để lưu audit logs.
- Cấu hình NemoClaw ghi audit vào database.
- Xây dựng giao diện (hoặc API) để quản trị viên tra cứu audit.
- Thiết lập alert channel cho guardrail violations.
### Tuần 3: Kiểm thử & Chuyển giao
- Đánh giá hiệu năng (độ trễ tăng do thêm lớp bảo vệ).
- Cập nhật tài liệu vận hành cho admin.