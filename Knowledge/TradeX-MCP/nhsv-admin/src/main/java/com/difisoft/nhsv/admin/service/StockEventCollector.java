package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.domain.request.TriggerCrawlEventStock;

public interface StockEventCollector {
    void vietStockEventCollectorJob();

    Object triggerCrawlEventFromVietStock(TriggerCrawlEventStock request, RequestContext<TriggerCrawlEventStock> ctx);
}
