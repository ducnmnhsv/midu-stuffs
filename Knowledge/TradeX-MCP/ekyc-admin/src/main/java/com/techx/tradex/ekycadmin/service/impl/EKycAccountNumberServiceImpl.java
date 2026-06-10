package com.techx.tradex.ekycadmin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.dao.RedisDao;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import com.techx.tradex.ekycadmin.models.request.LotteAccountNumberRequest;
import com.techx.tradex.ekycadmin.models.response.LotteAccountNumberResponse;
import com.techx.tradex.ekycadmin.repository.EKycRepository;
import com.techx.tradex.ekycadmin.service.CommonService;
import com.techx.tradex.ekycadmin.service.EContractCustomService;
import com.techx.tradex.ekycadmin.service.EKycAccountNumberService;
import com.techx.tradex.ekycadmin.utils.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Service
@RequiredArgsConstructor
public class EKycAccountNumberServiceImpl implements EKycAccountNumberService {

    private static final Logger log = LogManager.getLogger(EKycAccountNumberServiceImpl.class);

    private final AppConf appConf;
    private final EContractCustomService eContractCustomService;
    private final EKycRepository eKycRepository;
    private final LotteApiClientImpl lotteApiClientImpl;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisDao redisDao;
    private final TransactionTemplate transactionTemplate;
    private final CommonService commonService;
    private final PlatformTransactionManager platformTransactionManager;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Override
    @Async("threadPoolTaskExecutor")
    public void updateAccountNumberInfo(Long eKycId, String ctxId) {
        triggerUpdateAccountNumber(eKycId, ctxId);
    }

    private void triggerUpdateAccountNumber(Long eKycId, String ctxId) {
        if (Objects.isNull(eKycId)) {
            log.error("{} -- {}", ctxId, Constants.E_KYC_ID_IS_NOT_BE_EMPTY);
            throw new GeneralException(Constants.E_KYC_ID_IS_NOT_BE_EMPTY);
        }
        try {
            EKyc eKyc =
                    this.eKycRepository.findById(eKycId)
                            .orElseThrow(() -> new GeneralException(MessageFormat.format(Constants.E_KYC_INFO_IS_NOT_FOUND, eKycId)));
            String prefixLog = String.format(
                    "triggerUpdateAccountNumber -- ctxId : %s, eKycId: %s, core eKycId: %s",
                    ctxId,
                    eKycId,
                    eKyc.geteKycId()
            );
            log.info(
                    "{}, active thread: {}, eKyc item: {}",
                    prefixLog,
                    threadPoolTaskExecutor.getActiveCount(),
                    CommonUtil.objectToStringJsonIgnoreError(eKyc)
            );

            if (ChronoUnit.SECONDS.between(eKyc.getUpdatedAt(), ZonedDateTime.now()) > appConf.getThreadPool().getMaxPeriodQuerySeconds()) {
                throw new GeneralException(
                        MessageFormat.format("{0}, eKyc updated > 30 minutes --> switch handle to JOB", prefixLog)
                );
            }

            String apiKey = appConf.getFeignClient().getLotteApi().getApiKey();
            LotteAccountNumberRequest accountNumberRequest = new LotteAccountNumberRequest(eKyc.getIdentifierId());
            LotteAccountNumberResponse accountNumberResponse = this.lotteApiClientImpl.getAccountNumberInfo(prefixLog, accountNumberRequest, apiKey);
            if (Objects.isNull(accountNumberResponse) || CollectionUtils.isEmpty(accountNumberResponse.getData_list())) {
                log.info(
                        MessageFormat.format(
                                "{0}, accountNumberResponse is empty --> submit to another thread, active thread: {1}",
                                prefixLog,
                                threadPoolTaskScheduler.getActiveCount()
                        )
                );
                if (threadPoolTaskScheduler.getScheduledThreadPoolExecutor().isShutdown()) {
                    threadPoolTaskScheduler.initialize();
                }
                long delay = ChronoUnit.SECONDS.between(eKyc.getUpdatedAt(), ZonedDateTime.now()) <= appConf.getThreadPool().getFirstPeriodTimeSecond()
                        ? appConf.getThreadPool().getFirstPeriodDelayTimeMillisecond()
                        : appConf.getThreadPool().getLast29MinutestPeriodDelayMillisecond();
                threadPoolTaskScheduler.schedule(
                        () -> {
                            try {
                                triggerUpdateAccountNumber(eKyc.getId(), ctxId);
                            } catch (Exception e) {
                                log.error("{} triggerUpdateAccountNumber error: ", prefixLog, e);
                            }
                        },
                        new Date(
                                System.currentTimeMillis() + delay
                        )
                );
            } else {
                Boolean eKycAccNumUpdated = updateAccountNumber(prefixLog, eKyc, accountNumberResponse);
                if (Boolean.FALSE.equals(eKycAccNumUpdated)) {
                    throw new GeneralException("updateAccountNumber is failed");
                }
                log.info("{} updateAccountNumber is complete", prefixLog);

                Boolean initContract = eContractCustomService.initiateFptEContract(prefixLog, eKyc);
                if (Boolean.FALSE.equals(initContract)) {
                    throw new GeneralException("initiateFptEContract is failed");
                }
                log.info("{} initiateFptEContract is complete", prefixLog);
            }
        } catch (Exception e) {
            log.error("triggerUpdateAccountNumber root: -- ctxId : {}, eKycId: {}, error: ", ctxId, eKycId, e);
        }
    }

