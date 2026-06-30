package com.difisoft.nhsv.admin.service.vietstock.context;

import com.difisoft.nhsv.admin.domain.StockEvent;
import com.difisoft.nhsv.admin.domain.enumeration.VietStockEventType;
import com.difisoft.nhsv.admin.service.vietstock.response.IVietStockEventDto;
import com.difisoft.nhsv.admin.service.vietstock.resquest.VietStockEventQueryRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VietStockEventDataContext {
    private ZonedDateTime fromDate = ZonedDateTime.now();
    private ZonedDateTime toDate;

    // Event type
    private VietStockEventType eventType;

    // Request
    private VietStockEventQueryRequest request;

    // List of dto get from VietStock API
    List<IVietStockEventDto> dtos;

    // List of events converted from responses
    List<StockEvent> events;
}
