package com.difisoft.nhsv.admin.service.impl;

import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.config.AppConf;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderDetailsCustomRepository;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderDetailsCustomService;
import com.difisoft.nhsv.admin.service.RedisDaoExtend;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyMarketLeaderDetailsMapper;
import com.difisoft.nhsv.admin.service.mapper.CopyMarketLeaderDetailsMapperImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service("copyMarketLeaderDetailsCustomService")
@Transactional
@Slf4j
@Primary
public class CopyMarketLeaderDetailsCustomServiceImpl extends CopyMarketLeaderDetailsServiceImpl implements CopyMarketLeaderDetailsCustomService {

    private final CopyMarketLeaderDetailsCustomRepository copyMarketLeaderDetailsCustomRepository;
    private final CopyMarketLeaderDetailsMapperImpl copyMarketLeaderDetailsMapperImpl;
    private final ApplicationProperties appConf;
    private final RedisDaoExtend redisDaoExtend;
    private final AppConf propConf;

    @Autowired
    public CopyMarketLeaderDetailsCustomServiceImpl(
        CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository
        , CopyMarketLeaderDetailsMapper copyMarketLeaderDetailsMapper
        , CopyMarketLeaderDetailsCustomRepository copyMarketLeaderDetailsCustomRepository
        , CopyMarketLeaderDetailsMapperImpl copyMarketLeaderDetailsMapperImpl
        , ApplicationProperties appConf
        , RedisDaoExtend redisDaoExtend
        , AppConf propConf
    ) {
        super(copyMarketLeaderDetailsRepository, copyMarketLeaderDetailsMapper);
        this.copyMarketLeaderDetailsCustomRepository = copyMarketLeaderDetailsCustomRepository;
        this.copyMarketLeaderDetailsMapperImpl = copyMarketLeaderDetailsMapperImpl;
        this.appConf = appConf;
        this.redisDaoExtend = redisDaoExtend;
        this.propConf = propConf;
    }

    @Override
    @Scheduled(cron = "${app.cron.daily-total-subscribers}")
    public void totalSubscribersJob() {
        try {
            // Clear cache
            redisDaoExtend
                .keys(propConf.getRedis().getKeyPattern().getCacheSubscriberGrowthRate())
                .forEach(redisDaoExtend::deleteAKey);

            this.copyMarketLeaderDetailsCustomRepository.deleteAllTotalSubscriberData(
                LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyyMMdd))
                , Constants.CopyMarketLeaderDetailConstants.TYPE_COPY_TRADING
                , Constants.CopyMarketLeaderDetailConstants.LABEL_MARKET_LEADER_SUMMARY_INFO
                , Constants.CopyMarketLeaderDetailConstants.KEY_TOTAL_SUB
            );
            this.copyMarketLeaderDetailsCustomRepository.saveTotalSubscriberForAllMarketLeader();
        } catch (Exception e) {
            log.error("Job daily-total-subscribers failed", e);
        }
    }

    @Override
    public Page<CopyMarketLeaderDetails> findAllByMlIdsAndDateRangeAndConditions(List<Long> mlUserIds, ZonedDateTime fromDate, ZonedDateTime toDate, String type, String label, String key, Pageable pageable) {
        return copyMarketLeaderDetailsCustomRepository.findAllByMlIdsAndDateRangeAndConditions(mlUserIds, fromDate, toDate, type, label, key, pageable);
    }

    @Override
    public List<CopyMarketLeaderDetailsDTO> findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(List<Long> mlUserIds, String type, String label, String key, Sort sort) {
        List<CopyMarketLeaderDetails> copyMarketLeaderDetails = copyMarketLeaderDetailsCustomRepository.findAllLatestSummaryInfoByMlUserAndTypeAndLabelAndKey(mlUserIds, type, label, key, sort);
        log.info("[findAllByMlIds] copyMarketLeaderDetails: {}", copyMarketLeaderDetails);
        return copyMarketLeaderDetailsMapperImpl.toDto(copyMarketLeaderDetails);
    }
}
