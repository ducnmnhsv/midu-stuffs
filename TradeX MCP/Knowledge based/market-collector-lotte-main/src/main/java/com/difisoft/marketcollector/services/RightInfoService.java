package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.message.receive.RightInfoRcv;
import com.difisoft.htsconnection.socket.message.send.RightInfoSnd;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.model.utils.CompletablePool;
import com.difisoft.redis.RedisDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class RightInfoService {
    private static final String REDIS_KEY = "market_right_info_";
    private final DownloadAccountService downloadAccountService;
    private final RedisDao redisDao;
    private final CacheService cacheService;

    public RightInfoService(DownloadAccountService downloadAccountService, RedisDao redisDao, CacheService cacheService) {
        this.downloadAccountService = downloadAccountService;
        this.redisDao = redisDao;
        this.cacheService = cacheService;
    }

    public void getAndSaveRightInfo() {
        Set<String> stockCodes = cacheService.getMapSymbolInfo().keySet();
        CompletablePool<Object> pool = new CompletablePool<>(
                stockCodes.stream().map(stockCode -> () -> downloadRightInfo(stockCode)),
                100
        );
        pool.executeInPoolChain();
    }

    public CompletableFuture<Object> downloadRightInfo(String stockCode) {
        DownloadAccountService.ConnectionController connectionController = downloadAccountService.getConnection();
        String key = REDIS_KEY + stockCode;
        RightInfoSnd req = new RightInfoSnd();
        req.getStockCode().setValue(stockCode);
        return connectionController.getConnectionHandler()
                .sendMessageFuture(req, RightInfoRcv.class, 10000L)
                .thenApply(r -> {
                    WithCon wc = new WithCon();
                    wc.setBaseDate(r.getWithconBaseDate().getValue());
                    wc.setBaseRate(r.getWithconBaseRate().getValue());
                    wc.setDividRate(r.getWithconDividendRate().getValue());
                    wc.setIssuePrice(r.getWithconIissueprice().getValue());
                    wc.setApplyPeriod(r.getWithconApplyperiod().getValue());
                    wc.setTransferPeriod(r.getWithconTransferperiod().getValue());
                    wc.setRcpDate(r.getWithconRcpdate().getValue());
                    WithoutCon woc = new WithoutCon();
                    woc.setBaseDate(r.getWithoutconBaseDate().getValue());
                    woc.setBaseRate(r.getWithoutconBaseRate().getValue());
                    woc.setDividRate(r.getWithoutconDividendRate().getValue());
                    woc.setFrcStkPrice(r.getWithoutconFrcstkprice().getValue());
                    woc.setFrcPayDate(r.getWithoutconFrcpaydate().getValue());
                    woc.setRcpDate(r.getWithoutconRcpdate().getValue());
                    Dividend d = new Dividend();
                    d.setBaseDate(r.getDividendBaseDate().getValue());
                    d.setBaseRate(r.getDividendBaseRate().getValue());
                    d.setStkDividRate(r.getDividendStkdividrate().getValue());
                    d.setCashDividRate(r.getDividendCashdividrate().getValue());
                    d.setFrcStkPrice(r.getDividendFrcstkprice().getValue());
                    d.setFrcPayDate(r.getDividendFrcpaydate().getValue());
                    d.setCashPayDate(r.getDividendCashpaydate().getValue());
                    d.setRcpDate(r.getDividendRcpdate().getValue());
                    RightInfo rightInfo = new RightInfo(wc, woc, d, r.getMeetdate().getValue());
                    try {
                        redisDao.set(key, rightInfo, 7200000);
                        log.info("save RightInfo: {} {}", key, rightInfo);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    connectionController.release();
                    return "";
                });
    }

    @Data
    public static class RightInfo {
        private WithCon withcon;
        private WithoutCon withoutcon;
        private Dividend dividend;
        private String metaDate;

        public RightInfo(WithCon withcon, WithoutCon withoutcon, Dividend dividend, String metaDate) {
            this.withcon = withcon;
            this.withoutcon = withoutcon;
            this.dividend = dividend;
            this.metaDate = metaDate;
        }
    }

    @Data
    public static class WithCon {
        private String baseDate;
        private String baseRate;
        private String dividRate;
        private Integer issuePrice;
        private String applyPeriod;
        private String transferPeriod;
        private String rcpDate;
    }

    @Data
    public static class WithoutCon {
        private String baseDate;
        private String baseRate;
        private String dividRate;
        private Integer frcStkPrice;
        private String frcPayDate;
        private String rcpDate;
    }

    @Data
    public static class Dividend {
        private String baseDate;
        private String baseRate;
        private String stkDividRate;
        private String cashDividRate;
        private Integer frcStkPrice;
        private String frcPayDate;
        private String cashPayDate;
        private String rcpDate;
    }


}
