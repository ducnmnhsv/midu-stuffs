package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.request.*;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossCustomRepository;
import com.difisoft.nhsv.admin.utils.DateTimeUtil;
import com.difisoft.nhsv.admin.utils.Util;
import com.difisoft.redis.RedisDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.dockerjava.api.model.GenericResource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@ActiveProfiles({"dev", "no-liquibase"})
@SpringBootTest
@Slf4j
public class CopyTradingTest {

    @Autowired
    private CopyUserService copyUserService;

    @Autowired
    private CopyMarketLeaderProfitLossCustomService copyMarketLeaderProfitLossCustomService;

    @Autowired
    private CopyMarketLeaderDetailsCustomService copyMarketLeaderDetailsCustomService;

    @Autowired
    private CopySubscriberCustomService copySubscriberCustomService;

    @Autowired
    private CopyMarketLeaderProfitLossCustomRepository copyMarketLeaderProfitLossCustomRepository;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private RedisDaoExtend redisDaoExtend;

    @Test
    public void testMarketLeaders() throws JsonProcessingException {
        MtsMarketLeadersRequest request = MtsMarketLeadersRequest.builder().pageNumber(0).pageSize(1000).build();
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyUserService.findAllMarketLeader(request, new RequestContext<>("testMarketLeaders", null)));
        System.out.println(json);
    }

    @Test
    public void testMarketLeaders_userName() throws JsonProcessingException {
        MtsMarketLeadersRequest request = MtsMarketLeadersRequest.builder().mlUsername("huong").build();
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyUserService.findAllMarketLeader(request, new RequestContext<>("testMarketLeaders", null)));
        System.out.println(json);
    }

    @Test
    public void testMarketLeaderProfile() throws JsonProcessingException {
        MarketLeaderProfileRequest request = new MarketLeaderProfileRequest(1085L);
        System.out.println(copyUserService.findMarketLeaderProfile(request, new RequestContext<>("testMarketLeaderProfile", null)));
    }

    @Test
    public void testMarketLeaderProfile_testInValidId() throws JsonProcessingException {
        MarketLeaderProfileRequest request = new MarketLeaderProfileRequest(999999L);
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyUserService.findMarketLeaderProfile(request, new RequestContext<>("testMarketLeaderProfile_testInValidId", null)));
        System.out.println(json);
    }

    @Test
    public void testMarketLeaderProfile_testRequired() throws JsonProcessingException {
        MarketLeaderProfileRequest request = new MarketLeaderProfileRequest(null);
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyUserService.findMarketLeaderProfile(request, new RequestContext<>("testMarketLeaderProfile_testRequired", null)));
        System.out.println(json);
    }

    @Test
    public void testJobProfitLoss() {
        copyMarketLeaderProfitLossCustomService.dailyProfitLossJob();
    }

    @Test
    public void testAPIDailyProfitLoss() throws JsonProcessingException {
        String longList = "1073,1051";
        MarketLeaderProfitLossRequest request = new MarketLeaderProfitLossRequest(longList, "20230830", "20230901", null, 0, 10);
        copyMarketLeaderProfitLossCustomService.findAllMarketLeaderDailyProfitLoss(request, new RequestContext<>("testMarketLeaderProfile_testRequired", null));
        System.out.println("json");
    }

    @Test
    public void test_findCurrentPortfolio() throws JsonProcessingException {
        CurrentPorfolioRequest request = new CurrentPorfolioRequest(1085L, 0, 10);
        copyUserService.findCurrentPortfolio(request, new RequestContext<>("test_findCurrentPortfolio", null));
        System.out.println("json");
    }

    @Test
    public void testRedisDao() throws JsonProcessingException {
        System.out.println(redisDao.get("expiredIn15Minutes::findAllMarketLeaderDailyProfitLoss_1073,1051_20230830_20230901_null_0_10", GenericResource.class));
    }

    @Test
    public void testAPIMTSHistory() throws JsonProcessingException {
        HistoricalPortfolioAllStocksRequest request = new HistoricalPortfolioAllStocksRequest(125L, 0, 10);
        copyUserService.findHistoricalPortfolioAllStocks(request, new RequestContext<>("testMarketLeaderProfile_testRequired", null));
    }

    @Test
    public void testFindMarketLeaderProfitLossByPeriod() throws JsonProcessingException {
        MarketLeaderPeriodProfitLossRequest request = new MarketLeaderPeriodProfitLossRequest(List.of(1051L), "1Y", false, -1, 3);
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyMarketLeaderProfitLossCustomService.findMarketLeaderProfitLossByPeriod(request, new RequestContext<>("testMarketLeaderProfile_testRequired", null)));
        System.out.println(json);
    }

    @Test
    public void testFindMarketLeaderProfitLossByPeriod_INACTIVE() throws JsonProcessingException {
        MarketLeaderPeriodProfitLossRequest request = new MarketLeaderPeriodProfitLossRequest(List.of(1051L, 1L, 1000L, 45454L), null, true, 0, 20);
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyMarketLeaderProfitLossCustomService.findMarketLeaderProfitLossByPeriod(request, new RequestContext<>("testMarketLeaderProfile_testRequired", null)));
        System.out.println(json);
    }

    @Test
    public void testFindMarketLeaderProfitLossByPeriod_INVALID() throws JsonProcessingException {
        MarketLeaderPeriodProfitLossRequest request = new MarketLeaderPeriodProfitLossRequest(null, null, true, 0, 20);
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyMarketLeaderProfitLossCustomService.findMarketLeaderProfitLossByPeriod(request, new RequestContext<>("testMarketLeaderProfile_testRequired", null)));
        System.out.println(json);
    }

    @Test
    public void testJobTotalSub() {
        copyMarketLeaderDetailsCustomService.totalSubscribersJob();
    }

    @Test
    public void test_findMarketLeaderSubscriberGrowthRate() {
//        List<Long> mlIds = Arrays.asList(1L, 2L, 1051L, 1061L, 1071L, 1072L, 1073L, 1074L, 1075L, 1076L, 1077L, 1078L, 1079L, 1080L, 1081L, 1082L, 1083L, 1084L, 1085L, 1086L, 1087L, 1088L, 1089L, 1093L, 1096L, 1097L, 1098L, 1099L, 1100L, 1108L, 1109L, 1110L, 1111L, 1112L, 1113L, 1114L);
//        for (Long id : mlIds) {
        try {
            MarketLeaderSubGrowthRateRequest request = new MarketLeaderSubGrowthRateRequest(1085L, "20220101", "20250101", false, 0, 2);
            copySubscriberCustomService
                .findMarketLeaderSubscriberGrowthRate(
                    request, new RequestContext<>("test_findMarketLeaderSubscriberGrowthRate", null)
                );
        } catch (Exception e) {
            log.error("error: ", e);
        }
//        }
    }

    @Test
    public void test_get_redisKeys() {
        log.info("{}", Util.objectToStringJsonIgnoreError(redisDaoExtend
            .keys("*EXPIRED_IN_JOB_CLEAR::findMarketLeaderSubscriberGrowthRate*")));

    }

    @Test
    public void test_delete_redisKeys() {
        redisDaoExtend
            .keys("*EXPIRED_IN_JOB_CLEAR::findMarketLeaderSubscriberGrowthRate*")
            .forEach(key -> redisDaoExtend.deleteAKey(key));

    }

    @Test
    public void test_findMarketLeaderSubscriberGrowthRate_defaultValue() throws JsonProcessingException {
        MarketLeaderSubGrowthRateRequest request = new MarketLeaderSubGrowthRateRequest(1051L, null, null, null, null, null);
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copySubscriberCustomService.findMarketLeaderSubscriberGrowthRate(request, new RequestContext<>("test_findMarketLeaderSubscriberGrowthRate", null)));
        System.out.println(json);
    }

    @Test
    public void testDateTime() {
        System.out.println(DateTimeUtil.stringToZoneDateTime("20230808", Constants.DATE_FORMAT_yyyyMMdd, Constants.DateTimeType.DATE));
    }

    @Test
    public void testRecalculateProfitLoss() throws JsonProcessingException {
        RecalculateProfitLossByPeriodRequest request = new RecalculateProfitLossByPeriodRequest(List.of(1083L), "20230816");
        ObjectWriter ow = new ObjectMapper().registerModule(new JavaTimeModule()).writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(copyMarketLeaderProfitLossCustomService.recalculateProfitLossByPeriod(request, new RequestContext<>("testRecalculateProfitLoss", null)));
        System.out.println(json);
    }
}
