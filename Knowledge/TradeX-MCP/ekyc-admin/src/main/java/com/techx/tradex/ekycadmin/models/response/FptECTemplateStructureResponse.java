package com.techx.tradex.ekycadmin.models.response;


import com.techx.tradex.ekycadmin.models.dto.EContractField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FptECTemplateStructureResponse {
    private String templateId;
    private String alias;
    private Object syncType;
    private List<List<EContractField>> datas;

}
