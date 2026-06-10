package com.techx.tradex.ekycadmin.service.impl;

import com.difisoft.model.requests.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.InvalidParameterException;
import com.techx.tradex.common.utils.Pair;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.config.defaultConfig.Datas;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.constant.Messages;
import com.techx.tradex.ekycadmin.dao.RedisDao;
import com.techx.tradex.ekycadmin.domain.Blockholder;
import com.techx.tradex.ekycadmin.domain.EContract;
import com.techx.tradex.ekycadmin.domain.EContractInfo;
import com.techx.tradex.ekycadmin.domain.EContractInfoHistory;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.EKycAdditionalInfo;
import com.techx.tradex.ekycadmin.domain.EKycBankList;
import com.techx.tradex.ekycadmin.domain.PublicCoop;
import com.techx.tradex.ekycadmin.domain.enumeration.ContactIdAction;
import com.techx.tradex.ekycadmin.domain.enumeration.ContractStatus;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import com.techx.tradex.ekycadmin.models.dto.EContractField;
import com.techx.tradex.ekycadmin.models.lotte.LotteEKycUpdateContractStatusReq;
import com.techx.tradex.ekycadmin.models.lotte.LotteEKycUpdateContractStatusRes;
import com.techx.tradex.ekycadmin.models.request.EContractStatusReq;
import com.techx.tradex.ekycadmin.models.request.FptECEnvelopesRecipientRequest;
import com.techx.tradex.ekycadmin.models.request.FptECExCallRequest;
import com.techx.tradex.ekycadmin.models.request.FptECLoginRequest;
import com.techx.tradex.ekycadmin.models.request.FptECSignRequest;
import com.techx.tradex.ekycadmin.models.response.EContractStatusRes;
import com.techx.tradex.ekycadmin.models.response.FptECEnvelopesRecipientResponse;
import com.techx.tradex.ekycadmin.models.response.FptECExCallResponse;
import com.techx.tradex.ekycadmin.models.response.FptECLoginResponse;
import com.techx.tradex.ekycadmin.models.response.FptECSignResponse;
import com.techx.tradex.ekycadmin.models.response.FptECTemplateStructureResponse;
import com.techx.tradex.ekycadmin.models.response.GenericResponse;
import com.techx.tradex.ekycadmin.repository.BlockholderRepository;
import com.techx.tradex.ekycadmin.repository.EContractCustomRepository;
import com.techx.tradex.ekycadmin.repository.EContractInfoCustomRepository;
import com.techx.tradex.ekycadmin.repository.EContractInfoHistoryRepository;
import com.techx.tradex.ekycadmin.repository.EKycAdditionalInfoRepository;
import com.techx.tradex.ekycadmin.repository.EKycBankListRepository;
import com.techx.tradex.ekycadmin.repository.EKycRepository;
import com.techx.tradex.ekycadmin.repository.PublicCoopRepository;
import com.techx.tradex.ekycadmin.service.CommonService;
import com.techx.tradex.ekycadmin.service.EContractCustomService;
import com.techx.tradex.ekycadmin.service.EContractInfoCustomService;
import com.techx.tradex.ekycadmin.service.LotteApiService;
import com.techx.tradex.ekycadmin.service.MailService;
import com.techx.tradex.ekycadmin.utils.CommonUtil;
import com.techx.tradex.ekycadmin.utils.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Primary
public class EContractCustomServiceImpl implements EContractCustomService {

    private static final Logger log = LogManager.getLogger(EContractServiceImpl.class);

    private final EKycRepository eKycRepository;
    private final LotteApiClientImpl lotteApiClientImpl;
    private final AppConf appConf;
    private final FptEContractApiClientImpl fptEContractApiClientImpl;
    private final EKycBankListRepository eKycBankListRepository;
    private final EKycAdditionalInfoRepository eKycAdditionalInfoRepository;
    private final PublicCoopRepository publicCoopRepository;
    private final BlockholderRepository blockholderRepository;
    private final EContractCustomRepository eContractCustomRepository;
    private final EContractInfoCustomService eContractInfoCustomService;
    private final CommonService commonService;
    private final EContractInfoCustomRepository econtractInfoRepository;
    private final MailService mailService;
    private final LotteApiService lotteApiService;
    private final EContractInfoHistoryRepository eContractInfoHistoryRepository;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisDao redisDao;
    private final TransactionTemplate transactionTemplate;
    private final PlatformTransactionManager platformTransactionManager;
    private final com.difisoft.redis.RedisDao difisoftRedisDao;

