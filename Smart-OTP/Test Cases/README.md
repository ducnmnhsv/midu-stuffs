# SmartOTP Test Cases

## Mục Đích

Bộ test case dành cho phòng ban nghiệp vụ/non-tech để kiểm thử chức năng SmartOTP trên app NHSV Pro và WTS/HTS.

## Cách Dùng Trong Google Sheets

1. Open one `.tsv` file.
2. Select all content.
3. Copy and paste into Google Sheets.
4. Google Sheets should split content into columns automatically because files are tab-separated.
5. Some cells contain multiple lines for easier test execution. Keep line breaks inside the same cell.

## Sheets

| Sheet | File | Purpose |
| --- | --- | --- |
| Sheet 1 | `Sheet_1_Kich_Hoat_SmartOTP.tsv` | Kích hoạt SmartOTP |
| Sheet 2 | `Sheet_2_Lay_Ma_SmartOTP.tsv` | Lấy mã SmartOTP |
| Sheet 3 | `Sheet_3_Doi_PIN_SmartOTP.tsv` | Đổi PIN SmartOTP |
| Sheet 4 | `Sheet_4_Reset_PIN_SmartOTP.tsv` | Reset PIN SmartOTP |
| Sheet 5 | `Sheet_5_Kich_Hoat_Lai_SmartOTP.tsv` | Kích hoạt lại SmartOTP |
| Sheet 6 | `Sheet_6_WTS_HTS_Verify_SmartOTP.tsv` | WTS/HTS verify SmartOTP |

## Columns

- Test case no
- Test case name
- Description
- Preconditions
- Test steps
- Expected results
- Actual results
- Status
