# Backlog Jira — phạm vi BE (góc nhìn BA / nghiệp vụ)

**Epic gợi ý (Jira):** Mở tiểu khoản phái sinh online (NHSV Pro) — hệ thống phía server  
**PRD:** [PRD_Open_Derivatives_Sub_Account_Online_v2.md](../Planning/PRD_Open_Derivatives_Sub_Account_Online_v2.md)  
**Cách dùng:** Mỗi mục dưới đây là một **User Story / Story** cho team BE; phần *Ghi chú kỹ thuật* chỉ để dev đối chiếu nhanh — **chi tiết triển khai lấy từ PRD**, không cần nhét hết vào ticket.

**Khác với bản kỹ thuật:** ticket tập trung vào **ai làm gì, khi nào, quy tắc nghiệp vụ, kết quả cho KH & vận hành**, tránh ràng buộc tên bảng/API trừ khi đã chốt với đối tác.

---

## Quy ước đặt tên ticket (tùy team)

| Pattern | Ví dụ |
|---------|--------|
| Epic | DERIV-SUB80 |
| Story | DERIV-SUB80-BE-01 … |

---

## BE-01 — Lưu trữ hồ sơ đăng ký theo vòng đời (từ ký HĐ đến mở sub)

**Issue type:** Story  
**Summary:** [BE] Lưu trữ và theo dõi trạng thái toàn bộ quy trình mở tiểu khoản phái sinh

**Mô tả nghiệp vụ:**  
Khi khách bắt đầu đăng ký, hệ thống cần **một nơi ghi nhận duy nhất** cho mỗi lần đăng ký: trạng thái hợp đồng điện tử, kết quả làm việc với công ty chứng khoán (Lotte), trạng thái “tiểu khoản phái sinh đã hiển thị cho khách hay chưa”, lịch sử kiểm tra, thông tin phục vụ gửi thông báo đúng ngôn ngữ, và các lần thử lại khi đối tác chưa trả kết quả ngay.

**Giá trị:** CS và vận hành có thể truy vết từng hồ sơ; hệ thống không tạo trùng bản ghi cho cùng một lần đăng ký.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Mỗi lần khách (hoặc hệ thống) khởi tạo đăng ký có **mã tham chiếu** thống nhất trong suốt luồng.
- Dữ liệu lưu đủ để biết: đã ký HĐ chưa, đã gửi lệnh mở sub phía Lotte chưa, **đã xác nhận tiểu khoản phái sinh “hoàn tất” theo quy tắc PRD chưa** (bao gồm bước kiểm tra trạng thái tài khoản sau lệnh Lotte).
- Có chỗ ghi **ngôn ngữ thông báo** tại thời điểm đăng ký (để email/push không sai ngôn ngữ khi xử lý trễ).
- Không cho phép hai hồ sơ “đang active” trùng ý nghĩa nghiệp vụ theo quy tắc PRD (trùng mã tham chiếu / trùng luồng — chi tiết PRD).

**Ghi chú kỹ thuật (cho dev):** PRD mục data model / §9.5; index phục vụ các tiến trình nền theo §13.

**Phụ thuộc:** Không.

---

## BE-02 — Tiếp nhận đăng ký: kiểm tra điều kiện và cấp đường dẫn ký hợp đồng điện tử

**Issue type:** Story  
**Summary:** [BE] Khách yêu cầu mở sub phái sinh — xác thực điều kiện và trả link ký HĐ (FPT)

**Mô tả nghiệp vụ:**  
Khách bấm đăng ký trên app. **Server phải tự kiểm tra lại** các điều kiện nghiệp vụ (không chỉ tin thông tin client gửi lên). Nếu đủ điều kiện, hệ thống phối hợp FPT eContract để tạo gói ký, trả cho app **đường dẫn ký** để khách hoàn tất chữ ký điện tử. Nếu không đủ điều kiện, trả **thông báo lỗi rõ ràng** theo bảng mã lỗi thống nhất với PRD (để app hiển thị đúng cho khách).

