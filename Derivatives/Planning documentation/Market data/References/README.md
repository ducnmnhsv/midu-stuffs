# Market – References

Links và tài liệu tham chiếu cho FE khi implement issue thuộc module Market.

---

## Planning & Data Source

| Tài liệu | Mô tả |
|----------|--------|
| [Market/Planning/01_Integration_Plan](../Planning/01_Integration_Plan.md) | Init job, symbol_static.json, danh sách mã Derivatives (`t`, `m`) |
| [Market/README](../README.md) | Tổng quan Market Data, data flow, SymbolInfo |

---

## Figma

> **Ghi chú:** Dùng Figma MCP để review node và map với feature khi tạo issue mới. Tên màn = feature, không dùng node ID trong tên file issue.

| Feature / Màn hình | Link |
|------------------|------|
| Home | [node-id=40004829-276489](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-276489) |
| Market | [node-id=40004829-277238](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-277238) |
| Current price – Quote | [node-id=40004829-278373](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-278373) |
| Current price – Matched | [node-id=40004829-287260](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-287260) |
| Current price – Chart | [node-id=40004829-287157](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40004829-287157) – 1m, 5m, 15m, 30m |
| Derivatives – Bảng giá ngang (default) | [node-id=40005162-207007](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-207007) |
| Derivatives – Danh sách option (click) | [node-id=40005162-208618](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-208618) |
| Derivatives – Error loading | [node-id=40005162-206999](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005162-206999) |
| Derivatives – Phái sinh (tổng quan) | [node-id=40005821-302112](https://www.figma.com/design/7KYJfVHawWie4n8v12JtXm/NHSV-Pro?node-id=40005821-302112&t=7ET4YMgEP2r0vrEW-11) |

---

## FE Repo (read-only)

- **Path:** `nhsv-mts-rn`
- **Screens:** `src/screens/SearchScreen/`, `src/screens/SearchSymbolScreen/`, `src/screens/CurrentPriceScreen/`
