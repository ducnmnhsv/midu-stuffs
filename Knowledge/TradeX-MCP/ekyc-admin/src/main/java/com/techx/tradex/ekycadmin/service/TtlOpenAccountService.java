package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.utils.Pair;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.EKycCreatorStatus;
import com.techx.tradex.ekycadmin.domain.TtlIssuePlaceCodeMap;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import com.techx.tradex.ekycadmin.models.ttl.OpenAccountReq;
import com.techx.tradex.ekycadmin.models.ttl.OpenAccountRes;
import com.techx.tradex.ekycadmin.repository.EKycCreatorStatusRepository;
import com.techx.tradex.ekycadmin.repository.EKycRepository;
import com.techx.tradex.ekycadmin.repository.TtlIssuePlaceCodeMapRepository;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

class RegexTtlIssuePlaceCodeMap {

    String regex;
    String code;
    Pattern pattern;

    RegexTtlIssuePlaceCodeMap from(TtlIssuePlaceCodeMap it) {
        this.regex = it.getName();
        this.code = it.getCode();
        this.pattern = Pattern.compile(this.regex, Pattern.UNICODE_CHARACTER_CLASS);
        return this;
    }

    public String match(String name) {
        if (this.pattern.matcher(name).matches()) {
            return this.code;
        }
        return null;
    }
}

@Service
public class TtlOpenAccountService {

    private static final Logger log = LoggerFactory.getLogger(TtlOpenAccountService.class);
    static final String PENDING_STATUS = "PENDING";
    static final String FAIL_STATUS = "FAIL";
    public static final String SUCCESS_STATUS = "SUCCESS";

    private final AppConf appConf;
    private final EKycRepository eKycRepository;
    private final TtlIssuePlaceCodeMapRepository ttlIssuePlaceCodeMapRepository;
    private final EKycCreatorStatusRepository ekycCreatorStatusRepository;
    private final TTLApiService ttlApiService;
    private final TTlBankService tTlBankService;

    //    private final Collator comparator = Collator.getInstance(new Locale("vi", "VN"));

    private List<Pair<String, String>> extractMatch = new ArrayList<>();
    private List<RegexTtlIssuePlaceCodeMap> regexMatch = new ArrayList<>();

    private AtomicBoolean isRunningInit = new AtomicBoolean(false);

    @Autowired
    public TtlOpenAccountService(
        AppConf appConf,
        EKycRepository eKycRepository,
        TtlIssuePlaceCodeMapRepository ttlIssuePlaceCodeMapRepository,
        EKycCreatorStatusRepository ekycCreatorStatusRepository,
        TTLApiService ttlApiService,
        TTlBankService tTlBankService
    ) {
        this.appConf = appConf;
        this.eKycRepository = eKycRepository;
        this.ttlIssuePlaceCodeMapRepository = ttlIssuePlaceCodeMapRepository;
        this.ekycCreatorStatusRepository = ekycCreatorStatusRepository;
        this.ttlApiService = ttlApiService;
        this.tTlBankService = tTlBankService;
    }

    @Scheduled(cron = "${app.schedulers.ttlOpenAccountReload}")
    public void initScheduler() {
        if (appConf.getCore() == null || appConf.getCore().equals("ttl")) {
            this.init();
        }
    }

    @PostConstruct
    public void init() {
        if (this.isRunningInit.get()) {
            return;
        }
        this.isRunningInit.set(true);
        List<Pair<String, String>> extractMatch = new ArrayList<>();
        List<RegexTtlIssuePlaceCodeMap> regexMatch = new ArrayList<>();
        ttlIssuePlaceCodeMapRepository
            .findAll()
            .forEach(
                it -> {
                    if (appConf.isEnableDebugPlaceIssueMapping()) {
                        log.warn("**info mapping: '{}'\t'{}'\t'{}'", it.getCode(), it.getName().toLowerCase(), it.getEnableRegex());
                    }
                    if (it.getEnableRegex() != null && it.getEnableRegex()) {
                        regexMatch.add(new RegexTtlIssuePlaceCodeMap().from(it));
                    } else {
                        extractMatch.add(new Pair<>(it.getName().toLowerCase(), it.getCode()));
                    }
                }
            );
        this.extractMatch = extractMatch;
        this.regexMatch = regexMatch;
        this.isRunningInit.set(false);
    }

    @Async
    @Transactional
    public void openAccountTTL(Long ekycId) {
        openAccountTTLNoAsync(ekycId, true, true);
    }

    public void reloadTtlCodeMap() {
        this.init();
    }

