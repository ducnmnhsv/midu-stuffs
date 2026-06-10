package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.utils.Pair;
import com.techx.tradex.common.utils.StringUtils;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.domain.Blockholder;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.EKycAdditionalInfo;
import com.techx.tradex.ekycadmin.domain.EKycBankList;
import com.techx.tradex.ekycadmin.domain.EKycCreatorStatus;
import com.techx.tradex.ekycadmin.domain.PublicCoop;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import com.techx.tradex.ekycadmin.models.enums.LotteLangCode;
import com.techx.tradex.ekycadmin.models.lotte.LotteEKycCreateAccountReq;
import com.techx.tradex.ekycadmin.models.lotte.LotteEKycCreateAccountRes;
import com.techx.tradex.ekycadmin.models.lotte.LotteEKycUpdateAccountReq;
import com.techx.tradex.ekycadmin.models.lotte.LotteEKycUpdateAccountRes;
import com.techx.tradex.ekycadmin.models.request.EKycAddReq;
import com.techx.tradex.ekycadmin.models.response.CreateEKycResponse;
import com.techx.tradex.ekycadmin.models.response.EKycAddRes;
import com.techx.tradex.ekycadmin.repository.BlockholderRepository;
import com.techx.tradex.ekycadmin.repository.CustomEKycRepository;
import com.techx.tradex.ekycadmin.repository.EKycAdditionalInfoRepository;
import com.techx.tradex.ekycadmin.repository.EKycBankListRepository;
import com.techx.tradex.ekycadmin.repository.EKycCreatorStatusRepository;
import com.techx.tradex.ekycadmin.repository.PublicCoopRepository;
import com.techx.tradex.ekycadmin.utils.Util;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LotteEKycService {

    private static final Logger log = LoggerFactory.getLogger(LotteEKycService.class);
    static final String PENDING_STATUS = "PENDING";
    static final String FAIL_STATUS = "FAIL";
    public static final String SUCCESS_STATUS = "SUCCESS";
    private final AppConf appConf;
    private final CustomEKycRepository eKycRepository;
    private final EKycCreatorStatusRepository ekycCreatorStatusRepository;
    private final LotteApiService lotteApiService;
    private final EContractCustomService eContractCustomService;

    public LotteEKycService(
        AppConf appConf,
        CustomEKycRepository eKycRepository,
        EKycCreatorStatusRepository ekycCreatorStatusRepository,
        LotteApiService lotteApiService,
        EContractCustomService eContractCustomService
    ) {
        this.appConf = appConf;
        this.eKycRepository = eKycRepository;
        this.ekycCreatorStatusRepository = ekycCreatorStatusRepository;
        this.lotteApiService = lotteApiService;
        this.eContractCustomService = eContractCustomService;
    }

    public CreateEKycResponse createEKycLotte(String txId, EKycAddReq req) throws IOException {
        log.info("{} create ekyc {}", txId, req);
        req.validReqCreateEKycLotte();
        LotteEKycCreateAccountRes createAccountRes = this.callLotteEKycCreateAccount(req, txId);
        return new CreateEKycResponse(createAccountRes.getDataList().get(0).getOsSeqNo());
    }

    @Async
    @Transactional
    public EKycCreatorStatus lotteEKycAsync(Long eKycId, EKycAddReq req, String txId) {
        EKyc eKyc =
            this.eKycRepository.findById(eKycId).orElseThrow(() -> new BadRequestAlertException("EKYC not exist", "EKyc", "notexist"));
        return this.lotteEKycNoAsync(eKyc, req, true, true, txId);
    }

    public EKycCreatorStatus lotteEKycNoAsync(
        EKyc ekyc,
        EKycAddReq req,
        boolean returnNullDefault,
        boolean checkMatchingRate,
        String txId
    ) {
        Double matchingThresholdCoreLotte = appConf.getMatchThresholdPercent().get(appConf.getCore());
        if (appConf.isEnableCallTllOpenAccount()) {
            if (!ekyc.getStatus().equals(Status.PENDING)) {
                log.error("{} {}-{} EKYC already approved", txId, ekyc.getIdentifierId(), ekyc.getPhoneNo());
                throw new BadRequestAlertException("EKYC already approved", "EKyc", "exist");
            }
            EKycCreatorStatus creatorStatus =
                this.ekycCreatorStatusRepository.findById(ekyc.getId())
                    .orElseGet(
                        () -> {
                            EKycCreatorStatus eKycCreatorStatus = new EKycCreatorStatus();
                            eKycCreatorStatus.setStatus(PENDING_STATUS);
                            eKycCreatorStatus.setEKyc(ekyc);
                            return eKycCreatorStatus;
                        }
                    );
            try {
                if (!checkMatchingRate || matchingThresholdCoreLotte == null || matchingThresholdCoreLotte - ekyc.getMatchingRate() <= 0) {
                    if (SUCCESS_STATUS.equals(creatorStatus.getStatus())) {
                        log.error("{} {}-{} eKyc already auto approved", txId, ekyc.getIdentifierId(), ekyc.getPhoneNo());
                        throw new BadRequestAlertException("eKyc already auto approved", "EKyc", "exist");
                    } else {
                        creatorStatus.setReason(null);
                    }
                    creatorStatus.setUpdatedAt(ZonedDateTime.now());
                    LotteEKycUpdateAccountRes res = this.callLotteEKycUpdateAccount(req, txId);
                    creatorStatus.setFullResult(res.toString());
                    ekyc.setStatus(Status.WAITING_CONFIRMATION);
                    ekyc.setUpdatedAt(ZonedDateTime.now());
                    try {
                        this.eKycRepository.save(ekyc);
                    } catch (Exception ex) {
                        log.error("{} {}-{} fail when call save eKyc approve status", txId, ekyc.getIdentifierId(), ekyc.getPhoneNo(), ex);
                    }
                } else {
                    log.error(
                        "{} {}-{} The matching rate is too low {} compare with threshold {}",
                        txId,
                        ekyc.getIdentifierId(),
                        ekyc.getPhoneNo(),
                        ekyc.getMatchingRate(),
                        matchingThresholdCoreLotte
                    );
                    if (appConf.getCallCoreNoAsync()) {
                        throw new GeneralException("MATCHING_RATE_TOO_LOW");
                    }
                    creatorStatus.setStatus(FAIL_STATUS);
                    creatorStatus.setReason("Matching rate is too low");
                }
            } catch (GeneralException ge) {
                log.error("{} {}-{} fail when request lotte api eKyc", txId, ekyc.getIdentifierId(), ekyc.getPhoneNo(), ge);
                if (appConf.getCallCoreNoAsync()) {
                    throw ge.source(ge.getCause());
                }
                creatorStatus.setStatus(FAIL_STATUS);
                creatorStatus.setReason("Fail when request lotte api eKyc: " + ge.getCode());
            } catch (Exception e) {
                log.error("{} {}-{} fail when request lotte api eKyc", txId, ekyc.getIdentifierId(), ekyc.getPhoneNo(), e);
                if (appConf.getCallCoreNoAsync()) {
                    throw new GeneralException().source(e.getCause());
                }
                creatorStatus.setStatus(FAIL_STATUS);
                creatorStatus.setReason("Fail when request lotte api eKyc: " + e.getMessage());
            } finally {
                ekycCreatorStatusRepository.save(creatorStatus);
            }
            return creatorStatus;
        } else {
            if (returnNullDefault) {
                return null;
            }
            throw new BadRequestAlertException("Configuration not allow to open account by api", "EKyc", "wrongconfig");
        }
    }

    private LotteEKycCreateAccountRes callLotteEKycCreateAccount(EKycAddReq req, String txId) throws IOException {
        String langCode = req.getHeaders() != null && StringUtils.isNotEmpty(req.getHeaders().getAcceptLanguage())
            ? req.getHeaders().getAcceptLanguage()
            : LotteLangCode.vi.getCode();
        LotteEKycCreateAccountReq createAccountReq = new LotteEKycCreateAccountReq()
            .update(req.getPhoneNo(), req.getEmail(), req.getGroupType(), langCode, req.getDeviceUniqueId());
        log.info("{} {}-{} will request create account to core {}", txId, req.getIdentifierId(), req.getPhoneNo(), createAccountReq);
        Pair<LotteEKycCreateAccountRes, String> response =
            this.lotteApiService.postData(
                this.lotteApiService.getUrl(this.appConf.getLotteConfig().getCreateAccountUrl()).build().toUri(),
                LotteEKycCreateAccountRes.class,
                createAccountReq,
                txId
            );
        LotteEKycCreateAccountRes createAccountRes = response.getLeft();
        Pair<String, String> codePair = LotteApiService.parseMessages(
            createAccountRes.getErrorDesc(),
            createAccountRes.getErrorCode(),
            appConf
        );
        if (codePair.getLeft() != null) {
            throw new GeneralException(createAccountRes.getErrorDesc());
        }
        return createAccountRes;
    }

    private LotteEKycUpdateAccountRes callLotteEKycUpdateAccount(EKycAddReq req, String txId) throws Exception {
        String langCode = req.getHeaders() != null && StringUtils.isNotEmpty(req.getHeaders().getAcceptLanguage())
            ? req.getHeaders().getAcceptLanguage()
            : LotteLangCode.vi.getCode();
        LotteEKycUpdateAccountReq updateAccountReq = new LotteEKycUpdateAccountReq().update(req, langCode);
        log.info("{} {}-{} will request update account to core {}", txId, req.getIdentifierId(), req.getPhoneNo(), updateAccountReq);
        Pair<LotteEKycUpdateAccountRes, String> response =
            this.lotteApiService.postData(
                this.lotteApiService.getUrl(this.appConf.getLotteConfig().getUpdateAccountUrl()).build().toUri(),
                LotteEKycUpdateAccountRes.class,
                updateAccountReq,
                txId
            );
        LotteEKycUpdateAccountRes updateAccountRes = response.getLeft();
        Pair<String, String> codePair = LotteApiService.parseMessages(
            updateAccountRes.getErrorDesc(),
            updateAccountRes.getErrorCode(),
            appConf
        );
        if (codePair.getLeft() != null && !codePair.getLeft().equals("3103")) {
            throw new GeneralException(codePair.getRight());
        }
        return updateAccountRes;
    }

    public void callUploadImage(String seqNo, Integer imgKind, String imgSource, String identifierId, String txId) {
        try {
            Pair<byte[], String> result = Util.compressAndConvertImageJPG(
                imgSource,
                appConf.getResizeSignature().getWidth(),
                appConf.getResizeSignature().getHeigth(),
                appConf.getResizeSignature().getQuality(),
                appConf.getResizeSignature().getMaxSize()
            );
            log.info("{} will request upload image to core {}-{}-{}", txId, seqNo, imgKind, result.getRight());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder
                .addTextBody("seq_no", seqNo)
                .addTextBody("img_kind", imgKind.toString())
                .addTextBody("id_no", identifierId)
                .addBinaryBody("file", result.getLeft(), ContentType.IMAGE_JPEG, result.getRight());
            this.lotteApiService.postFormData(
                this.lotteApiService.getUrl(this.appConf.getLotteConfig().getUploadImageUrl()).build().toUri(),
                Object.class,
                builder.build(),
                txId
            );
        } catch (Exception ex) {
            log.error("{} {} fail when call upload image", txId, identifierId, ex);
        }
    }
}
