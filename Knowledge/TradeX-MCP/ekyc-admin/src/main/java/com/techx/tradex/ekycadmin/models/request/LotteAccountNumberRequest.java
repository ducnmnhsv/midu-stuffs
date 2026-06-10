package com.techx.tradex.ekycadmin.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LotteAccountNumberRequest {

    private String idno;
}
