package com.difisoft.nhsv.admin.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentPorfolioRequest {
    private Long marketLeaderId;
    private Integer pageNumber = 0;
    private Integer pageSize = 20;

    public String objToString() {
        return MessageFormat.format("{0}_{1}_{2}", marketLeaderId, pageNumber, pageSize);
    }
}
