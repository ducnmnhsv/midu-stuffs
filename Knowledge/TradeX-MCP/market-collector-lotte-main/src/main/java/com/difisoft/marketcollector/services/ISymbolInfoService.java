package com.difisoft.marketcollector.services;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.KafkaSender;

import java.util.concurrent.CompletableFuture;

public interface ISymbolInfoService extends KafkaSender {
    Object downloadSymbolFromRequest(Object ignoredRequest, RequestContext<Object> ctx);

    Object forceDownloadSymbolFromRequest(Object ignoredRequest, RequestContext<Object> ctx);

    CompletableFuture<Void> downloadSymbol(String id);
}