**Giá trị:** Đảm bảo chỉ khách đủ điều kiện mới vào bước ký; giảm tranh chấp và gọi nhầm sang Lotte.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Bốn nhóm điều kiện trong PRD được áp dụng **phía server**; từng trường hợp từ chối có **mã lỗi** và (nếu PRD quy định) thông điệp thống nhất.
- Khi FPT chấp nhận tạo gói ký thành công: hệ thống **ghi nhận hồ sơ ở trạng thái “chờ ký”** và trả cho app thông tin cần để mở luồng ký (theo PRD — ví dụ URL ký).
- Lưu **ngôn ngữ** khách dùng lúc đăng ký để dùng cho bước thông báo sau này.

**Ghi chú kỹ thuật (cho dev):** PRD §7, §8, §9; mapping lỗi §8.3; chuẩn API TradeX — xem tài liệu conventions trong repo.

**Phụ thuộc:** BE-01.

---

## BE-03 — Phối hợp FPT eContract: tạo gói ký, tra cứu trạng thái, hủy khi cần

**Issue type:** Story  
**Summary:** [BE] Kết nối nền tảng ký điện tử FPT theo quy trình đối tác

**Mô tả nghiệp vụ:**  
Hệ thống cần **đăng nhập/phiên làm việc** với FPT, lấy cấu trúc hợp đồng mẫu, **tạo phiên ký (envelope)**, lấy **trạng thái người nhận / phiên ký** để biết khách đã ký hay chưa, và **hủy phiên** khi vận hành yêu cầu (theo quyền). Vận hành cần hành vi ổn định khi FPT chậm hoặc tạm lỗi (không “treo” silênt).

**Giá trị:** Luồng ký điện tử là cửa ải pháp lý; lỗi FPT phải được ghi nhận để CS/Ops xử lý.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Có thể tạo phiên ký theo **đúng template nghiệp vụ phái sinh** (field/hợp đồng theo PRD và xác nhận BA–FPT).
- Trạng thái từ FPT được **giải thích đúng** thành: chưa ký / đã ký / các trạng thái trung gian mà PRD yêu cầu.
- Có phương án **hủy phiên** phục vụ admin/Ops khi có quyền (theo PRD mục vận hành).

**Ghi chú kỹ thuật (cho dev):** Các bước FPT 3.2.x trong PRD; cache token/template nếu PRD cho phép.

**Phụ thuộc:** BE-02 (hoặc song song sau BE-01 nếu tách module).

---

## BE-04 — Theo dõi định kỳ: khi khách đã ký HĐ thì chuyển sang bước “mở sub” phía Lotte

**Issue type:** Story  
**Summary:** [BE] Tự động phát hiện HĐ đã ký và kích hoạt bước tiếp theo (không gọi Lotte trước khi ký xong)

**Mô tả nghiệp vụ:**  
Sau khi khách ký, hệ thống **không giả định** ký xong ngay lập tức. Một tiến trình nền **định kỳ** kiểm tra với FPT: hồ sơ nào đã chuyển sang **đã ký**. Chỉ khi xác nhận đã ký, mới được phép **gửi yêu cầu mở tiểu khoản phái sinh** sang Lotte. Nếu cùng một sự kiện được nhận qua nhiều kênh trong tương lai (ví dụ webhook), hệ thống vẫn chỉ **xử lý một lần** cho một mốc trạng thái (tránh mở sub hai lần).

**Giá trị:** Tuân thủ thứ tự nghiệp vụ: ký HĐ trước, mở sub sau; giảm lỗi tác động hệ thống cốt lõi.

**Tiêu chí chấp nhận (nghiệp vụ):**

- **Không** gửi yêu cầu mở sub tới Lotte khi hợp đồng chưa ở trạng thái “đã ký” theo PRD.
- Khi chuyển sang “đã ký”, hệ thống kích hoạt đúng **bước kế tiếp trong PRD** (một luồng xử lý thống nhất).
- Có giới hạn xử lý theo lô và cơ chế **tắt khẩn cấp** khi FPT/Lotte bất thường (theo PRD §13 — để vận hành an toàn).

**Ghi chú kỹ thuật (cho dev):** PRD Job A / §10, §13.1; polling-first.

**Phụ thuộc:** BE-01, BE-03.

---

