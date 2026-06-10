package com.techx.tradex.ekycadmin.service;

import com.difisoft.redis.CoordinatorService;
import com.difisoft.redis.RedisDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.InvalidFormatException;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.consumers.BroadcastHandler;
import com.techx.tradex.ekycadmin.domain.Blockholder;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.EKycAdditionalInfo;
import com.techx.tradex.ekycadmin.domain.EKycBankList;
import com.techx.tradex.ekycadmin.domain.EKycCreatorStatus;
import com.techx.tradex.ekycadmin.domain.EKycExt;
import com.techx.tradex.ekycadmin.domain.PublicCoop;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import com.techx.tradex.ekycadmin.models.request.EKycAddReq;
import com.techx.tradex.ekycadmin.models.request.InternalGetEKycRequest;
import com.techx.tradex.ekycadmin.models.request.VNPTDataBase64;
import com.techx.tradex.ekycadmin.models.response.EKycAddRes;
import com.techx.tradex.ekycadmin.repository.BlockholderRepository;
import com.techx.tradex.ekycadmin.repository.CustomEKycRepository;
import com.techx.tradex.ekycadmin.repository.EKycAdditionalInfoRepository;
import com.techx.tradex.ekycadmin.repository.EKycBankListRepository;
import com.techx.tradex.ekycadmin.repository.EKycExtRepository;
import com.techx.tradex.ekycadmin.repository.PublicCoopRepository;
import com.techx.tradex.ekycadmin.utils.CommonUtil;
import com.techx.tradex.ekycadmin.utils.Util;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.zalando.problem.Problem;

import java.io.IOException;
import java.security.PublicKey;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link EKyc}.
 */
@Service
@Transactional
public class CustomEKycService {

    private static final Logger log = LoggerFactory.getLogger(CustomEKycService.class);

    private final CustomEKycRepository customEKycRepo;
    private final EKycExtRepository eKycExtRepository;
    private final TtlOpenAccountService ttlOpenAccountService;
    private final EmailService emailService;
    private final AppConf appConf;
    private final LotteEKycService lotteEKycService;
    private final ObjectMapper objectMapper;
    private final EKycBankListRepository eKycBankListRepository;
    private final EKycAdditionalInfoRepository ekycAdditionalInfoRepository;
    private final BlockholderRepository blockholderRepository;
    private final PublicCoopRepository publicCoopRepository;
    private final CoordinatorService coordinatorService;
    private final EContractCustomService eContractCustomService;
    private final EKycAccountNumberService eKycAccountNumberService;
    private final RedisDao redisDao;

    @Autowired
    public CustomEKycService(
        TtlOpenAccountService ttlOpenAccountService,
        BroadcastHandler broadcastHandler,
        CustomEKycRepository customEKycRepo,
        EKycExtRepository eKycExtRepository,
        EmailService emailService,
        AppConf appConf,
        LotteEKycService lotteEKycService,
        ObjectMapper objectMapper,  
        EKycBankListRepository eKycBankListRepository,
        EKycAdditionalInfoRepository ekycAdditionalInfoRepository,
        BlockholderRepository blockholderRepository,
        PublicCoopRepository publicCoopRepository,
        CoordinatorService coordinatorService,
        EContractCustomService eContractCustomService,
        EKycAccountNumberService eKycAccountNumberService,
        RedisDao redisDao
    ) {
        this.ttlOpenAccountService = ttlOpenAccountService;
        this.customEKycRepo = customEKycRepo;
        this.eKycExtRepository = eKycExtRepository;
        this.emailService = emailService;
        this.appConf = appConf;
        this.lotteEKycService = lotteEKycService;
        this.objectMapper = objectMapper;
        this.eKycBankListRepository = eKycBankListRepository;
        this.ekycAdditionalInfoRepository = ekycAdditionalInfoRepository;
        this.blockholderRepository = blockholderRepository;
        this.publicCoopRepository = publicCoopRepository;
        this.coordinatorService = coordinatorService;
        this.eContractCustomService = eContractCustomService;
        this.eKycAccountNumberService = eKycAccountNumberService;
        this.redisDao = redisDao;
    }