    @Override
    @Scheduled(cron = "${app.cron.eKycUpdateAccNumJob}")
    public void updateAccountNumberInfoJob() {
        String prefixLog = Constants.E_KYC_UPDATE_ACCOUNT_NUMBER_JOB;
        if (!appConf.getCron().isEKycUpdateAccNumJobActiveStatus()) {
            log.info("{} is inActive", prefixLog);
            return;
        }
        log.info("{} START: {}", prefixLog, LocalDateTime.now().atZone(ZoneId.systemDefault()));
        if (redisDao.isExists(prefixLog)) {
            log.info("{} is running", prefixLog);
            return;
        } else {
            try {
                redisDao.set(
                        prefixLog,
                        true,
                        // The timeout duration is equal to half of the job's rerun time.
                        appConf.getCron().getEKycUpdateAccNumJobTimeIntervalMilliseconds()
                );
            } catch (JsonProcessingException e) {
                log.info("set key {} is failed", prefixLog);
                try {
                    redisDao.set(prefixLog, true, appConf.getDefaulltIdCardExpiredTime());
                } catch (JsonProcessingException ex) {
                    log.info("re-try set key {} is failed", prefixLog);
                    return;
                }
            }
        }
        List<EKyc> eKycList = this.eKycRepository.findAllEKycNotUpdateAccountNumber(Status.WAITING_CONFIRMATION);
        if (CollectionUtils.isEmpty(eKycList)) {
            log.info("{} eKyc list is empty!", prefixLog);
            return;
        }
        ExecutorService copyTask = threadPoolTaskExecutor.getThreadPoolExecutor();
        int chunk = commonService.getChunk(eKycList.size());
        log.info(
                "{} active thread:{}, chunk :{}, eKycList: {}",
                prefixLog,
                threadPoolTaskExecutor.getActiveCount(),
                chunk,
                CommonUtil.objectToStringJsonIgnoreError(eKycList)
        );
        List<List<EKyc>> subscribersPartition = Lists.partition(eKycList, chunk);
        for (int i = 0; i < subscribersPartition.size(); i++) {
            int finalI = i;
            copyTask.submit(() -> jobExecuteUpdateAccountNumber(prefixLog, subscribersPartition.get(finalI), finalI));
        }
        log.info("[{}] END: {}", prefixLog, LocalDateTime.now().atZone(ZoneId.systemDefault()));
    }

    private void jobExecuteUpdateAccountNumber(String prfLog, List<EKyc> eKycList, int index) {
        for (EKyc eKyc : eKycList) {
            String prefixLog = String.format("[%s_%s_%s_%s_%s] ", prfLog, index, eKyc.getId(), eKyc.geteKycId(), eKyc.getIdentifierId());
            try {
                Boolean eKycAccNumUpdated = updateAccountNumber(prefixLog, eKyc, null);
                if (Objects.isNull(eKycAccNumUpdated)) {
                    throw new GeneralException("updateAccountNumber is failed");
                }
            } catch (Exception e) {
                log.error("{} error: ", prefixLog, e);
            }
        }
    }

    private Boolean updateAccountNumber(String prefixLog, EKyc eKyc, LotteAccountNumberResponse accountNumberData) {
        try {
            transactionTemplate.setTransactionManager(platformTransactionManager);
            return this.transactionTemplate.execute(
                    status -> {
                        String apiKey = appConf.getFeignClient().getLotteApi().getApiKey();
                        LotteAccountNumberRequest accountNumberRequest = new LotteAccountNumberRequest(eKyc.getIdentifierId());
                        log.info(
                                "{} -- getAccountNumberInfo -- accessToken: {}, accountNumberRequest: {}",
                                prefixLog,
                                apiKey,
                                CommonUtil.objectToStringJsonIgnoreError(accountNumberRequest)
                        );
                        LotteAccountNumberResponse accountNumberResponse;
                        if (Objects.isNull(accountNumberData)) {
                            accountNumberResponse =
                                    this.lotteApiClientImpl.getAccountNumberInfo(prefixLog, accountNumberRequest, apiKey);
                            if (Objects.isNull(accountNumberResponse) || CollectionUtils.isEmpty(accountNumberResponse.getData_list())) {
                                throw new GeneralException(MessageFormat.format("{0}, accountNumberResponse is empty", prefixLog));
                            }
                        } else {
                            accountNumberResponse = accountNumberData;
                        }
                        Optional<EKyc> entityUpdated =
                                this.eKycRepository.findByIdAndStatusAndAccountNumberStatus(eKyc.getId(), Status.APPROVED);
                        log.info("{} eKyc record: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(entityUpdated));
                        if (entityUpdated.isPresent()) {
                            log.info("{} eKyc is updated", prefixLog);
                        } else {
                            LotteAccountNumberResponse.DataList accInfo = accountNumberResponse.getData_list().get(0);
                            eKyc.setAccountNumber(accInfo.getAcnt_no());
                            eKyc.setContractNo(accInfo.getCntr_no());
                            eKyc.setStatus(Status.APPROVED);
                            EKyc saveResult = this.eKycRepository.save(eKyc);
                            log.info(
                                    "{} eKyc account number info update: {}_{}_{}",
                                    prefixLog,
                                    saveResult.getId(),
                                    saveResult.getAccountNumber(),
                                    saveResult.getContractNo()
                            );
                        }
                        return Boolean.TRUE;
                    }
            );
        } catch (Exception e) {
            log.error("{} error: ", prefixLog, e);
            return Boolean.FALSE;
        }
    }
}
