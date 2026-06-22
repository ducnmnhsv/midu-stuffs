# Service Agreement (Hợp đồng/Điều khoản dịch vụ)

> **Điều 5, Khoản 4** — Cung cấp dịch vụ phải thể hiện bằng hợp đồng hoặc điều khoản của hợp đồng với khách hàng
> **Priority:** 🟡 P1

## Yêu cầu TT134

Khoản 4 Điều 5 yêu cầu hợp đồng/điều khoản dịch vụ với khách hàng phải bao gồm:

| # | Yêu cầu | Mô tả |
|---|---------|-------|
| 1 | **Phương thức giao dịch trực tuyến & loại GD tương ứng** | Hệ thống phải cung cấp và hiển thị danh sách các phương thức giao dịch trực tuyến mà KH có thể sử dụng, kèm loại giao dịch tương ứng |
| 2 | **Số điện thoại đăng ký + xác minh** | SĐT di động đăng ký sử dụng dịch vụ phải được xác minh thuộc quyền sử dụng hợp pháp của KH |
| 3 | **Rủi ro & trách nhiệm bồi thường** | Các rủi ro và trách nhiệm bồi thường của mỗi bên phải được nêu rõ |

## Current Gap

Hiện tại hệ thống chưa có:

- **Phone verification service**: Chưa có cơ chế xác minh quyền sở hữu SIM. KHÔNG dùng internal cross-check (tự xác minh) — yêu cầu xác minh từ bên thứ ba (carrier network hoặc C06)
- **Carrier API integration**: Chưa tích hợp GSMA Open Gateway / CAMARA APIs từ Viettel, VinaPhone, MobiFone
- **VNeID/RAR integration**: Chưa tích hợp RAR Center (C06 Bộ Công an) — Đề án 06
- **Terms & Conditions management**: Chưa có version control, acceptance tracking, hoặc API phục vụ hợp đồng điện tử
- **Trading methods registry**: Chưa có API trả về dynamic danh sách phương thức GD + loại GD
- **Risk disclosure system**: Chưa có cơ chế hiển thị và xác nhận rủi ro/trách nhiệm

## Verification Architecture (3-Layer)

```
Layer 1: Carrier API (Phase 1 — Primary)
├── GSMA Open Gateway CAMARA Number Verification API
│   ├── Xác minh SĐT đang active và thuộc về subscriber
│   ├── Gọi qua API gateway của từng carrier (Viettel/VinaPhone/MobiFone)
│   └── Kết quả: CONFIRMED / NOT_CONFIRMED / UNAVAILABLE
│
├── GSMA Open Gateway CAMARA SIM Swap API
│   ├── Kiểm tra SĐT có bị đổi SIM gần đây không (fraud indicator)
│   └── Kết quả: swapped (yes/no) + lastSwapDate
│
└── OTP fallback (khi carrier API unavailable)
    └── Gửi OTP + cross-check với hồ sơ nội bộ

Layer 2: VNeID / RAR Center (Phase 2 — Stronger verification)
├── App-to-app flow: NHSV App → VNeID → xác thực sinh trắc
├── RAR Center (C06 Bộ Công an) trả về identity token
│   ├── CCCD number, fullName, dob, face image
│   └── Xác thực SĐT qua National Population Database
└── Aligns with UBCK + C06 cooperation plan (Đề án 06)

Layer 3: eKYC (Fallback / Existing)
├── ekyc-admin — CCCD scan + face matching (Lotte)
├── FPT eContract — hợp đồng điện tử
└── Chỉ dùng khi Layer 1 & 2 không khả thi
```

## Phạm vi

### Phone Verification Service (Phase 1)
- **Carrier API integration**: GSMA Open Gateway CAMARA Number Verification + SIM Swap APIs
- **Multi-carrier gateway**: Abstract layer cho Viettel, VinaPhone (VNPT), MobiFone
- **OTP fallback**: Khi carrier API unavailable (network issues, foreign carriers)
- **Phone change flow**: Biometric L2 + carrier re-verification
- API quản lý SĐT (đăng ký, thay đổi, xác minh, lịch sử)