    @Transactional
    public void approve(Long ekycId) {
        EKyc ekyc = customEKycRepo.findById(ekycId).orElseThrow(() -> new BadRequestAlertException("EKYC not exist", "EKyc", "notexist"));
        if (!ekyc.getStatus().equals(Status.PENDING)) {
            throw new BadRequestAlertException("Invalid status", "EKyc", "invalidstatus");
        }
        ekyc.setStatus(Status.APPROVED);
        ekyc.setUpdatedAt(ZonedDateTime.now());
        customEKycRepo.save(ekyc);
    }

    @Transactional
    public EKycCreatorStatus approveAndCreate(Long ekycId) {
        EKyc ekyc = customEKycRepo.findById(ekycId).orElseThrow(() -> new BadRequestAlertException("EKYC not exist", "EKyc", "notexist"));
        if (!ekyc.getStatus().equals(Status.PENDING)) {
            throw new BadRequestAlertException("Invalid status", "EKyc", "invalidstatus");
        }
        EKycCreatorStatus eKycCreatorStatus = ttlOpenAccountService.openAccountTTLNoAsync(ekycId, false, false);
        if (eKycCreatorStatus == null || TtlOpenAccountService.SUCCESS_STATUS.equals(eKycCreatorStatus.getStatus())) {
            ekyc.setStatus(Status.APPROVED);
            ekyc.setUpdatedAt(ZonedDateTime.now());
            customEKycRepo.save(ekyc);
        }
        return eKycCreatorStatus;
    }