    public EKycCreatorStatus openAccountTTLNoAsync(Long ekycId, boolean returnNullDefault, boolean checkMatchingRate) {
        if (appConf.isEnableCallTllOpenAccount()) {
            EKyc ekyc =
                this.eKycRepository.findById(ekycId).orElseThrow(() -> new BadRequestAlertException("EKYC not exist", "EKyc", "notexist"));
            if (!ekyc.getStatus().equals(Status.PENDING)) {
                log.error("{}-{} EKYC already approved", ekyc.getIdentifierId(), ekyc.getPhoneNo());
                throw new BadRequestAlertException("EKYC already approved", "EKyc", "exist");
            }
            EKycCreatorStatus creatorStatus = this.ekycCreatorStatusRepository.findById(ekycId).orElse(null);
            if (
                !checkMatchingRate ||
                appConf.getMatchThresholdPercentToCallTllOpenAccount() == null ||
                appConf.getMatchThresholdPercentToCallTllOpenAccount() - ekyc.getMatchingRate() * 100 < 0.0000001
            ) {
                if (creatorStatus == null) {
                    creatorStatus = new EKycCreatorStatus();
                    creatorStatus.setEKyc(ekyc);
                    creatorStatus.setStatus(PENDING_STATUS);
                } else if (SUCCESS_STATUS.equals(creatorStatus.getStatus())) {
                    log.error("{}-{} EKYC already auto approved", ekyc.getIdentifierId(), ekyc.getPhoneNo());
                    throw new BadRequestAlertException("EKYC already auto approved", "EKyc", "exist");
                } else {
                    creatorStatus.setReason(null);
                }
                creatorStatus.setUpdatedAt(ZonedDateTime.now());
                if (ObjectUtils.isEmpty(ekyc.getTradingCodeImageUrl())) {
                    OpenAccountReq openAccountReq = null;
                    try {
                        openAccountReq = new OpenAccountReq().update(ekyc, this.tTlBankService, appConf);
                    } catch (GeneralException e) {
                        log.error(
                            "{}-{} exception when converting request: {}-{}",
                            e.getCode(),
                            ekyc.getIdentifierId(),
                            ekyc.getPhoneNo(),
                            e
                        );
                        creatorStatus.setStatus(FAIL_STATUS);
                        creatorStatus.setReason("Fail to get request: " + e.getCode());
                    }
                    if (openAccountReq != null) {
                        try {
                            openAccountReq.setCityIssue(getCityIssueCode(ekyc.getIssuePlace()));
                            log.info(
                                "{}-{} using city issue code {} from issuePlace '{}'",
                                ekyc.getIdentifierId(),
                                ekyc.getPhoneNo(),
                                openAccountReq.getCityIssue(),
                                ekyc.getIssuePlace()
                            );
                        } catch (InvalidValueException e) {
                            log.error("{}-{} cannot find issuePlace '{}'", ekyc.getIdentifierId(), ekyc.getPhoneNo(), ekyc.getIssuePlace());
                            creatorStatus.setStatus(FAIL_STATUS);
                            creatorStatus.setReason("Cannot find mapping for issuePlace");
                        }
                    }
                    if (ObjectUtils.isEmpty(creatorStatus.getReason())) {
                        try {
                            log.info(
                                "{}-{} will request open account to core {}",
                                ekyc.getIdentifierId(),
                                ekyc.getPhoneNo(),
                                openAccountReq
                            );
                            Pair<OpenAccountRes, String> response = ttlApiService.openAccount(openAccountReq);
                            log.info("{}-{} finish request open account to core", ekyc.getIdentifierId(), ekyc.getPhoneNo());
                            creatorStatus.setStatus(SUCCESS_STATUS);
                            creatorStatus.setFullResult(response.getRight());
                            ekyc.setStatus(Status.AUTO_APPROVED);
                            try {
                                eKycRepository.save(ekyc);
                            } catch (Exception ex) {
                                log.error("{}-{} fail to call save ekyc approve status", ekyc.getIdentifierId(), ekyc.getPhoneNo(), ex);
                            }
                        } catch (GeneralException e) {
                            log.error("{}-{} fail to call open account", ekyc.getIdentifierId(), ekyc.getPhoneNo(), e);
                            creatorStatus.setStatus(FAIL_STATUS);
                            creatorStatus.setReason("call ttl api: " + e.getCode());
                        } catch (Exception e) {
                            log.error("{}-{} fail to call open account", ekyc.getIdentifierId(), ekyc.getPhoneNo(), e);
                            creatorStatus.setStatus(FAIL_STATUS);
                            creatorStatus.setReason("call ttl api: " + e.getMessage());
                        }
                    }
                } else {
                    log.error("{}-{} fail to open account in core: Customer is foreigner", ekyc.getIdentifierId(), ekyc.getPhoneNo());
                    creatorStatus.setStatus(FAIL_STATUS);
                    creatorStatus.setReason("Customer is foreigner");
                }
            } else {
                log.error(
                    "{}-{} The matching rate is too low {} compare with threshold {}",
                    ekyc.getIdentifierId(),
                    ekyc.getPhoneNo(),
                    ekyc.getMatchingRate(),
                    appConf.getMatchThresholdPercentToCallTllOpenAccount()
                );
                creatorStatus.setStatus(FAIL_STATUS);
                creatorStatus.setReason("Matching rate is too low");
            }
            ekycCreatorStatusRepository.save(creatorStatus);
            return creatorStatus;
        } else {
            if (returnNullDefault) {
                return null;
            }
            throw new BadRequestAlertException("Configuration not allow to open account by api", "EKyc", "wrongconfig");
        }
    }

    public String getCityIssueCode(String issuePlace) {
        String lowerCase = issuePlace.toLowerCase();
        try {
            return extractMatch
                .stream()
                .filter(it -> it.getLeft().equalsIgnoreCase(lowerCase))
                .findFirst()
                .map(Pair::getRight)
                .orElseGet(
                    () ->
                        regexMatch
                            .stream()
                            .map(it -> it.match(lowerCase))
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElseThrow(() -> new InvalidValueException("issuePlace"))
                );
        } catch (Exception e) {
            if (appConf.isEnableDebugPlaceIssueMapping()) {
                StringBuilder sb = new StringBuilder();
                sb.append("**********************************\n");
                sb.append(String.format("testing for value '%s'\nwith  bytes value '%s'", lowerCase, getByteString(lowerCase)));
                extractMatch.forEach(
                    pair -> {
                        sb.append(
                            String.format(
                                "key '%s' with match '%b' and ignore case '%b'\nbytes: %s\n",
                                pair.getLeft(),
                                pair.getLeft().equals(lowerCase),
                                pair.getLeft().equalsIgnoreCase(lowerCase),
                                getByteString(pair.getLeft())
                            )
                        );
                    }
                );
                sb.append("**********************************\n");
                log.warn(sb.toString());
            }
            throw e;
        }
    }

    private String getByteString(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%d ", b));
        }
        return sb.toString();
    }
}
