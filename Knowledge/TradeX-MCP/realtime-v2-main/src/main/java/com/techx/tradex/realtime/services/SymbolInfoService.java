package com.techx.tradex.realtime.services;

import com.difisoft.file.FileService;
import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.market.common.MarketInit;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.utils.ConvertUtils;
import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.market.model.v2.realtime.SymbolInfoUpdate;
import com.difisoft.model.notification.MethodEnum;
import com.difisoft.model.notification.OneSignalConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.realtime.configurations.AppConf;
import com.techx.tradex.realtime.constants.Constants;
import com.techx.tradex.realtime.constants.enums.IndexTypeEnum;
import com.techx.tradex.realtime.constants.enums.IndexTypeSortEnum;
import com.techx.tradex.realtime.constants.enums.KafkaDomainEnum;
import com.techx.tradex.realtime.model.dto.TopWorstReturnsDTO;
import com.techx.tradex.realtime.model.request.IndexRankRequest;
import com.techx.tradex.realtime.model.response.IndexRankResponse;
import com.techx.tradex.realtime.utils.CommonUtil;
import com.techx.tradex.realtime.utils.NumberUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SymbolInfoService {
    private static final Logger log = LoggerFactory.getLogger(SymbolInfoService.class);

    @Value("${app.notifications.stockTopWordsNumberRank}")
    private Integer stockTopWordsNumberRank;
    @Value("${app.notifications.stockTopWordsRedirectUrl}")
    private String stockTopWordsRedirectUrl;
    private final AppConf appConf;
    private final MarketRedisDao marketRedisDao;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private final FileService fileService;
    private final CommonService commonService;
    private final IndexStockService indexStockService;
    private final HolidayService holidayService;

    public SymbolInfoService(
            AppConf appConf,
            MarketRedisDao marketRedisDao,
            CacheService cacheService,
            ObjectMapper objectMapper,
            FileService fileService,
            CommonService commonService,
            IndexStockService indexStockService, HolidayService holidayService) {
        this.appConf = appConf;
        this.marketRedisDao = marketRedisDao;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
        this.fileService = fileService;
        this.commonService = commonService;
        this.indexStockService = indexStockService;
        this.holidayService = holidayService;
    }

    public void updateBySymbolInfoUpdate(SymbolInfoUpdate symbolInfoUpdate, boolean updateRedis) {
        SymbolInfo symbolInfo = cacheService.getMapSymbolInfo().computeIfAbsent(symbolInfoUpdate.getCode(), k -> new SymbolInfo());
        symbolInfo.update(symbolInfoUpdate);

        if (updateRedis) {
            marketRedisDao.setSymbolInfo(symbolInfo);
        }
    }

    public void uploadMarketStaticFile() {
        MarketInit.uploadMarketDataFile(marketRedisDao.getAllSymbolInfo(), objectMapper, fileService, appConf.getMarketConf());
    }

    public Object stockTopWorstReturnsInfoExecute(Object request, RequestContext<Object> ctx) {
        String ctxID = ctx.getId();
        log.info("[stockTopWorstReturnsInfoExecute] ctxID: {}, request: {}", ctxID, request);
        if (holidayService.isHolidayOrWeekend()) {
            log.info("========== TODAY IS HOLIDAY OR WEEKEND - END send vnIndexTopWorstReturns =======");
            return Strings.EMPTY;
        }
        try {
            SymbolInfo vnIndexInfo = marketRedisDao.getSymbolInfo(Constants.VN_INDEX);
            log.info("[stockTopWorstReturnsInfoExecute] ctxID: {}, vnIndexInfo: {}"
                    , ctxID, CommonUtil.objectToStringJsonIgnoreError(vnIndexInfo));
            if (Objects.isNull(vnIndexInfo) || Objects.isNull(vnIndexInfo.getId())) {
                return Strings.EMPTY;
            }

            IndexRankResponse topHSXResponse = this.indexStockService.getIndexRanks(
                    new IndexRankRequest(
                            IndexTypeEnum.VN.name()
                            , IndexTypeSortEnum.RATE_DESC.name()
                            , BigDecimal.ZERO.intValue()
                            , stockTopWordsNumberRank
                    )
            );
            log.info("[stockTopWorstReturnsInfoExecute] ctxID: {}. topHSXResponse: {}"
                    , ctxID, CommonUtil.objectToStringJsonIgnoreError(topHSXResponse));
            if (Objects.isNull(topHSXResponse) || CollectionUtils.isEmpty(topHSXResponse.getIndexRanks())) {
                return Strings.EMPTY;
            }

            IndexRankResponse worstHSXResponse = this.indexStockService.getIndexRanks(
                    new IndexRankRequest(
                            IndexTypeEnum.VN.name()
                            , IndexTypeSortEnum.RATE_ASC.name()
                            , BigDecimal.ZERO.intValue()
                            , stockTopWordsNumberRank
                    )
            );
            log.info("[stockTopWorstReturnsInfoExecute] ctxID: {}. worstHSXResponse: {}"
                    , ctxID, CommonUtil.objectToStringJsonIgnoreError(worstHSXResponse));
            if (Objects.isNull(worstHSXResponse) || CollectionUtils.isEmpty(worstHSXResponse.getIndexRanks())) {
                return Strings.EMPTY;
            }

            // prepare data
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("vnIndexInfo"
                    , MessageFormat.format("{0} ({1}%)", vnIndexInfo.getLast(), NumberUtils.round(2, vnIndexInfo.getRate())));
            messageData.put("hsxTopStockInfo", TopWorstReturnsDTO.makeMessageData(topHSXResponse.getIndexRanks()));
            messageData.put("hsxWorstStockInfo", TopWorstReturnsDTO.makeMessageData(worstHSXResponse.getIndexRanks()));

            OneSignalConfiguration configuration = new OneSignalConfiguration();
            configuration.setFilters(Collections.singletonList(this.commonService.setOneSignalFilter("vnindexReturns", "true")));
            commonService.sendNotification(
                    ctxID, messageData, KafkaDomainEnum.PAAVE.getKey(), MethodEnum.ONESIGNAL
                    , Constants.PAAVE_STOCK_TOP_WORST_RETURNS_NOTIFICATION_TEMPLATE_NAME
                    , configuration
                    , stockTopWordsRedirectUrl
            );
            return "SUCCESS";
        } catch (Exception e) {
            log.error("[stockTopWorstReturnsInfoExecute] ctxID: {}, error: {}"
                    , ctxID, CommonUtil.objectToStringJsonIgnoreError(e.getStackTrace()));
            return Strings.EMPTY;
        }
    }
}