### Phone Verification Service (Phase 2)
- **VNeID/RAR Center**: App-to-app identity verification flow
- **Identity binding**: Liên kết CCCD + SĐT qua National Population Database
- **Biometric verification**: Face matching qua VNeID

### Terms & Conditions Management
- Versioning (tạo, update, retire terms)
- API lấy terms theo phiên bản (latest / specific version)
- API customer acceptance (đồng ý / từ chối)
- Tracking lịch sử acceptance (who, when, which version)
- Force accept flow cho terms bắt buộc

### Trading Methods & Risk Disclosure
- API trả về danh sách phương thức GD online + loại GD tương ứng
- API trả về nội dung rủi ro & trách nhiệm bồi thường
- Admin config UI cho trading methods mapping
- Hiển thị trong flow đăng ký/contract signing

## Research References

### GSMA Open Gateway / CAMARA (Carrier APIs)
- **April 2025**: Viettel, VinaPhone (VNPT), MobiFone signed MoU for GSMA Open Gateway adoption
- **APIs available**: Number Verification (SIM ownership), SIM Swap (recent swap check), Device Location (future)
- **Standard**: CAMARA Project — Linux Foundation / GSMA, RESTful APIs, OAuth2
- **Use case**: Verify phone number belongs to the subscriber requesting service

### Circular 08/2026/TT-BKHCN
- **Effective**: April 15, 2026
- **Mandate**: Carriers must verify SIM with 4 fields (ID number, full name, DOB, face) against National Population Database
- **Impact**: Carrier API verification becomes legally mandated, not optional
- **Timeline**: Aligns with Phase 1 deployment window

### VNeID / RAR Center (C06 Bộ Công an)
- **RAR Center**: Trung tâm Dữ liệu quốc gia về dân cư (C06) — National Population Database
- **Integration model**: App-to-app — NHSV App triggers VNeID app, user authenticates, VNeID returns identity data
- **Banks already integrated**: Vietcombank, TPBank, BIDV, NCB, HDBank, BVBank, PVcomBank, Nam A Bank
- **VNeID level 2**: Xác thực qua CCCD gắn chip + face matching
- **API Portal**: NCB published integration docs showing VNeID use cases
- **VNPT eKYC IDCheck**: Alternative intermediary connecting to RAR-C06

### UBCK + C06 Đề án 06
- **Cooperation plan**: UBCKNN + C06 signed plan for investor identity verification via VNeID
- **Goal**: Verify investor identity electronically instead of physical paperwork
- **Alignment**: Service Agreement phone verification directly supports this initiative

## Output dự kiến

- Service Agreement spec (Markdown API spec + HTML PM-readable spec)
- Carrier API Integration spec (GSMA Open Gateway CAMARA)
- Phone Verification API endpoints (carrier + OTP fallback)
- Terms & Conditions Management API endpoints
- Trading Methods & Risk Disclosure API endpoints
- BE implementation plan (service layer + database schema + carrier gateway)

## Dependencies

- [Device Fingerprinting](../Device%20Fingerprinting/) — Để định danh thiết bị khi xác minh SĐT
- [Session Management](../Session%20Management/) — Session context cho terms acceptance flow
- [Biometric System](../Biometric%20System/) — Biometric verification cho phone change flow
- [Smart-OTP](../../Smart-OTP/) — OTP engine cho phone verification (fallback)
- **Carrier API Gateway** — Service mới: abstract layer gọi GSMA Open Gateway APIs
- **VNeID Integration** — Phase 2: app-to-app flow với RAR Center C06

## Integration Points

| Service | Integration |
|---------|-------------|
| `aaa` | User identity, JWT context, phone management |
| `rest-proxy` | API gateway cho terms & disclosure endpoints |
| `Smart-OTP` | OTP generation & verification (fallback) |
| `Carrier API Gateway` | GSMA Open Gateway CAMARA — Number Verification, SIM Swap |
| `VNeID / RAR (Phase 2)` | App-to-app identity verification via C06 |
| `eKYC Admin` | Existing eKYC flow (Layer 3 fallback) |
| `Biometric System` | L2 biometric cho phone change flow |

**Document Status:** 🆕 New
**For:** BE / FE / BA (Pháp chế)
**Next Steps:** Cập nhật API spec với carrier API flow
