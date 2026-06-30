package com.difisoft.nhsv.admin.service.vietstock;

import com.difisoft.nhsv.admin.domain.enumeration.VietStockEventType;

public interface IVietStockEventStrategyFactory {
    // Get event strategy by event type
    IVietStockEventStrategy getEventStrategy(VietStockEventType eventType);
}
