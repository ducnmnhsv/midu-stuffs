package com.difisoft.nhsv.admin.service.vietstock.resquest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VietStockAuthData {
    private String cookies;
    private String requestToken;
}
