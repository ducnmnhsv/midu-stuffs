# Asset - Use Cases & Testing

**Status:** TBD  
**Last Updated:** February 13, 2026

---

## Overview

Các use case và kịch bản test cho module Asset.

**Test scenarios (Open Position List):** Xem [Open_Position_List_API_Spec.md §7 Testing Scenarios](../Specifications/Open_Position_List_API_Spec.md#7-testing-scenarios)

- TC-1: Query first page
- TC-2: Query next page (pagination)
- TC-3: Empty result
- TC-4: Missing accountNumber → 400
- TC-5: Unauthorized account → 403
- TC-6: Lotte error pass-through → 422
