package com.difisoft.nhsv.admin.domain;

import java.time.ZonedDateTime;

public interface IMarketHistoryJobResultStockEvent {
    Long getId();
    String getSymbols();
    Long getUserId();
    Boolean getIsSuccess();
    ZonedDateTime getTimeStart();
    ZonedDateTime getTimeEnd();
    String getError();
    String getEventId();
    String getEventType();
    String getEventName();
}