## BE-05 — Gửi yêu cầu mở tiểu khoản phái sinh tới Lotte sau khi HĐ đã ký

**Issue type:** Story  
**Summary:** [BE] Sau khi ký xong HĐ — gọi Lotte (DRACC-038) để tạo tiểu khoản phái sinh

**Mô tả nghiệp vụ:**  
Khi hợp đồng đã ký, hệ thống gửi **lệnh mở tiểu khoản phái sinh** tới Lotte theo hợp đồng tích hợp. **Thành công phía Lotte ở bước này chưa đồng nghĩa** khách đã thấy tiểu khoản trên app — bước xác nhận tiếp theo (BE-06/07) quy định khi nào coi là **hoàn tất** để thông báo cho khách.

**Giá trị:** Tự động hóa mở sub, giảm nhập tay và sai sót.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Chỉ gửi lệnh khi đã thỏa điều kiện “HĐ đã ký” và **không trùng xử lý** cho cùng một hồ sơ.
- Ghi nhận **thời điểm Lotte chấp nhận lệnh thành công** (để CS đối chiếu).
- Khi Lotte từ chối/lỗi: trạng thái hồ sơ phản ánh **lỗi có thể tra cứu** (không để hồ sơ “mơ hồ” giữa các bước).

**Ghi chú kỹ thuật (cho dev):** PRD §11.1; DRACC-038; quy tắc “chưa SUCCESS cho đến khi ACC-032 Y” — §11.2.

**Phụ thuộc:** BE-04.

---

## BE-06 — Xác nhận tiểu khoản phái sinh đã “hiển thị/hoạt động” theo hệ thống Lotte (ACC-032)

**Issue type:** Story  
**Summary:** [BE] Kiểm tra trạng thái tiểu khoản phái sinh phía Lotte trước khi coi là xong cho khách

**Mô tả nghiệp vụ:**  
Sau khi Lotte xử lý lệnh mở sub, hệ thống cần **hỏi lại** Lotte: tiểu khoản phái sinh đã ở trạng thái **đã đăng ký/hoạt động** theo dữ liệu họ trả về hay chưa. PRD quy định: chỉ khi chuỗi trạng thái bắt đầu bằng **Y** (ví dụ dạng “Y.…”) thì coi là **đã hoàn tất nghiệp vụ mở sub** cho mục đích thông báo khách; nếu vẫn **N…** hoặc chưa tra được, **chưa được coi là xong** — cần tiến trình thử lại (BE-08).

**Giá trị:** Tránh báo “thành công” cho khách khi phía Lotte chưa phản ánh đủ trên hệ thống của họ.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Phân loại đúng ba nhóm: **Y** (hoàn tất theo quy tắc), **N** (chưa hoàn tất), **lỗi/ không đọc được** (chưa quyết định — chờ thử lại).
- Lưu **nguyên văn** trạng thái Lotte trả về để CS đối chiếu khi khiếu nại.
- Có **thời điểm kiểm tra** lần cuối để biết dữ liệu có cũ không.

**Ghi chú kỹ thuật (cho dev):** PRD §11.2, §18; API ACC-032.

**Phụ thuộc:** BE-05.

---

## BE-07 — Hoàn tất trạng thái hồ sơ và chuẩn bị gửi thông báo thành công (chỉ khi Lotte xác nhận Y)

**Issue type:** Story  
**Summary:** [BE] Chỉ khi Lotte xác nhận tiểu khoản phái sinh (Y) mới “đóng” hồ sơ thành công và chuyển sang gửi thông báo

**Mô tả nghiệp vụ:**  
Đây là **cổng nghiệp vụ quan trọng**: dù lệnh DRACC đã thành công, **không được** gửi push/email “mở sub thành công” nếu bước ACC-032 vẫn cho **N…** hoặc lỗi. Khi ACC-032 **Y…**, hệ thống: cập nhật hồ sơ sang trạng thái **hoàn tất**, làm mới thông tin tài khoản phía app nếu PRD yêu cầu (cache/danh sách sub), và **ghi nhận nhu cầu gửi thông báo** cho bước sau.

**Giá trị:** Thông báo đúng thời điểm nghiệp vụ; giảm khiếu nại “app báo xong mà chưa thấy sub”.