    public EContractStatusRes getEContractStatus(EContractStatusReq request) {
        log.info("getEContractStatus -- request: {}", CommonUtil.objectToStringJsonIgnoreError(request));
        if (request.getData() != null) {
            String decryptData = Util.decryptRSA(request.getData(), appConf.getKeyPath());
            String data =
                (
                    request.getRefId() +
                    "_" +
                    request.getEnvelopeId() +
                    "_" +
                    request.getContactId() +
                    "_" +
                    request.getContactIdAction() +
                    "_" +
                    request.getContractStatus()
                );
            if (!StringUtils.equals(decryptData, data)) {
                throw new GeneralException(Constants.EContract.INVALID_DATA_MESSAGE);
            }
        } else {
            throw new InvalidParameterException()
                .add(Constants.FIELD_IS_REQUIRED, Constants.EContract.DATA, Collections.singletonList(Constants.EContract.DATA));
        }
        if (request.getRefId() == null) {
            throw new InvalidParameterException()
                .add(Constants.FIELD_IS_REQUIRED, Constants.EContract.REF_ID, Collections.singletonList(Constants.EContract.REF_ID));
        }
        if (request.getEnvelopeId() == null) {
            throw new InvalidParameterException()
                .add(
                    Constants.FIELD_IS_REQUIRED,
                    Constants.EContract.ENVELOP_ID,
                    Collections.singletonList(Constants.EContract.ENVELOP_ID)
                );
        }
        if (request.getContractStatus() == null) {
            throw new InvalidParameterException()
                .add(
                    Constants.FIELD_IS_REQUIRED,
                    Constants.EContract.CONTRACT_STATUS,
                    Collections.singletonList(Constants.EContract.CONTRACT_STATUS)
                );
        }
        EContract eContract = eContractCustomRepository.findByRefIdAndEnvelopeId(request.getRefId(), request.getEnvelopeId()).orElse(null);
        if (eContract == null) {
            throw new GeneralException(Constants.EContract.CONTRACT_NOT_FOUND);
        }
        EContractInfo eContractInfo = econtractInfoRepository.findByEContractId(eContract.getId()).orElse(null);
        if (eContractInfo == null) {
            eContractInfo = new EContractInfo();
            eContractInfo.setId(eContract.getId());
            eContractInfo.setCreatedAt(ZonedDateTime.now());
        }
        ContractStatus contractStatus;
        if (request.getContractStatus() == null) {
            throw new InvalidParameterException()
                .add(
                    Constants.FIELD_IS_REQUIRED,
                    Constants.EContract.CONTRACT_STATUS,
                    Collections.singletonList(Constants.EContract.CONTRACT_STATUS)
                );
        } else {
            try {
                contractStatus = ContractStatus.valueOf(request.getContractStatus());
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException()
                    .add(
                        Constants.INVALID_VALUE,
                        Constants.EContract.CONTRACT_STATUS,
                        Collections.singletonList(Constants.EContract.CONTRACT_STATUS)
                    );
            }
            eContractInfo.setContractStatus(contractStatus.name());
        }
        if (request.getContactId() != null) {
            eContractInfo.setContactId(request.getContactId());
            ContactIdAction contactIdAction;
            if (request.getContactIdAction() == null) {
                throw new InvalidParameterException()
                    .add(
                        Constants.FIELD_IS_REQUIRED,
                        Constants.EContract.CONTRACT_ID_ACTION,
                        Collections.singletonList(Constants.EContract.CONTRACT_ID_ACTION)
                    );
            } else {
                try {
                    contactIdAction = ContactIdAction.valueOf(request.getContactIdAction());
                } catch (IllegalArgumentException e) {
                    throw new InvalidParameterException()
                        .add(
                            Constants.INVALID_VALUE,
                            Constants.EContract.CONTRACT_ID_ACTION,
                            Collections.singletonList(Constants.EContract.CONTRACT_ID_ACTION)
                        );
                }
            }
            if (request.getContactId().equals(eContract.getIdentifierId())) {
                if (contactIdAction.equals(ContactIdAction.signed) && contractStatus.equals(ContractStatus.processing)) {
                    eContractInfo.setCustomerSignatueStatus(contactIdAction.name());
                    String accesToken = loginFPT(eContract.getEKyc().geteKycId().toString());
                    final EContractInfo finalEContractInfo = eContractInfo;
                    int i = appConf.getMaxRetryCallTllGetContract();
                    boolean foundSignFileContent = false;
                    while (i > 0 && !foundSignFileContent) {
                        try {
                            FptECExCallResponse fptECExCallResponse =
                                this.exCall(
                                        eContract.getEnvelopeId(),
                                        eContract.getRefId(),
                                        eContract.getEKyc().geteKycId().toString(),
                                        accesToken
                                    );
                            FptECExCallResponse.Response response = fptECExCallResponse.getResponse();
                            if (response != null) {
                                for (FptECExCallResponse.Contact contact : response.getContacts()) {
                                    if (
                                        contact.getContactId().equals(eContract.getIdentifierId()) &&
                                        contact.getSignStatus().equals(Constants.accepted)
                                    ) {
                                        finalEContractInfo.setSignFileContent(contact.getSignFileContent());
                                        foundSignFileContent = true;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("getEContractStatus -- error: {}", e.getMessage());
                        }
                        i--;
                    }
                    String signedFileContent = finalEContractInfo.getSignFileContent();
                    String accountNumber = eContract.getEKyc().getAccountNumber();
                    if (accountNumber != null) {
                        callLotteEKycUpdateContractStatus(accountNumber, eContract.getRefId() + "_" + eContract.getEnvelopeId());
                    }
                    if (signedFileContent != null) {
                        byte[] jpgImage = null;
                        try {
                            jpgImage = Util.convertBase64toJPG(signedFileContent);
                        } catch (IOException e) {
                            throw new GeneralException(Constants.EKYC_UPLOAD_IMAGE_ERROR);
                        }
                        log.info("getEContractStatus -- jpgImage: {}", jpgImage);
                        eContractInfo.setSignFileContent(signedFileContent);
                        String seq = eContract.getEKyc().geteKycId().toString();
                        callUploadImage(seq, 1, jpgImage, accountNumber, eContract.getIdentifierId(), eContract.getRefId() + "_" + eContract.getEnvelopeId());
                    }
                }
                if (contactIdAction.equals(ContactIdAction.rejected) && contractStatus.equals(ContractStatus.rejected)) {
                    eContractInfo.setCustomerSignatueStatus(contactIdAction.name());
                }
            } else {
                if (contactIdAction.equals(ContactIdAction.signed) && contractStatus.equals(ContractStatus.processing)) {
                    eContractInfo.setSecuritiesSignatureStatus(contactIdAction.name());
                }
                if (contactIdAction.equals(ContactIdAction.rejected) && contractStatus.equals(ContractStatus.rejected)) {
                    eContractInfo.setSecuritiesSignatureStatus(contactIdAction.name());
                }
            }
        } else {
            if (request.getContactIdAction() == null && request.getContractStatus().equals(ContractStatus.completed.name())) {
                String accesToken = loginFPT(eContract.getEKyc().geteKycId().toString());
                String fileName = Util.getContractFileName(eContractInfo.getRequestData()) + ".pdf";
                String contractFileContent = null;
                int i = appConf.getMaxRetryCallTllGetContract();
                while (i > 0 && contractFileContent == null) {
                    try {
                        FptECExCallResponse fptECExCallResponse =
                            this.exCall(
                                    eContract.getEnvelopeId(),
                                    eContract.getRefId(),
                                    eContract.getEKyc().geteKycId().toString(),
                                    accesToken
                                );
                        FptECExCallResponse.Response response = fptECExCallResponse.getResponse();
                        if (response != null) {
                            if (
                                response.getContractStatus().equals(ContractStatus.completed.name()) &&
                                response.getContractFileContent() != null
                            ) {
                                contractFileContent = response.getContractFileContent();
                            }
                        }
                    } catch (Exception e) {
                        log.error("getEContractStatus -- error: {}", e.getMessage());
                    }
                    i--;
                }
                if (contractFileContent != null) {
                    eContractInfo.setContractFileContent(contractFileContent);
                    byte[] contractFile = Base64.getDecoder().decode(contractFileContent);
                    String email = eContract.getEKyc().getEmail();
                    String fullName = eContract.getEKyc().getFullName();
                    String accountNumber = eContract.getEKyc().getAccountNumber();
                    mailService.sendCompletedContractEmail(email, contractFile, fileName, fullName, accountNumber);
                }
            }
        }
        eContractInfo.setEContract(eContract);
        eContractInfo.setUpdatedAt(ZonedDateTime.now());
        try {
            econtractInfoRepository.save(eContractInfo);
        } catch (Exception e) {
            log.error("getEContractStatus -- error: {}", e.getMessage());
            throw new GeneralException(Constants.EContract.SAVE_CONTRACT_INFO_ERROR);
        }
        try {
            eContractInfoHistoryRepository.save(new EContractInfoHistory(eContractInfo));
        } catch (Exception e) {
            log.error("getEContractStatus -- error: {}", e.getMessage());
        }
        return new EContractStatusRes();
    }

    private String loginFPT(String id) {
        String prefixLog = String.format("getEContractStatus -- eKycId: %s", id);
        AppConf.EContract eContractConf = this.appConf.getFeignClient().getFpt().getEContract();
        AppConf.LoginInfo fptLoginInfo = eContractConf.getLoginInfo();
        FptECLoginResponse loginResp =
            this.fptEContractApiClientImpl.login(
                    prefixLog,
                    new FptECLoginRequest(
                        fptLoginInfo.getUsername(),
                        fptLoginInfo.getPassword(),
                        fptLoginInfo.getClientId(),
                        fptLoginInfo.getClientSecret()
                    )
                );
        if (Objects.isNull(loginResp) || Objects.isNull(loginResp.getAccess_token())) {
            throw new GeneralException(MessageFormat.format("{0}, access token is empty", prefixLog));
        }
        return loginResp.getAccess_token();
    }

    private FptECExCallResponse exCall(String envelopeId, String refId, String id, String token) {
        String prefixLog = String.format("getEContractStatus -- eKycId: %s", id);
        FptECExCallRequest exCallRequest = new FptECExCallRequest();
        exCallRequest.setSelector(Constants.EContract.SELECTOR);
        FptECExCallRequest.Body body = new FptECExCallRequest.Body();
        List<FptECExCallRequest.Act> actList = new ArrayList<>();
        FptECExCallRequest.Act act = new FptECExCallRequest.Act();
        act.setEnvelopeId(envelopeId);
        act.setRefId(refId);
        actList.add(act);
        body.setActList(actList);
        exCallRequest.setBody(body);
        FptECExCallResponse fptECExCallResponse = this.fptEContractApiClientImpl.exCall(prefixLog, exCallRequest, token);
        if (Objects.isNull(fptECExCallResponse) || Objects.isNull(fptECExCallResponse.getResponse())) {
            throw new GeneralException(
                MessageFormat.format("{0}, fptECExCall response is invalid (response is null || envelopeId is null)", prefixLog)
            );
        }
        return fptECExCallResponse;
    }

    private LotteEKycUpdateContractStatusRes callLotteEKycUpdateContractStatus(String accountNumber, String txId) {
        log.info("callLotteEKycUpdateContractStatus -- accountNumber: {}, txId: {}", accountNumber, txId);
        LotteEKycUpdateContractStatusReq req = new LotteEKycUpdateContractStatusReq();
        req.setAccountNumber(accountNumber);
        Pair<LotteEKycUpdateContractStatusRes, String> response;
        try {
            response =
                this.lotteApiService.postData(
                        this.lotteApiService.getUrl(this.appConf.getLotteConfig().getUpdateStatusContractUrl()).build().toUri(),
                        LotteEKycUpdateContractStatusRes.class,
                        req,
                        txId
                    );
        } catch (IOException e) {
            log.error("callLotteEKycUpdateContractStatus -- accountNumber: {}, txId: {}, error: {}", accountNumber, txId, e.getMessage());
            throw new GeneralException(Constants.EKYC_UPDATE_CONTRACT_STATUS_ERROR);
        }
        LotteEKycUpdateContractStatusRes res = response.getLeft();
        if (!res.getErrorCode().equals("0000")) {
            log.error(
                "callLotteEKycUpdateContractStatus -- accountNumber: {}, txId: {}, error: {}",
                accountNumber,
                txId,
                res.getErrorDesc()
            );
        }
        return res;
    }

    @Async
    private void callUploadImage(String seqNo, Integer imgKind, byte[] imgSource, String fileName, String identifierId, String txId) {
        log.info("callUploadImage -- idNo: {} seqNo: {}, imgKind: {}, txId: {}", identifierId, seqNo, imgKind, txId);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder
            .addTextBody("seq_no", seqNo)
            .addTextBody("img_kind", imgKind.toString())
            .addTextBody("id_no", identifierId)
            .addBinaryBody("file", imgSource, ContentType.IMAGE_JPEG, String.format("%s.jpg", fileName));
        try {
            this.lotteApiService.postFormData(
                    this.lotteApiService.getUrl(this.appConf.getLotteConfig().getUploadImageUrl()).build().toUri(),
                    Object.class,
                    builder.build(),
                    txId
                );
        } catch (Exception e) {
            log.error("callUploadImage -- seqNo: {}, imgKind: {}, txId: {}, error: {}", seqNo, imgKind, txId, e.getMessage());
            throw new GeneralException(Constants.EKYC_UPLOAD_IMAGE_ERROR);
        }
    }

    @Override
    @Scheduled(cron = "${app.cron.initiateFptEContractJob}")
    public void initiateFptEContractJob() {
        String prefixLog = Constants.INITIATE_FPT_E_CONTRACT_JOB;
        if (!appConf.getCron().isInitiateFptEContractJobJobActiveStatus()) {
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
                    appConf.getCron().getInitiateFptEContractJobIntervalMilliseconds()
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
        List<EKyc> eKycList = this.eKycRepository.findAllEKycUpdateAccountNumberAndHaveNotInitiatedEContracts(Status.APPROVED);
        if (CollectionUtils.isEmpty(eKycList)) {
            log.info("{} eKyc list is empty!", prefixLog);
            return;
        }
        ExecutorService copyTask = threadPoolTaskExecutor.getThreadPoolExecutor();
        int chunk = commonService.getChunk(eKycList.size());
        log.info("{} chunk :{}, eKycList: {}", prefixLog, chunk, CommonUtil.objectToStringJsonIgnoreError(eKycList));
        List<List<EKyc>> subscribersPartition = Lists.partition(eKycList, chunk);
        for (int i = 0; i < subscribersPartition.size(); i++) {
            int finalI = i;
            copyTask.submit(() -> jobExecuteInitiateFptEContract(prefixLog, subscribersPartition.get(finalI), finalI));
        }
        log.info("[{}] END: {}", prefixLog, LocalDateTime.now().atZone(ZoneId.systemDefault()));
    }

    public void jobExecuteInitiateFptEContract(String prfLog, List<EKyc> eKycList, int index) {
        for (EKyc eKyc : eKycList) {
            String prefixLog = String.format("[%s_%s_%s_%s_%s] ", prfLog, index, eKyc.getId(), eKyc.geteKycId(), eKyc.getIdentifierId());
            try {
                Boolean initContract = initiateFptEContract(prefixLog, eKyc);
                if (Boolean.FALSE.equals(initContract)) {
                    throw new GeneralException("initiateFptEContract is failed");
                }
            } catch (Exception e) {
                log.error("{} error: ", prefixLog, e);
            }
        }
    }

    @Override
    public Boolean initiateFptEContract(String prefixLog, EKyc ekyc) {
        try {
            transactionTemplate.setTransactionManager(platformTransactionManager);
            return transactionTemplate.execute(
                status -> {
                    if (!StringUtils.startsWith(ekyc.getAccountNumber(), Constants.EContract.PREFIX_ACC_NUM)) {
                        throw new GeneralException(
                            MessageFormat.format(Constants.EContract.ACC_NUM_NOT_SUPPORTED, ekyc.getAccountNumber())
                        );
                    }
                    AppConf.EContract eContractConf = this.appConf.getFeignClient().getFpt().getEContract();
                    log.info("{}, eContractConf: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(eContractConf));

                    // Login info
                    AppConf.LoginInfo fptLoginInfo = eContractConf.getLoginInfo();
                    FptECLoginResponse loginResp =
                        this.fptEContractApiClientImpl.login(
                                prefixLog,
                                new FptECLoginRequest(
                                    fptLoginInfo.getUsername(),
                                    fptLoginInfo.getPassword(),
                                    fptLoginInfo.getClientId(),
                                    fptLoginInfo.getClientSecret()
                                )
                            );
                    if (Objects.isNull(loginResp) || Objects.isNull(loginResp.getAccess_token())) {
                        String msg = MessageFormat.format("{0}, access token is empty", prefixLog);
                        log.error("{} error: {}", prefixLog, msg);
                        throw new GeneralException(msg);
                    }

                    // Template structure info
                    FptECTemplateStructureResponse templateStructureResp =
                        this.fptEContractApiClientImpl.getTemplateStructure(
                                prefixLog,
                                eContractConf.getTemplate().getAlias().getHdmtk(),
                                loginResp.getAccess_token()
                            );
                    if (
                        Objects.isNull(templateStructureResp) ||
                        Objects.isNull(templateStructureResp.getTemplateId()) ||
                        CollectionUtils.isEmpty(templateStructureResp.getDatas()) ||
                        CollectionUtils.isEmpty(templateStructureResp.getDatas().get(0))
                    ) {
                        String msg = MessageFormat.format(
                            "{0}, templateStructure response is invalid. Response = {1}",
                            prefixLog,
                            templateStructureResp
                        );
                        log.error("{} error: {}", prefixLog, msg);
                        throw new GeneralException(msg);
                    }

                    // Request fpt e-contract
                    String refId = UUID.randomUUID().toString();
                    FptECExCallRequest exCallRequest;
                    try {
                        exCallRequest = this.buildEContractRequest(ekyc, templateStructureResp, refId, prefixLog);
                    } catch (Exception e) {
                        log.error("{}, buildEContractRequest error:", prefixLog, e);
                        throw new RuntimeException(e);
                    }
                    FptECExCallResponse fptECExCallResponse =
                        this.fptEContractApiClientImpl.exCall(prefixLog, exCallRequest, loginResp.getAccess_token());
                    if (
                        Objects.isNull(fptECExCallResponse) ||
                        Objects.isNull(fptECExCallResponse.getResponse()) ||
                        Objects.isNull(fptECExCallResponse.getResponse().getEnvelopeId())
                    ) {
                        String msg = MessageFormat.format(
                            "{0}, fptECExCall response is invalid (response is null || envelopeId is null)",
                            prefixLog
                        );
                        log.error("{} error: {}", prefixLog, msg);
                        throw new GeneralException(msg);
                    }

                    Optional<EContract> eContractInserted = this.eContractCustomRepository.findByEKycId(ekyc.getId());
                    log.info("{} eContract record: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(eContractInserted));
                    EContract eContract = new EContract();
                    if (eContractInserted.isPresent()) {
                        log.info("{} eContract is inserted", prefixLog);
                        eContract = eContractInserted.get();
                    } else {
                        // Save eContract info
                        eContract.setRefId(refId);
                        eContract.setEnvelopeId(fptECExCallResponse.getResponse().getEnvelopeId());
                        eContract.setIdentifierId(ekyc.getIdentifierId());
                        eContract.setTemplateId(templateStructureResp.getTemplateId());
                        eContract.setAlias(templateStructureResp.getAlias());
                        eContract.setCompanyType(Constants.EContract.FPT);
                        eContract.setEKyc(ekyc);
                        eContract.setCreatedAt(ZonedDateTime.now());
                        eContract.setUpdatedAt(ZonedDateTime.now());
                        EContract contractRs = this.eContractCustomRepository.save(eContract);
                        log.info("{}, eContract id insert: {}", prefixLog, contractRs.getId());
                    }

                    EContractInfo eContractInfoInserted = econtractInfoRepository.findByEContractId(eContract.getId()).orElse(null);
                    log.info("{} eContractInfo record: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(eContractInfoInserted));
                    if (Objects.nonNull(eContractInfoInserted)) {
                        log.info("{} eContractInfo is inserted", prefixLog);
                    } else {
                        EContractInfo eContractInfo = new EContractInfo();
                        eContractInfo.setCreatedAt(ZonedDateTime.now());
                        eContractInfo.setUpdatedAt(ZonedDateTime.now());
                        eContractInfo.setEContract(eContract);
                        eContractInfo.setTemplateId(templateStructureResp.getTemplateId());
                        exCallRequest
                            .getBody()
                            .getCustomData()
                            .forEach(
                                x -> {
                                    x.setPhotoBackSideIDCard(null);
                                    x.setPhotoFrontSideIDCard(null);
                                }
                            );
                        eContractInfo.setRequestData(CommonUtil.objectToStringJsonIgnoreError(exCallRequest));
                        EContractInfo infoRs = this.eContractInfoCustomService.save(eContractInfo);
                        log.info("{} eContractInfo ID insert: {}", prefixLog, infoRs.getId());
                    }

                    return Boolean.TRUE;
                }
            );
        } catch (Exception e) {
            log.error("{} error: ", prefixLog, e);
            return Boolean.FALSE;
        }
    }

    @Override
    public GenericResponse<FptECSignResponse> signEContract(FptECSignRequest request) {
        log.info("signEContract -- request: {}", request.toString());
        GenericResponse<FptECSignResponse> response;
        String identifierId = request.getIdentifierId();
        String eKycId = request.getEKycId();
        String prefixLog = String.format("signEContract -- identifierId: %s_%s", identifierId, eKycId);
        response = GenericResponse.success(StringUtils.EMPTY);

        Token token = request.getHeaders().getToken();
        commonService.validateTraDexTokenGrantType(token.getGrantType());
        boolean isClientCredentials = StringUtils.equalsIgnoreCase(token.getGrantType(), Constants.EContract.CLIENT_CREDENTIALS);
        log.info("{}, isClientCredentials: {}", prefixLog, isClientCredentials);

        String contactId;
        String envelopeId = null;
        if (isClientCredentials) {
            if (StringUtils.isBlank(eKycId)) {
                throw new InvalidParameterException()
                    .add(Constants.FIELD_IS_REQUIRED, Constants.E_KYC_ID_FIELD, Collections.singletonList(Constants.E_KYC_ID_FIELD));
            }

            // check if this ekyc id can be accessed by this session id (refresh token id)
            Long sessionId = request.getHeaders().getToken().getRefreshTokenId();
            log.info("{} -- sessionId: {}", prefixLog, sessionId);
            String accessableEkycId = difisoftRedisDao.get(Constants.EKYC_SESSION_ID_PREFIX + sessionId, String.class);
            log.info("{} -- accessableEkycId: {}", prefixLog, accessableEkycId);
            if (accessableEkycId == null || !accessableEkycId.equals(eKycId)) {
                throw new GeneralException(Messages.EKYC_ID_NOT_ACCESSIBLE_BY_THIS_SESSION);
            }

            Optional<EKyc> eKycOpt = this.eKycRepository.findByEKycId(eKycId);
            log.info("{} -- eKycOpt: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(eKycOpt));
            if (!eKycOpt.isPresent()) {
                throw new GeneralException(Messages.EKYC_NOT_FOUND);
            }
            else if (eKycOpt.get().getAccountNumber() == null) {
                throw new GeneralException(Messages.ACCOUNT_NUMBER_NOT_AVAILABLE);
            }
            Optional<EContract> eContractOpt = this.eContractCustomRepository.findByEKycTableId(eKycOpt.get().getId());
            log.info("{} -- eContractOpt: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(eContractOpt));
            if (!eContractOpt.isPresent()) {
                log.info("{} -- {}", prefixLog, Messages.E_CONTRACT_INFO_NOT_FOUND);
                FptECSignResponse data = new FptECSignResponse();
                data.setItems(new ArrayList<>());
                response.setData(data);
                return response;
            }

            contactId = eContractOpt.get().getIdentifierId();
            envelopeId = eContractOpt.get().getEnvelopeId();
        } else {
            if (Objects.isNull(token.getUserData())) {
                response = GenericResponse.badRequest(Messages.TOKEN_UD_NUMBER_IS_EMPTY);
                return response;
            }
            if (StringUtils.isBlank(token.getUserData().getIdentifierNumber())) {
                log.info("{} -- {}", prefixLog, Messages.IDENTIFIER_NUMBER_IS_EMPTY);
                FptECSignResponse data = new FptECSignResponse();
                data.setItems(new ArrayList<>());
                response.setData(data);
                return response;
            }
            contactId = token.getUserData().getIdentifierNumber();
        }

        // Login
        AppConf.EContract eContractConf = this.appConf.getFeignClient().getFpt().getEContract();
        AppConf.LoginInfo fptLoginInfo = eContractConf.getLoginInfo();
        FptECLoginResponse loginResp =
            this.fptEContractApiClientImpl.login(
                    prefixLog,
                    new FptECLoginRequest(
                        fptLoginInfo.getUsername(),
                        fptLoginInfo.getPassword(),
                        fptLoginInfo.getClientId(),
                        fptLoginInfo.getClientSecret()
                    )
                );
        if (Objects.isNull(loginResp)) {
            throw new GeneralException(Messages.FPT_LOGIN_IS_EMPTY);
        }
        if (Objects.isNull(loginResp.getAccess_token())) {
            throw new GeneralException(Messages.FPT_TOKEN_IS_INVALID);
        }

        // Get web view
        FptECEnvelopesRecipientRequest recipientReq = new FptECEnvelopesRecipientRequest(contactId, envelopeId);
        log.info("{}, recipientReq: {}", prefixLog, recipientReq);
        List<FptECEnvelopesRecipientResponse> envelopesRecipientResponse =
            this.fptEContractApiClientImpl.envelopesRecipient(
                    prefixLog,
                    loginResp.getAccess_token(),
                    Constants.DEFAULT_PAGE,
                    Constants.DEFAULT_SIZE,
                    Constants.DEFAULT_CHECK_AUTHENTICATE,
                    recipientReq
                );
        if (Objects.isNull(envelopesRecipientResponse)) {
            throw new GeneralException(Messages.ENVELOPES_RECIPIENT_RESPONSE_IS_EMPTY);
        }

        FptECSignResponse data = new FptECSignResponse();
        if (CollectionUtils.isEmpty(envelopesRecipientResponse)) {
            data.setItems(new ArrayList<>());
        } else {
            data.setItems(envelopesRecipientResponse.stream().map(this::buildSignItemResp).collect(Collectors.toList()));
        }
        response.setData(data);
        return response;
    }

    private FptECSignResponse.Item buildSignItemResp(FptECEnvelopesRecipientResponse item) {
        FptECSignResponse.WebView webView = new FptECSignResponse.WebView(
            item.getWebView().getUrl(),
            item.getWebView().getCookieName(),
            item.getWebView().getCookieValue(),
            item.getWebView().getExpireTime(),
            item.getWebView().getIframeUrl()
        );

        List<FptECEnvelopesRecipientResponse.HeaderField> headerField = item.getHeaderFields();
        AtomicReference<String> contractName = new AtomicReference<>(StringUtils.EMPTY);
        AtomicReference<String> contractNo = new AtomicReference<>(StringUtils.EMPTY);
        AtomicReference<String> createdDate = new AtomicReference<>(StringUtils.EMPTY);
        AtomicReference<String> submittedFrom = new AtomicReference<>(StringUtils.EMPTY);
        headerField.forEach(
            x -> {
                if (StringUtils.equals(x.getId(), Constants.EContract.vEnvName)) {
                    contractName.set(x.getValue());
                }
                if (StringUtils.equals(x.getId(), Constants.EContract.vEnvNo)) {
                    contractNo.set(x.getValue());
                }
                if (StringUtils.equals(x.getId(), Constants.EContract.vEnvDate)) {
                    createdDate.set(x.getValue());
                }
                if (StringUtils.equals(x.getId(), Constants.EContract.vEnvSubmittedFrom)) {
                    submittedFrom.set(x.getValue());
                }
            }
        );
        FptECSignResponse.ContractInfo contractInfo = new FptECSignResponse.ContractInfo(
            contractName.get(),
            contractNo.get(),
            createdDate.get(),
            submittedFrom.get()
        );

        return new FptECSignResponse.Item(item.getEnvId(), item.getEnvStatus(), item.getRecipientStatus(), contractInfo, webView);
    }

    private FptECExCallRequest buildEContractRequest(
        EKyc ekyc,
        FptECTemplateStructureResponse templateData,
        String refId,
        String prefixLog
    ) throws Exception {
        AppConf.HDMTK defaultFields = this.appConf.getTemplateEContract().getDefaultFields().getHdmtk();
        log.info("{}, defaultFields: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(defaultFields));

        FptECExCallRequest request = new FptECExCallRequest();
        request.setRefId(refId);
        request.setSelector(defaultFields.getSelector());
        request.setLookup(refId);
        request.setAttrs(setField(defaultFields.getAttrs()));
        request.setPayload(defaultFields.getPayload());

        FptECExCallRequest.Body body = new FptECExCallRequest.Body();
        body.setCustomData(Collections.singletonList(buildCustomData(ekyc, defaultFields)));
        body.setInputData(buildInputData(ekyc, defaultFields, templateData, refId, prefixLog));
        request.setBody(body);
        return request;
    }

    private FptECExCallRequest.CustomData buildCustomData(EKyc ekyc, AppConf.HDMTK defaultFields) throws Exception {
        FptECExCallRequest.CustomData customData = new FptECExCallRequest.CustomData();
        customData.setRecipientId(defaultFields.getRecipientId());
        customData.setEmail(ekyc.getEmail());
        customData.setTelephoneNumber(ekyc.getPhoneNo());
        customData.setContactId(ekyc.getIdentifierId());
        customData.setPersonalName(ekyc.getFullName());
        customData.setLocation(ekyc.getPermanentAddress());
        customData.setStateOrProvince(ekyc.getPermanentAddress());
        customData.setCountry(defaultFields.getCountry());
        customData.setPersonalID(ekyc.getIdentifierId());
        customData.setPassportID(setField(defaultFields.getPassportID()));
        customData.setType(defaultFields.getType());
        customData.setPhotoIDCard(setField(defaultFields.getPhotoIDCard()));
        customData.setPhotoIDCardContentType(setField(defaultFields.getPhotoIDCardContentType()));
        customData.setPhotoFrontSideIDCard(base64OnlineFile(ekyc.getFrontImageUrl()));
        customData.setPhotoFrontSideIDCardContentType(defaultFields.getPhotoFrontSideIDCardContentType());
        customData.setPhotoBackSideIDCard(base64OnlineFile(ekyc.getBackImageUrl()));
        customData.setPhotoBackSideIDCardContentType(defaultFields.getPhotoBackSideIDCardContentType());
        customData.setStatusCode(defaultFields.getStatusCode());
        customData.setResourceType(defaultFields.getResourceType());
        customData.setProvideAddress(ekyc.getIssuePlace());
        customData.setProvideDate(ekyc.getIssueDate().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DMY)));
        customData.setCommune(ekyc.getPermanentAddress());
        customData.setRefId(setField(defaultFields.getRefId()));
        return customData;
    }

    private FptECExCallRequest.InputData buildInputData(
        EKyc ekyc,
        AppConf.HDMTK defaultFields,
        FptECTemplateStructureResponse templateData,
        String refId,
        String prefixLog
    ) throws Exception {
        FptECExCallRequest.InputData inputData = new FptECExCallRequest.InputData();
        inputData.setTemplateId(templateData.getTemplateId());
        inputData.setAlias(defaultFields.getAlias());
        inputData.setSyncType(defaultFields.getSyncType());

        List<EKycBankList> bankList = this.eKycBankListRepository.findAllByEKycId(ekyc.getId());
        if (CollectionUtils.isEmpty(bankList)) {
            throw new GeneralException(MessageFormat.format("{0} -- bankList is empty", prefixLog));
        }
        log.info("{}, bankList: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(bankList));
        EKycAdditionalInfo additionalInfo = this.eKycAdditionalInfoRepository.findById(ekyc.getId()).orElse(null);
        log.info("{}, additionalInfo: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(additionalInfo));
        List<PublicCoop> publicCoops = new ArrayList<>();
        if (Objects.nonNull(additionalInfo)) {
            publicCoops.addAll(this.publicCoopRepository.findAllByEKycAdditionalInfoId(additionalInfo.getId()));
        }
        log.info("{}, publicCoops: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(publicCoops));
        List<Blockholder> blockHolders = new ArrayList<>();
        if (Objects.nonNull(additionalInfo)) {
            blockHolders.addAll(this.blockholderRepository.findAllByEKycAdditionalInfoId(additionalInfo.getId()));
        }
        log.info("{}, blockHolders: {}", prefixLog, CommonUtil.objectToStringJsonIgnoreError(blockHolders));

        List<EContractField> dataRaw = templateData.getDatas().get(0);
        List<EContractField> dataBuild = new ArrayList<>();
        for (EContractField item : dataRaw) {
            dataBuild.add(buildField(item, ekyc, defaultFields.getDatas(), bankList, additionalInfo, publicCoops, blockHolders, refId));
        }
        inputData.setDatas(Collections.singletonList(dataBuild));

        return inputData;
    }

    private EContractField buildField(
        EContractField field,
        EKyc ekyc,
        Datas dfv,
        List<EKycBankList> bankList,
        EKycAdditionalInfo additionalInfo,
        List<PublicCoop> publicCoops,
        List<Blockholder> blockHolders,
        String refId
    ) throws Exception {
        int i = Constants.EContract.PREFIX_ACC_NUM.length();
        String conditionVal = StringUtils.equals(field.getOwner(), Constants.EContract.REQUESTER) ? field.getName() : field.getId();
        switch (conditionVal) {
            case Constants.EContract.vEnvName:
                field.setValue(MessageFormat.format(Constants.EContract.envNameValuePattern, ekyc.getAccountNumber(), ekyc.getFullName()));
                break;
            case Constants.EContract.vEnvNo:
                field.setValue(dfv.getDfvEnvNo());
                break;
            case Constants.EContract.vEnvDate:
                field.setValue(ekyc.getCreatedAt().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DMY)));
                break;
            case Constants.EContract.vEnvSubmittedFrom:
                field.setValue(dfv.getDfvEnvSubmittedFrom());
                break;
            case Constants.EContract.vP001:
                field.setValue(dfv.getDfvP_001());
                break;
            case Constants.EContract.vP002:
                field.setValue(dfv.getDfvP_002());
                break;
            case Constants.EContract.vlk1:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 1));
                break;
            case Constants.EContract.vllk2:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 2));
                break;
            case Constants.EContract.vlk3:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 3));
                break;
            case Constants.EContract.vlk4:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 4));
                break;
            case Constants.EContract.vlk5:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 5));
                break;
            case Constants.EContract.vlk6:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 6));
                break;
            case Constants.EContract.vtt1:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 1));
                break;
            case Constants.EContract.vtt2:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 2));
                break;
            case Constants.EContract.vtt3:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 3));
                break;
            case Constants.EContract.vtt4:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 4));
                break;
            case Constants.EContract.vtt5:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 5));
                break;
            case Constants.EContract.vtt6:
                field.setValue(getCharByIndex(ekyc.getAccountNumber(), i, 6));
                break;
            case Constants.EContract.vkq1:
                field.setValue(ekyc.getIsMargin() ? getCharByIndex(ekyc.getAccountNumber(), i, 1) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vkq2:
                field.setValue(ekyc.getIsMargin() ? getCharByIndex(ekyc.getAccountNumber(), i, 2) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vkq3:
                field.setValue(ekyc.getIsMargin() ? getCharByIndex(ekyc.getAccountNumber(), i, 3) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vkq4:
                field.setValue(ekyc.getIsMargin() ? getCharByIndex(ekyc.getAccountNumber(), i, 4) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vkq5:
                field.setValue(ekyc.getIsMargin() ? getCharByIndex(ekyc.getAccountNumber(), i, 5) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vkq6:
                field.setValue(ekyc.getIsMargin() ? getCharByIndex(ekyc.getAccountNumber(), i, 6) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vdate:
                field.setValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DMY)));
                break;
            case Constants.EContract.vFullname:
                field.setValue(ekyc.getFullName());
                break;
            case Constants.EContract.vNationlity:
                field.setValue(ekyc.getNationality());
                break;
            case Constants.EContract.vDateOfBirth:
                field.setValue(ekyc.getBirthDay().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DMY)));
                break;
            case Constants.EContract.vGenderMale:
                field.setValue(
                    StringUtils.equals(ekyc.getGender(), Constants.EContract.GENDER_MALE)
                        ? Constants.EContract.DEFAULT_X_VAL
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vGenderFemale:
                field.setValue(
                    StringUtils.equals(ekyc.getGender(), Constants.EContract.GENDER_FEMALE)
                        ? Constants.EContract.DEFAULT_X_VAL
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vIdNumber:
                field.setValue(ekyc.getIdentifierId());
                break;
            case Constants.EContract.vIssueDate:
                field.setValue(ekyc.getIssueDate().format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DMY)));
                break;
            case Constants.EContract.vIssueOrganization:
                if (StringUtils.contains(ekyc.getIssuePlace(), Constants.EContract.ISSUE_PLACE_CCSQLHCVTTXH)) {
                    field.setValue(dfv.getDfvIssueOrganization_CCSQLHCVTTXH());
                } else if (StringUtils.contains(ekyc.getIssuePlace(), Constants.EContract.ISSUE_PLACE_CCSDKQLCTVDLQGVDC)) {
                    field.setValue(dfv.getDfvIssueOrganization_CCSDKQLCTVDLQGVDC());
                } else {
                    field.setValue(ekyc.getIssuePlace());
                }
                break;
            case Constants.EContract.vHomeAddress:
                field.setValue(ekyc.getPermanentAddress());
                break;
            case Constants.EContract.vContactAddress:
                field.setValue(ekyc.getContactAddress());
                break;
            case Constants.EContract.vPhoneNumber:
                field.setValue(ekyc.getPhoneNo());
                break;
            case Constants.EContract.vEmail:
                field.setValue(ekyc.getEmail());
                break;
            case Constants.EContract.vTaxCode:
                field.setValue(ekyc.getTaxNumber());
                break;
            case Constants.EContract.vNhsvRepresentative:
                field.setValue(dfv.getDfvNhsvRepresentative());
                break;
            case Constants.EContract.vNhsvRepresentativePotition:
                field.setValue(dfv.getDfvNhsvRepresentativePotition());
                break;
            case Constants.EContract.vAuthorizationDocNo:
                field.setValue(dfv.getDfvAuthorizationDocNo());
                break;
            case Constants.EContract.vAuthorizationDate:
                field.setValue(dfv.getDfvAuthorizationDate());
                break;
            case Constants.EContract.vAccountTypeA:
                field.setValue(dfv.getDfvAccountTypeA());
                break;
            case Constants.EContract.vAccountTypeB:
                field.setValue(dfv.getDfvAccountTypeB());
                break;
            case Constants.EContract.vAccountTypeC:
                field.setValue(dfv.getDfvAccountTypeC());
                break;
            case Constants.EContract.vAccountMarginYes:
                field.setValue(ekyc.getIsMargin() ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY);
                break;
            case Constants.EContract.vAccountMarginNo:
                field.setValue(ekyc.getIsMargin() ? StringUtils.EMPTY : Constants.EContract.DEFAULT_X_VAL);
                break;
            case Constants.EContract.vTradeOnlineYes:
                field.setValue(ekyc.getOnlineTrading() ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY);
                break;
            case Constants.EContract.vTradeOnlineNo:
                field.setValue(ekyc.getOnlineTrading() ? StringUtils.EMPTY : Constants.EContract.DEFAULT_X_VAL);
                break;
            case Constants.EContract.vAuthenticationOtp:
                field.setValue(
                    StringUtils.equals(ekyc.getAuthenMethod(), Constants.EContract.OTP)
                        ? Constants.EContract.DEFAULT_X_VAL
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vAuthenticationToken:
                field.setValue(
                    StringUtils.equals(ekyc.getAuthenMethod(), Constants.EContract.TOKEN)
                        ? Constants.EContract.DEFAULT_X_VAL
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vAdvanceMoneyYes:
                field.setValue(ekyc.getAdvancedCashIncluded() ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY);
                break;
            case Constants.EContract.vAdvanceMoneyNo:
                field.setValue(ekyc.getAdvancedCashIncluded() ? StringUtils.EMPTY : Constants.EContract.DEFAULT_X_VAL);
                break;
            case Constants.EContract.vSmsReceiveYes:
                field.setValue(
                    StringUtils.equals(ekyc.getSmsMethod(), Constants.EContract.ADVANCED)
                        ? Constants.EContract.DEFAULT_X_VAL
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vSmsReceiveNo:
                field.setValue(
                    StringUtils.equals(ekyc.getSmsMethod(), Constants.EContract.BASIC)
                        ? Constants.EContract.DEFAULT_X_VAL
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vBeneficiary1:
                field.setValue(bankList.get(0).getOwnerName());
                break;
            case Constants.EContract.vBankAccount1:
                field.setValue(bankList.get(0).getBankAccNo());
                break;
            case Constants.EContract.vBankName1:
                field.setValue(bankList.get(0).getBankName());
                break;
            case Constants.EContract.vBeneficiary2:
                field.setValue(bankList.size() > 1 ? convertStringIgnoreBlank(bankList.get(1).getOwnerName()) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vBankAccount2:
                field.setValue(bankList.size() > 1 ? convertStringIgnoreBlank(bankList.get(1).getBankAccNo()) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vBankName2:
                field.setValue(bankList.size() > 1 ? convertStringIgnoreBlank(bankList.get(1).getBankName()) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vBeneficiary3:
                field.setValue(bankList.size() > 2 ? convertStringIgnoreBlank(bankList.get(2).getOwnerName()) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vBankAccount3:
                field.setValue(bankList.size() > 2 ? convertStringIgnoreBlank(bankList.get(2).getBankAccNo()) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vBankName3:
                field.setValue(bankList.size() > 2 ? convertStringIgnoreBlank(bankList.get(2).getBankName()) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vBoFullname:
                field.setValue(Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getFullName()));
                break;
            case Constants.EContract.vBoDateOfBirth:
                field.setValue(
                    Objects.isNull(additionalInfo) || Objects.isNull(additionalInfo.getBirthDay())
                        ? StringUtils.EMPTY
                        : LocalDate
                            .parse(additionalInfo.getBirthDay(), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyyMMdd))
                            .format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DMY))
                );
                break;
            case Constants.EContract.vBoNationality:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getNationality())
                );
                break;
            case Constants.EContract.vBoIdNumber:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getIdentifierId())
                );
                break;
            case Constants.EContract.vBoIssueDate:
                field.setValue(
                    Objects.isNull(additionalInfo)
                        ? StringUtils.EMPTY
                        : Util.strDateToStringDateFormat(
                            convertStringIgnoreBlank(additionalInfo.getIssueDate()),
                            Constants.DATE_FORMAT_yyyyMMdd,
                            Constants.DATE_FORMAT_DMY
                        )
                );
                break;
            case Constants.EContract.vBoIssueOrganization:
                if (Objects.isNull(additionalInfo) || Objects.isNull(additionalInfo.getIssuePlace())) {
                    field.setValue(StringUtils.EMPTY);
                } else {
                    if (StringUtils.contains(ekyc.getIssuePlace(), Constants.EContract.ISSUE_PLACE_CCSQLHCVTTXH)) {
                        field.setValue(dfv.getDfvIssueOrganization_CCSQLHCVTTXH());
                    } else if (StringUtils.contains(ekyc.getIssuePlace(), Constants.EContract.ISSUE_PLACE_CCSDKQLCTVDLQGVDC)) {
                        field.setValue(dfv.getDfvIssueOrganization_CCSDKQLCTVDLQGVDC());
                    } else {
                        field.setValue(additionalInfo.getIssuePlace());
                    }
                }
                break;
            case Constants.EContract.vBoHomeAddress:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getPermanentAddress())
                );
                break;
            case Constants.EContract.vBoContractAddress:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getContactAddress())
                );
                break;
            case Constants.EContract.vBoJob:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getOccupation())
                );
                break;
            case Constants.EContract.vBoPosition:
                field.setValue(Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getPosition()));
                break;
            case Constants.EContract.vBoPhoneNumber:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getPhoneNumber())
                );
                break;
            case Constants.EContract.vBoVisaNumber:
                field.setValue(Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getVisaNo()));
                break;
            case Constants.EContract.vBoVisaIssueOrganization:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getVisaIssuePlace())
                );
                break;
            case Constants.EContract.vBoForeignResidence:
                field.setValue(
                    Objects.isNull(additionalInfo) ? StringUtils.EMPTY : convertStringIgnoreBlank(additionalInfo.getForeignResidence())
                );
                break;
            case Constants.EContract.vEmployee:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.ONE) ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vAcquaintance:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.TWO) ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vAds:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.THREE) ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vOthers:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.FOUR) ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vYes:
                field.setValue(ekyc.getCustomerSupport() ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY);
                break;
            case Constants.EContract.vNo:
                field.setValue(ekyc.getCustomerSupport() ? StringUtils.EMPTY : Constants.EContract.DEFAULT_X_VAL);
                break;
            case Constants.EContract.vInvestmentGoalLongTerm:
                field.setValue(
                    Objects.isNull(additionalInfo) || !StringUtils.equals(additionalInfo.getInvestmentGoal(), Constants.EContract.LONG_TERM)
                        ? StringUtils.EMPTY
                        : Constants.EContract.DEFAULT_X_VAL
                );
                break;
            case Constants.EContract.vInvestmentGoalMidTerm:
                field.setValue(
                    Objects.isNull(additionalInfo) || !StringUtils.equals(additionalInfo.getInvestmentGoal(), Constants.EContract.MID_TERM)
                        ? StringUtils.EMPTY
                        : Constants.EContract.DEFAULT_X_VAL
                );
                break;
            case Constants.EContract.vInvestmentGoalShortTerm:
                field.setValue(
                    Objects.isNull(additionalInfo) ||
                        !StringUtils.equals(additionalInfo.getInvestmentGoal(), Constants.EContract.SHORT_TERM)
                        ? StringUtils.EMPTY
                        : Constants.EContract.DEFAULT_X_VAL
                );
                break;
            case Constants.EContract.vRiskLow:
                field.setValue(
                    Objects.isNull(additionalInfo) || !StringUtils.equals(additionalInfo.getRisk(), Constants.EContract.LOW)
                        ? StringUtils.EMPTY
                        : Constants.EContract.DEFAULT_X_VAL
                );
                break;
            case Constants.EContract.vRiskNormal:
                field.setValue(
                    Objects.isNull(additionalInfo) || !StringUtils.equals(additionalInfo.getRisk(), Constants.EContract.NORMAL)
                        ? StringUtils.EMPTY
                        : Constants.EContract.DEFAULT_X_VAL
                );
                break;
            case Constants.EContract.vRiskHigh:
                field.setValue(
                    Objects.isNull(additionalInfo) || !StringUtils.equals(additionalInfo.getRisk(), Constants.EContract.HIGH)
                        ? StringUtils.EMPTY
                        : Constants.EContract.DEFAULT_X_VAL
                );
                break;
            case Constants.EContract.vExperienceYes:
                field.setValue(
                    Objects.isNull(additionalInfo) || Objects.isNull(additionalInfo.getExperienced()) || !additionalInfo.getExperienced()
                        ? StringUtils.EMPTY
                        : Constants.EContract.DEFAULT_X_VAL
                );
                break;
            case Constants.EContract.vExperienceNo:
                field.setValue(
                    Objects.isNull(additionalInfo) || Objects.isNull(additionalInfo.getExperienced()) || !additionalInfo.getExperienced()
                        ? Constants.EContract.DEFAULT_X_VAL
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vInternalCompanyName1:
                field.setValue(
                    CollectionUtils.isEmpty(publicCoops) ? StringUtils.EMPTY : convertStringIgnoreBlank(publicCoops.get(0).getCompanyName())
                );
                break;
            case Constants.EContract.vInternalStock1:
                field.setValue(
                    CollectionUtils.isEmpty(publicCoops) ? StringUtils.EMPTY : convertStringIgnoreBlank(publicCoops.get(0).getStock())
                );
                break;
            case Constants.EContract.vInternalPosition1:
                field.setValue(
                    CollectionUtils.isEmpty(publicCoops) ? StringUtils.EMPTY : convertStringIgnoreBlank(publicCoops.get(0).getPosition())
                );
                break;
            case Constants.EContract.vInternalCompanyName2:
                field.setValue(
                    CollectionUtils.isEmpty(publicCoops)
                        ? StringUtils.EMPTY
                        : publicCoops.size() > 1 ? convertStringIgnoreBlank(publicCoops.get(1).getCompanyName()) : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vInternalStock2:
                field.setValue(
                    CollectionUtils.isEmpty(publicCoops)
                        ? StringUtils.EMPTY
                        : publicCoops.size() > 1 ? convertStringIgnoreBlank(publicCoops.get(1).getStock()) : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vInternalPosition2:
                field.setValue(
                    CollectionUtils.isEmpty(publicCoops)
                        ? StringUtils.EMPTY
                        : publicCoops.size() > 1 ? convertStringIgnoreBlank(publicCoops.get(1).getPosition()) : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vOwnCompanyName1:
                field.setValue(
                    CollectionUtils.isEmpty(blockHolders)
                        ? StringUtils.EMPTY
                        : convertStringIgnoreBlank(blockHolders.get(0).getCompanyName())
                );
                break;
            case Constants.EContract.vOwnStock1:
                field.setValue(
                    CollectionUtils.isEmpty(blockHolders) ? StringUtils.EMPTY : convertStringIgnoreBlank(blockHolders.get(0).getStock())
                );
                break;
            case Constants.EContract.vOwnPosition1:
                field.setValue(
                    CollectionUtils.isEmpty(blockHolders) ? StringUtils.EMPTY : convertStringIgnoreBlank(blockHolders.get(0).getPosition())
                );
                break;
            case Constants.EContract.vOwnCompanyName2:
                field.setValue(
                    CollectionUtils.isEmpty(blockHolders)
                        ? StringUtils.EMPTY
                        : blockHolders.size() > 1 ? convertStringIgnoreBlank(blockHolders.get(1).getCompanyName()) : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vOwnStock2:
                field.setValue(
                    CollectionUtils.isEmpty(blockHolders)
                        ? StringUtils.EMPTY
                        : blockHolders.size() > 1 ? convertStringIgnoreBlank(blockHolders.get(1).getStock()) : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vOwnPosition2:
                field.setValue(
                    CollectionUtils.isEmpty(blockHolders)
                        ? StringUtils.EMPTY
                        : blockHolders.size() > 1 ? convertStringIgnoreBlank(blockHolders.get(1).getPosition()) : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vFatcaA:
                field.setValue(ekyc.getFatca() ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY);
                break;
            case Constants.EContract.vFatcaB:
                field.setValue(ekyc.getFatca() ? Constants.EContract.DEFAULT_X_VAL : StringUtils.EMPTY);
                break;
            case Constants.EContract.vFatcaC:
                field.setValue(ekyc.getFatca() ? StringUtils.EMPTY : Constants.EContract.DEFAULT_X_VAL);
                break;
            case Constants.EContract.vContractNumber:
                field.setValue(ekyc.getContractNo());
                break;
            case Constants.EContract.vAcquaintanceName:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.TWO)
                        ? convertStringIgnoreBlank(ekyc.getPartnerName())
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vCaregiverName:
                field.setValue(ekyc.getCustomerSupport() ? convertStringIgnoreBlank(ekyc.getCsName()) : StringUtils.EMPTY);
                break;
            case Constants.EContract.vEmployeeName:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.ONE)
                        ? convertStringIgnoreBlank(ekyc.getPartnerName())
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vAdsName:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.THREE)
                        ? convertStringIgnoreBlank(ekyc.getPartnerName())
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vOthersName:
                field.setValue(
                    Objects.equals(ekyc.getReferral(), Constants.EContract.FOUR)
                        ? convertStringIgnoreBlank(ekyc.getPartnerName())
                        : StringUtils.EMPTY
                );
                break;
            case Constants.EContract.vDueDays:
                field.setValue(dfv.getDfvDueDays());
                break;
            case Constants.EContract.vRefId:
                field.setValue(refId);
                break;
            default:
                log.info("not supported field: {}", field);
                break;
        }

        buildFieldSameId(field, ekyc, dfv);

        return field;
    }

    private void buildFieldSameId(EContractField field, EKyc eKyc, Datas dfv) {
        if (
            StringUtils.equals(field.getId(), Constants.EContract.vP001R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vNameRecipient)
        ) {
            field.setValue(dfv.getDfvP_001_r_001_name_recipient());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP001R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vMailRecipient)
        ) {
            field.setValue(dfv.getDfvP_001_r_001_mail_recipient());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP001R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vPhoneRecipient)
        ) {
            field.setValue(dfv.getDfvP_001_r_001_phone_recipient());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP001R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vContactRecipient)
        ) {
            field.setValue(dfv.getDfvP_001_r_001_contact_recipient());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP002R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vNameRecipient)
        ) {
            field.setValue(eKyc.getFullName());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP002R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vMailRecipient)
        ) {
            field.setValue(eKyc.getEmail());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP002R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vPhoneRecipient)
        ) {
            field.setValue(eKyc.getPhoneNo());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP002R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vContactRecipient)
        ) {
            field.setValue(eKyc.getIdentifierId());
        } else if (
            StringUtils.equals(field.getId(), Constants.EContract.vP002R001) &&
            StringUtils.equals(field.getName(), Constants.EContract.vApplicationFormRecipient)
        ) {
            field.setValue(dfv.getDfvP_002_r_001_applicationForm_recipient());
        }
    }

    private String getCharByIndex(String str, int rootIndex, int position) {
        return StringUtils.substring(str, rootIndex + position - 1, rootIndex + position);
    }

    private String setField(String input) {
        return StringUtils.equals(input, Constants.EContract.NULL_VALUE) ? null : input;
    }

    private String base64OnlineFile(String imageUrl) throws IOException {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    private String convertStringIgnoreBlank(String input) {
        return StringUtils.isBlank(input) ? StringUtils.EMPTY : input;
    }
}
