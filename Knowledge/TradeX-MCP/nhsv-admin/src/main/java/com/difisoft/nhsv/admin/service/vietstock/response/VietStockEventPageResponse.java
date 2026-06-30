package com.difisoft.nhsv.admin.service.vietstock.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VietStockEventPageResponse {

    // List of event dtos
    private List<IVietStockEventDto> list;

    // Total number of events
    private Integer total;

}