**Tiêu chí chấp nhận (nghiệp vụ):**

- **Không** phát sinh thông báo thành công chỉ dựa trên DRACC mà **thiếu** xác nhận Y từ ACC-032.
- Một hồ sơ **không** phát sinh trùng lặp thông báo thành công khi hệ thống xử lý lại (replay).

**Ghi chú kỹ thuật (cho dev):** Outbox / nhất quán giao dịch — PRD §12.4, §16.

**Phụ thuộc:** BE-06.

---

## BE-08 — Xử lý trễ và thử lại: Lotte chưa xong hoặc kiểm tra trạng thái lỗi (chu kỳ 5 phút)

**Issue type:** Story  
**Summary:** [BE] Định kỳ thử lại lệnh Lotte và kiểm tra lại ACC-032 cho hồ sơ chưa “hoàn tất”

**Mô tả nghiệp vụ:**  
Thực tế Lotte có thể **chậm cập nhật** trạng thái tiểu khoản. Hệ thống cần tiến trình nền **mỗi 5 phút** (theo PRD) để: thử lại lệnh mở sub nếu trước đó lỗi; **hỏi lại** ACC-032 nếu DRACC đã thành nhưng trạng thái vẫn N… hoặc lần kiểm tra trước lỗi. Khi vượt ngưỡng thử lại, chuyển hồ sơ sang trạng thái **cần can thiệp** và **cảnh báo** Ops (theo PRD).

**Giá trị:** Giảm case “treo” vô hạn; có điểm dừng để con người xử lý.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Đúng **chu kỳ 5 phút** và **điều kiện chọn hồ sơ** như PRD (không quét bừa toàn bộ lịch sử).
- Có **giới hạn số lần thử** và hành vi khi đạt trần (CS/Ops được báo).
- Không gây **quá tải** đối tác (khoảng cách giữa các lần gọi theo PRD).

**Ghi chú kỹ thuật (cho dev):** PRD Job B / §13.2, §19.

**Phụ thuộc:** BE-05, BE-06, BE-07.

---

## BE-09 — Gửi thông báo thành công (push + email) đúng ngôn ngữ, một lần cho mỗi hồ sơ

**Issue type:** Story  
**Summary:** [BE] Gửi thông báo “mở sub thành công” sau khi hồ sơ đã hoàn tất theo quy tắc nghiệp vụ

**Mô tả nghiệp vụ:**  
Khi hồ sơ đã **hoàn tất** (theo BE-07), hệ thống gửi **push** và **email** cho khách nội dung theo PRD. Ngôn ngữ lấy từ **ngôn ngữ đã lưu lúc đăng ký**. Mỗi hồ sơ chỉ nhận **một bộ thông báo thành công** (trừ khi vận hành chủ động gửi lại có kiểm soát — Resend trong PRD).

**Giá trị:** Trải nghiệm khách nhất quán; giảm spam và khiếu nại trùng thông báo.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Push và email dùng **biến nội dung** đúng PRD (tên, số tài khoản, thời điểm, hotline/email hỗ trợ).
- Nếu email lỗi: **không** làm sai trạng thái “đã mở sub thành công” của hồ sơ; có dấu vết để CS biết và **gửi lại** khi được phép.

**Ghi chú kỹ thuật (cho dev):** OneSignal / email provider — §12; dedup theo PRD.

**Phụ thuộc:** BE-07.

---

## BE-10 — Lưu bản PDF hợp đồng đã ký để tra cứu và phục vụ CS

**Issue type:** Story  
**Summary:** [BE] Tải và lưu trữ bản PDF hợp đồng đã ký (sau khi có đường lấy file từ FPT)

**Mô tả nghiệp vụ:**  
Sau khi ký, cần có **bản PDF** lưu trong hệ thống NHSV để admin/CS xem lại khi cần (khiếu nại, kiểm tra). Tiến trình nền **không được chặn** bước mở sub; nếu lưu file lỗi thì ghi trạng thái để thử lại.

**Giá trị:** Minh chứng pháp lý và phục vụ CS; không phụ thuộc FPT mãi mãi để xem lại.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Khi lưu thành công: hồ sơ hiển thị **đã có file** cho admin theo quy tắc PRD.
- Admin chỉ xem được file khi **được phép** (theo PRD §15 — không lộ đường dẫn thô).