    public EKycAddRes addEKyc(String txId, EKycAddReq req) throws Exception {
        log.info("{} add ekyc {}", txId, req);
        try {
            String acquireKey = this.coordinatorService.acquire(req.getEKycId(), appConf.getNodeId(), 60000);
            if (StringUtils.isEmpty(acquireKey)) {
                log.info("{} will wait for {} to finish", txId, req.getEKycId());
                this.coordinatorService.waitForResult(req.getEKycId());
            }
            if (appConf.getCore().equals("lotte")) {
                this.validRequestEkycNHSV(req, txId);
            }
            if (appConf.isEnableRequireGender() && StringUtils.isEmpty(req.getGender())) {
                throw new GeneralException("GENDER_IS_REQUIRED");
            }
            Pattern pattern = appConf.getAddressRegexPattern();
            if (pattern != null && !appConf.getCore().equals("lotte")) {
                if (!pattern.matcher(req.getAddress()).matches()) {
                    throw new GeneralException("INVALID_ADDRESS_VALUE");
                }
            }
            if (StringUtils.isNotBlank(req.getReferrerIdName())) {
                req.setReferrerIdName(req.getReferrerIdName().trim().toUpperCase());
                if (!req.getReferrerIdName().matches("^[/.0-9A-Z]{1,16}$")) {
                    throw new InvalidValueException("referrerIdName");
                }
            }
            if (StringUtils.isNotBlank(req.getBankName()) && StringUtils.isBlank(req.getAccountName())) {
                req.setAccountName(req.getFullName());
            }
            if (
                !appConf.getCore().equals("lotte") &&
                    !(
                        StringUtils.isNotBlank(req.getBankName()) == StringUtils.isNotBlank(req.getAccountName()) &&
                            StringUtils.isNotBlank(req.getBankName()) == StringUtils.isNotBlank(req.getBranch()) &&
                            StringUtils.isNotBlank(req.getBankName()) == StringUtils.isNotBlank(req.getBankAccount())
                    )
            ) {
                throw new GeneralException("ALL_BANK_INFORMATION_MUST_BE_INPUT");
            }

            if (StringUtils.isNotBlank(req.getBankAccount())) {
                req.setBankAccount(req.getBankAccount().trim());
                if (!req.getBankAccount().matches("^[0-9a-zA-Z]{1,32}$")) {
                    throw new InvalidFormatException("bankAccount");
                }
            }
            List<EKyc> existedEKycs = new ArrayList<>();
            if (appConf.getCore().equals("lotte")) {
                existedEKycs = customEKycRepo.findByIdentifierId(req.getIdentifierId());
            } else {
                existedEKycs = customEKycRepo.findByIdentifierIdOrPhoneNo(req.getIdentifierId(), req.getPhoneNo());
            }
            List<EKyc> existedPendingEKycs = new ArrayList<>();
            existedEKycs.forEach(
                e -> {
                    if (e.getStatus() == Status.APPROVED) {
                        if (appConf.getCallCoreNoAsync()) {
                            throw new GeneralException(Constants.ACCOUNT_CREATED);
                        } else {
                            throw new GeneralException(Constants.EKYC_ALREADY_EXISTED);
                        }
                    } else if (e.getStatus() == Status.PENDING) {
                        existedPendingEKycs.add(e);
                    } else if (e.getStatus() == Status.WAITING_CONFIRMATION) {
                        throw new GeneralException(Constants.EXISTED_WAITING_CONFIRMATION);
                    }
                }
            );
            existedPendingEKycs.sort(Comparator.comparing(EKyc::getId));
            EKyc eKyc = req.toEKyc(appConf.getDefaulltIdCardExpiredTime());
            if (existedPendingEKycs.size() >= 1) {
                customEKycRepo.deleteAll(existedPendingEKycs.subList(1, existedPendingEKycs.size()));
                eKyc.setId(existedPendingEKycs.get(0).getId());
                log.warn(
                    "{} found duplicate ekyc {}. update on {}",
                    txId,
                    existedPendingEKycs.stream().map(EKyc::getId).collect(Collectors.toList()),
                    eKyc.getId()
                );
            }
            customEKycRepo.save(eKyc);

            log.debug("{} -- req.getHeaders(): {}", txId, CommonUtil.objectToStringJsonIgnoreError(req.getHeaders()));

            // save session id and ekyc id to redis
            Long sessionId = req.getHeaders().getToken().getRefreshTokenId();
            redisDao.set(Constants.EKYC_SESSION_ID_PREFIX + sessionId, eKyc.geteKycId().toString(), Constants.EKYC_SESSION_ID_EXPIRE_TIME);

            this.saveEKycRegisterBankList(req.getBankList(), eKyc);
            this.saveEKycContract(req.getBeneficiaryOwner(), req.getInvestmentExperience(), eKyc);
            log.warn("{} save ekyc {}-{}-{}", txId, eKyc.getId(), eKyc.getIdentifierId(), eKyc.getPhoneNo());
            if (!StringUtils.isEmpty(req.getLogId()) || !StringUtils.isEmpty(req.getRawData())) {
                EKycExt ext = new EKycExt();
                ext.setEKyc(eKyc);
                ext.setLogId(req.getLogId());
                ext.setRawData(req.getRawData());
                eKycExtRepository.save(ext);
                log.warn("{} save ekyc extend {}-{}-{}", txId, eKyc.getId(), eKyc.getIdentifierId(), eKyc.getPhoneNo());
            }
            if (appConf.getCallCoreNoAsync()) {
                log.info("{} will open account synchronized {}-{}-{}", txId, eKyc.getId(), eKyc.getIdentifierId(), eKyc.getPhoneNo());
                if (StringUtils.equals(appConf.getCore(), "lotte")) {
                    lotteEKycService.lotteEKycNoAsync(eKyc, req, true, appConf.getCheckMatchingRate(), txId);
                    EKycAddRes res = new EKycAddRes();
                    res.setStatus("success");
                    log.info("{} start updateAccountNumberInfo {}", txId, eKyc.getId());
                    TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                lotteEKycService.callUploadImage(req.getEKycId(), Constants.FRONT_IMG_KIND, req.getFrontImageUrl(), eKyc.getIdentifierId(), txId);
                                lotteEKycService.callUploadImage(req.getEKycId(), Constants.BACK_IMG_KIND, req.getBackImageUrl(), eKyc.getIdentifierId(), txId);
                                log.info("{} register updateAccountNumberInfo {}", txId, eKyc.getId());
                                eKycAccountNumberService.updateAccountNumberInfo(eKyc.getId(), txId);
                            }
                        }
                    );
                    log.info("{} EKycAddRes: {}", txId, CommonUtil.objectToStringJsonIgnoreError(res));
                    return res;
                } else {
                    ttlOpenAccountService.openAccountTTLNoAsync(eKyc.getId(), true, true);
                }
            } else {
                TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            log.info(
                                "{} will open account after commit {}-{}-{}",
                                txId,
                                eKyc.getId(),
                                eKyc.getIdentifierId(),
                                eKyc.getPhoneNo()
                            );
                            switch (appConf.getCore()) {
                                case "lotte":
                                    lotteEKycService.lotteEKycAsync(eKyc.getId(), req, txId);
                                    break;
                                default:
                                    ttlOpenAccountService.openAccountTTL(eKyc.getId());
                            }
                        }
                    }
                );
            }
            return EKycAddRes.fromEKyc(eKyc);
        } finally {
            this.coordinatorService.release(req.getEKycId());
        }
    }

    public void sendApprovedEmail(List<String> emails) {
        try {
            this.emailService.sendApprovedEmail(emails);
        } catch (Exception e) {
            throw Problem
                .builder()
                .withStatus(org.zalando.problem.Status.INTERNAL_SERVER_ERROR)
                .withTitle("Fail to send Approval email")
                .with("message", e.getMessage())
                .build();
        }
    }

    public EKyc internalGetEkyc(InternalGetEKycRequest request) {
        List<EKyc> eKycs = customEKycRepo.findByIdentifierId(request.getIdentifierId());
        if (eKycs.isEmpty()) {
            return new EKyc();
        }
        return eKycs.get(0);
    }

    public void sendRejectedEmail(List<String> emails) {
        try {
            this.emailService.sendRejectedEmail(emails);
        } catch (IOException e) {
            throw Problem
                .builder()
                .withStatus(org.zalando.problem.Status.INTERNAL_SERVER_ERROR)
                .withTitle("Fail to send Rejected email")
                .with("message", e.getMessage())
                .build();
        }
    }

    private void validRequestEkycNHSV(EKycAddReq req, String txId) throws Exception {
        req.validReqAddEKycLotte(appConf.getMaxBankList(), appConf.getMaxPublicCoop(), appConf.getMaxBlockholder());
        byte[] base64Decoded = Util.base64Decode(req.getDataBase64());
        byte[] signature = Util.base64Decode(req.getDataSign());
        PublicKey publicKey = Util.getPublicKey(appConf.getVnpt().getPublicKey());
        boolean verifySignature = false;
        try {
            verifySignature =
                Util.verifySignature(publicKey, req.getDataBase64().getBytes(), signature, appConf.getVnpt().getAlgorithmSignature());
            if (!verifySignature && appConf.getDifisoft().getTesting()) { // cau hinh BA test API
                log.info("{} Try to verify", txId);
                publicKey = Util.getPublicKey(appConf.getDifisoft().getPublicKey());
                verifySignature =
                    Util.verifySignature(
                        publicKey,
                        req.getDataBase64().getBytes(),
                        signature,
                        appConf.getDifisoft().getAlgorithmSignature()
                    );
            }
        } catch (Exception e) {
            log.error("{} Fail to verify signature", txId, e);
            if (appConf.getDifisoft().getTesting()) {
                log.info("{} Retry to verify", txId);
                publicKey = Util.getPublicKey(appConf.getDifisoft().getPublicKey());
                verifySignature =
                    Util.verifySignature(
                        publicKey,
                        req.getDataBase64().getBytes(),
                        signature,
                        appConf.getDifisoft().getAlgorithmSignature()
                    );
            }
        }
        if (!verifySignature) {
            throw new GeneralException("INVALID_SIGNATURE");
        }
        VNPTDataBase64 vnptDataBase64 = objectMapper.readValue(base64Decoded, VNPTDataBase64.class);
        if (
            !req.getIdentifierId().equals(vnptDataBase64.getObject().getId()) ||
                !req.getFullName().equals(vnptDataBase64.getObject().getName())
        ) {
            throw new GeneralException("INVALID_DATA");
        }
    }

    private void saveEKycRegisterBankList(List<EKycAddReq.BankList> bankList, EKyc eKyc) {
        if (bankList != null) {
            List<EKycBankList> eKycRegisterBankLists = bankList
                .stream()
                .map(
                    bank -> {
                        EKycBankList eKycBankList = new EKycBankList();
                        eKycBankList.setEKyc(eKyc);
                        eKycBankList.setBankId(bank.getBankId());
                        eKycBankList.setBankName(bank.getBankName());
                        eKycBankList.setBankAccNo(bank.getBankAccNo());
                        eKycBankList.setOwnerName(bank.getOwnerName());
                        eKycBankList.setBranchId(bank.getBranchId());
                        return eKycBankList;
                    }
                )
                .collect(Collectors.toList());
            this.eKycBankListRepository.saveAll(eKycRegisterBankLists);
        }
    }

    public void saveEKycContract(
        EKycAddReq.BeneficiaryOwner beneficiaryOwner,
        EKycAddReq.InvestmentExperience investmentExperience,
        EKyc eKyc
    ) {
        if (beneficiaryOwner == null && investmentExperience == null) {
            return;
        }
        EKycAdditionalInfo ekycAdditionalInfo = geteKycAdditionalInfo(beneficiaryOwner, investmentExperience, eKyc);
        this.ekycAdditionalInfoRepository.save(ekycAdditionalInfo);
        if (investmentExperience != null && investmentExperience.getPublicCoop() != null) {
            List<PublicCoop> publicCoops = investmentExperience
                .getPublicCoop()
                .stream()
                .map(
                    item -> {
                        PublicCoop publicCoop = new PublicCoop();
                        publicCoop.setEKycAdditionalInfo(ekycAdditionalInfo);
                        publicCoop.setStock(item.getStock());
                        publicCoop.setPosition(item.getPosition());
                        publicCoop.setCompanyName(item.getCompanyName());
                        return publicCoop;
                    }
                )
                .collect(Collectors.toList());
            this.publicCoopRepository.saveAll(publicCoops);
        }
        if (investmentExperience != null && investmentExperience.getBlockholder() != null) {
            List<Blockholder> blockholders = investmentExperience
                .getBlockholder()
                .stream()
                .map(
                    item -> {
                        Blockholder blockholder = new Blockholder();
                        blockholder.setEKycAdditionalInfo(ekycAdditionalInfo);
                        blockholder.setStock(item.getStock());
                        blockholder.setPosition(item.getPosition());
                        blockholder.setCompanyName(item.getCompanyName());
                        return blockholder;
                    }
                )
                .collect(Collectors.toList());
            this.blockholderRepository.saveAll(blockholders);
        }
    }

    private static EKycAdditionalInfo geteKycAdditionalInfo(
        EKycAddReq.BeneficiaryOwner beneficiaryOwner,
        EKycAddReq.InvestmentExperience investmentExperience,
        EKyc eKyc
    ) {
        EKycAdditionalInfo ekycAdditionalInfo = new EKycAdditionalInfo();
        if (beneficiaryOwner != null) {
            ekycAdditionalInfo.setFullName(beneficiaryOwner.getFullName());
            ekycAdditionalInfo.setBirthDay(beneficiaryOwner.getBirthDay());
            ekycAdditionalInfo.setNationality(beneficiaryOwner.getNationality());
            ekycAdditionalInfo.setIdentifierId(beneficiaryOwner.getIdentifierId());
            ekycAdditionalInfo.setIssueDate(beneficiaryOwner.getIssueDate());
            ekycAdditionalInfo.setIssuePlace(beneficiaryOwner.getIssuePlace());
            ekycAdditionalInfo.setPermanentAddress(beneficiaryOwner.getPermanentAddress());
            ekycAdditionalInfo.setContactAddress(beneficiaryOwner.getContactAddress());
            ekycAdditionalInfo.setOccupation(beneficiaryOwner.getOccupation());
            ekycAdditionalInfo.setPosition(beneficiaryOwner.getPosition());
            ekycAdditionalInfo.setPhoneNumber(beneficiaryOwner.getPhoneNumber());
            ekycAdditionalInfo.setVisaNo(beneficiaryOwner.getVisaNo());
            ekycAdditionalInfo.setVisaIssuePlace(beneficiaryOwner.getVisaIssuePlace());
            ekycAdditionalInfo.setForeignResidence(beneficiaryOwner.getForeignResidence());
        }
        if (investmentExperience != null) {
            ekycAdditionalInfo.setInvestmentGoal(StringUtils.isNotBlank(investmentExperience.getInvestmentGoal())
                ? investmentExperience.getInvestmentGoal()
                : null);
            ekycAdditionalInfo.setRisk(StringUtils.isNotBlank(investmentExperience.getRisk())
                ? investmentExperience.getRisk()
                : null);
            ekycAdditionalInfo.setExperienced(investmentExperience.getExperienced());
        }
        ekycAdditionalInfo.setEKyc(eKyc);
        return ekycAdditionalInfo;
    }
}
