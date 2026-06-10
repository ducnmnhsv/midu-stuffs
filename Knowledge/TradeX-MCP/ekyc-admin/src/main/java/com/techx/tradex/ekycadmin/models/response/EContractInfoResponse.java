package com.techx.tradex.ekycadmin.models.response;

import com.techx.tradex.ekycadmin.domain.EContract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EContractInfoResponse {
    private Long id;
    private String contractStatus;
    private String signFileContent;
    private String contractFileContent;
    private String customerSignatueStatus;
    private String securitiesSignatureStatus;
    private EContract eContract;
    private String contractFileName;
}
