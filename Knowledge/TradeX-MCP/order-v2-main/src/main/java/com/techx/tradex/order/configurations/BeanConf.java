package com.techx.tradex.order.configurations;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.order.dao.BridgeOrderDao;
import com.techx.tradex.order.dao.LotteBridgeOrderDao;
import com.techx.tradex.order.dao.TtlBridgeOrderDao;
import com.techx.tradex.order.repositories.ProfitLossOrderRepository;
import com.techx.tradex.order.repositories.StopOrderRepository;
import com.techx.tradex.order.repositories.TrailingOrderRepository;
import com.techx.tradex.order.services.RequestSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConf {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        return objectMapper;
    }

    @Bean
    public BridgeOrderDao bridgeOrderDao(
            AppConf appConf,
            RequestSender requestSender,
            ObjectMapper objectMapper,
            TrailingOrderRepository trailingOrderRepo,
            StopOrderRepository stopOrderRepository,
            ProfitLossOrderRepository plOrderRepo
    ) {
        if ("LOTTE".equalsIgnoreCase(appConf.getCore())) {
            return new LotteBridgeOrderDao(
                    appConf,
                    requestSender,
                    objectMapper,
                    trailingOrderRepo,
                    stopOrderRepository,
                    plOrderRepo
            );
        } else if ("TTL".equalsIgnoreCase(appConf.getCore())) {
            return new TtlBridgeOrderDao(
                    appConf,
                    requestSender,
                    objectMapper,
                    trailingOrderRepo,
                    stopOrderRepository,
                    plOrderRepo
            );
        }
        throw new IllegalStateException("Core is not supported: " + appConf.getCore());
    }
}
