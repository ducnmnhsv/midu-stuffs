package com.difisoft.marketcollector.configurations;

import com.difisoft.file.FileService;
import com.difisoft.file.FileUtils;
import com.difisoft.market.common.redis.MarketRedisDao;
import com.difisoft.market.common.repository.IndexStockListRepository;
import com.difisoft.market.common.repository.SymbolDailyRepository;
import com.difisoft.market.common.repository.SymbolInfoRepository;
import com.difisoft.market.common.repository.SymbolInfoRollerRepository;
import com.difisoft.marketcollector.services.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConf {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    public FileService fileService(AppConf appConf) {
        return FileUtils.getFileService(appConf.getMarketConf().getFileConfig());
    }

    @Bean
    public ISymbolInfoService symbolInfoService(SymbolInfoRepository symbolInfoRepo,
                                                AppConf appConf,
                                                RequestSender requestSender,
                                                DownloadSymbolListService downloadSymbolListService,
                                                DownloadInfoService downloadInfoService,
                                                CacheService cacheService,
                                                MarketRedisDao marketRedisDao,
                                                IndexStockListRepository indexStockListRepository,
                                                ObjectMapper objectMapper,
                                                SymbolInfoRollerRepository symbolInfoRollerRepository,
                                                SymbolDailyRepository symbolDailyRepository,
                                                DownloadAccountService downloadAccountService,
                                                RealTimeDataListenerService realTimeDataListenerService,
                                                FileService fileService,
                                                CoordinatorService coordinatorService,
                                                HolidayService holidayService,
                                                LotteApiService lotteApiService
    ) {
        if (appConf.isUsingApi()) {
            return new LotteApiSymbolInfoService(
                    symbolInfoRepo,
                    appConf,
                    requestSender,
                    marketRedisDao,
                    indexStockListRepository,
                    objectMapper,
                    symbolInfoRollerRepository,
                    symbolDailyRepository,
                    fileService,
                    coordinatorService,
                    holidayService,
                    lotteApiService
            );
        }
        return new HtsSymbolInfoService(
                symbolInfoRepo,
                appConf,
                requestSender,
                downloadSymbolListService,
                downloadInfoService,
                cacheService,
                marketRedisDao,
                indexStockListRepository,
                objectMapper,
                symbolInfoRollerRepository,
                symbolDailyRepository,
                downloadAccountService,
                realTimeDataListenerService,
                fileService,
                coordinatorService,
                holidayService);
    }
}