**Ghi chú kỹ thuật (cho dev):** PRD §14, §13.3; lưu object storage.

**Phụ thuộc:** BE-03, BE-04.

---

## BE-11 — Màn hình vận hành: tra cứu hồ sơ, xem PDF, thao tác hỗ trợ (Retry / Gửi lại thông báo)

**Issue type:** Story  
**Summary:** [BE] API phục vụ admin — danh sách, lọc, chi tiết timeline, xem PDF tạm thời, thao tác Ops

**Mô tả nghiệp vụ:**  
Ops/CS cần **tìm hồ sơ** theo thời gian và trạng thái nghiệp vụ (ký HĐ / Lotte / hoàn tất / lỗi), xem **diễn biến** và **lỗi** từ từng bước, **mở PDF** trong thời gian giới hạn, và **thử lại** bước Lotte hoặc **gửi lại thông báo** khi có quyền — kèm **nhật ký thao tác** để kiểm soát nội bộ.

**Giá trị:** Giảm thời gian xử lý khiếu nại; phân quyền rõ ràng.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Danh sách mặc định **một tháng gần nhất**; lọc theo các trạng thái **thống nhất với PRD** (mapping hiển thị contract + Lotte).
- Chi tiết hiển thị **đủ bước** để CS giải thích cho khách (ký chưa, Lotte lỗi gì, ACC đang N hay Y).
- Chỉ role được phép mới thấy nút **Retry / Resend**; mọi thao tác **ghi audit**.

**Ghi chú kỹ thuật (cho dev):** PRD §15; RBAC; link xem file có thời hạn.

**Phụ thuộc:** BE-01, BE-10.

---

## BE-12 — Giám sát vận hành, cấu hình chu kỳ, và an toàn thông tin đối tác

**Issue type:** Task  
**Summary:** [BE] Đo lường, cảnh báo, tắt khẩn cấp luồng — phục vụ vận hành

**Mô tả nghiệp vụ:**  
Vận hành cần **nhìn thấy** tiến trình có khỏe không: tiến trình kiểm tra ký, tiến trình Lotte/ACC, lỗi đăng nhập FPT; và có nút **tạm dừng** khi đối tác sự cố. Thông tin đăng nhập đối tác **không** để lộ trong ticket/chat — quản lý theo chuẩn bảo mật công ty.

**Giá trị:** Phát hiện sớm sự cố; giảm rủi ro rò rỉ credential.

**Tiêu chí chấp nhận (nghiệp vụ):**

- Có **chỉ số hoặc báo cáo** tối thiểu theo PRD §17 (tỷ lệ thành công tiến trình, độ trễ gọi Lotte/ACC, chuỗi lỗi FPT).
- Có **ngưỡng cảnh báo** cấu hình được (theo PRD).
- Có cơ chế **tắt/bật** luồng tự động khi cần (theo PRD).

**Ghi chú kỹ thuật (cho dev):** PRD §17–§18; Vault/secret; log có correlation, ẩn PII.

**Phụ thuộc:** BE-04, BE-08.

---

## Thứ tự triển khai gợi ý (theo giá trị nghiệp vụ)

| Giai đoạn | Stories | Ý nghĩa nghiệp vụ |
|-----------|---------|-------------------|
| 1 | BE-01 → BE-02 → BE-03 | Có hồ sơ, khách bắt đầu được luồng ký |
| 2 | BE-04 → BE-05 → BE-06 → BE-07 | Tự động mở sub + xác nhận đúng cổng “thành công” |
| 3 | BE-08 → BE-09 | Xử lý trễ đối tác + thông báo đúng lúc |
| 4 | BE-10 → BE-11 | Minh chứng PDF + công cụ CS/Ops |
| 5 | BE-12 (song song từ giai đoạn 2) | Vận hành ổn định |

---

**Document status:** Bản BA — sẵn sàng import Jira (Summary / Description / AC)  
**Next step:** BA rà soát wording với SME; Tech Lead bổ sung estimate và mapping component khi grooming.
