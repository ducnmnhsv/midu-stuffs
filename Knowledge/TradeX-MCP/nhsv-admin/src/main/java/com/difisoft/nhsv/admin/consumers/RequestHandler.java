package com.difisoft.nhsv.admin.consumers;

import com.difisoft.kafka.handler.Controller;
import com.difisoft.kafka.handler.DeserializeServerRequestHandler;
import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.domain.request.*;
import com.difisoft.nhsv.admin.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RequestHandler extends DeserializeServerRequestHandler {

    @Autowired
    public RequestHandler(
            ObjectMapper objectMapper, ApplicationProperties appConf, ChatRoomServiceImp chatRoomService,
            CopyUserService copyUserService,
            CopyMarketLeaderProfitLossCustomService copyMarketLeaderProfitLossCustomService,
            CopySubscriberCustomService copySubscriberCustomService, FeedbackService feedbackService,
            CopyTradingSendOTPService copyTradingSendOTPService,
            CopyTradingOpenSubAccountService copyTradingOpenSubAccountService,
            CopyTradingSubscriberEstPortfolioValueService copyTradingSubscriberEstPortfolioValueService,
            StockEventCollector stockEventCollector) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), 5);
        Map<String, Controller> map = new HashMap<>();
        map.put("get:/api/v1/brokerChat/brokerProfile/{brokerId}", new Controller<>(GetProfileRequest.class, chatRoomService::getProfile));
        map.put("get:/api/v1/brokerChat/brokerProfile", new Controller<>(GetAllProfileRequest.class, chatRoomService::getAllProfile));
        map.put("get:/api/v1/brokerChat/chatRooms", new Controller<>(GetAllChatRoomRequest.class, chatRoomService::getAllChatRoom));
        map.put("get:/api/v1/brokerChat/chatRooms/{chatRoomId}", new Controller<>(GetChatRoomRequest.class, chatRoomService::getChatRoom));
        map.put("get:/api/v1/copyTrading/marketLeader/marketLeaderList", new Controller<>(MtsMarketLeadersRequest.class, copyUserService::findAllMarketLeader));
        map.put("get:/api/v1/copyTrading/marketLeader/profile/{marketLeaderId}", new Controller<>(MarketLeaderProfileRequest.class, copyUserService::findMarketLeaderProfile));
        map.put("get:/api/v1/copyTrading/marketLeader/dailyProfitLossRatio", new Controller<>(MarketLeaderProfitLossRequest.class, copyMarketLeaderProfitLossCustomService::findAllMarketLeaderDailyProfitLoss));
        map.put("get:/api/v1/copyTrading/marketLeader/periodicProfitLossRatio", new Controller<>(MarketLeaderPeriodProfitLossRequest.class, copyMarketLeaderProfitLossCustomService::findMarketLeaderProfitLossByPeriod));
        map.put("get:/api/v1/copyTrading/marketLeader/subscriberGrowthRate", new Controller<>(MarketLeaderSubGrowthRateRequest.class, copySubscriberCustomService::findMarketLeaderSubscriberGrowthRate));

        map.put("get:/api/v1/copyTrading/marketLeader/currentPortfolio",
                new Controller<>(CurrentPorfolioRequest.class, copyUserService::findCurrentPortfolio));
        map.put("get:/api/v1/copyTrading/marketLeader/historicalPortfolio",
                new Controller<>(HistoricalPortfolioRequest.class,
                        copyUserService::findHistoricalPortfolio));
        map.put("get:/api/v1/copyTrading/marketLeader/historicalPortfolio/allStocks",
                new Controller<>(HistoricalPortfolioAllStocksRequest.class,
                        copyUserService::findHistoricalPortfolioAllStocks));
        map.put("post:/api/v1/copyTrading/subscriber/subscribe",
                new Controller<>(SubscribeRequest.class,
                        copySubscriberCustomService::subscribe));
        map.put("delete:/api/v1/copyTrading/subscriber/subscribe",
                new Controller<>(UnSubscribeRequest.class,
                        copySubscriberCustomService::unSubscribe));
        map.put("get:/api/v1/copyTrading/subscriber/subscriberInformation",
                new Controller<>(SubscriberInformationRequest.class,
                        copySubscriberCustomService::findSubscriberInformation));
        map.put("post:/api/v1/equity/account/feedback",
                new Controller<>(FeedbackRequest.class, feedbackService::saveFeedback));
        map.put("get:/api/v1/copyTrading/marketLeader/list",
                new Controller<>(GetAllMarketLeaderRequest.class, copyMarketLeaderProfitLossCustomService::getAllMarketLeader));
        map.put("post:/api/v1/copyTrading/openSubAccount",
                new Controller<>(CopyTradingOpenSubAccountRequest.class, copyTradingOpenSubAccountService::openSubAccount));
        map.put("get:/api/v1/copyTrading/subAccountStatus",
                new Controller<>(CopyTradingCheckSubStatusRequest.class, copyTradingOpenSubAccountService::checkSubStatus));
        map.put("get:/api/v1/copyTrading/subscriber/estPortfolioValue",
            new Controller<>(CopyTradingSubscriberEstPortfolioValueRequest.class, copyTradingSubscriberEstPortfolioValueService::getEstPortfolioValue));
        map.put("post:/api/v1/copyTrading/sendOtp",
            new Controller<>(CopyTradingSendOTPRequest.class, copyTradingSendOTPService::generateOtp));
        map.put("post:/api/v1/copyTrading/verifyOTP",
            new Controller<>(CopyTradingVerifyOTPRequest.class, copyTradingSendOTPService::verifyOtp));
        map.put("job:/api/v1/crawl/event/viet-stock",
            new Controller<>(TriggerCrawlEventStock.class, stockEventCollector::triggerCrawlEventFromVietStock));
        this.setControllerMap(map);
    }
}
