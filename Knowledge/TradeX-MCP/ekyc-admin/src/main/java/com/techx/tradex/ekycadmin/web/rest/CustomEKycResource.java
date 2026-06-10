package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.domain.*;
import com.techx.tradex.ekycadmin.domain.enumeration.Status;
import com.techx.tradex.ekycadmin.models.dto.IEContractInfo;
import com.techx.tradex.ekycadmin.models.response.EContractInfoResponse;
import com.techx.tradex.ekycadmin.models.request.MatchingRateRequest;
import com.techx.tradex.ekycadmin.repository.*;
import com.techx.tradex.ekycadmin.service.CustomEKycService;
import com.techx.tradex.ekycadmin.service.TtlOpenAccountService;
import com.techx.tradex.ekycadmin.service.criteria.EKycCriteria;
import com.techx.tradex.ekycadmin.utils.Util;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

import javax.annotation.PostConstruct;

/**
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EKyc}.
 */
@RestController
@RequestMapping("/api/v1")
public class CustomEKycResource {

    private final Logger log = LoggerFactory.getLogger(EKycResource.class);

    private static final String ENTITY_NAME = "eKyc";

    private final CustomEKycRepository customEKycRepository;
    private final CustomEKycService customEKycService;
    private final TtlOpenAccountService ttlOpenAccountService;
    private final CustomEKycBankListRepository eKycBankListRepository;
    private final CustomEKycAdditionalInfoRepository eKycAdditionalInfoRepository;
    private final CustomBlockholderRepository blockholderRepository;
    private final CustomPublicCoopRepository publicCoopRepository;
    private final EContractInfoCustomRepository eContractInfoCustomRepository;
    private final CustomEKycQueryService eKycQueryService;
    private final AppConf appConf;
    private final CustomMatchingRateRepository matchingRateRepository;


    public CustomEKycResource(
            CustomEKycRepository customEKycRepository,
            TtlOpenAccountService ttlOpenAccountService,
            CustomEKycService customEKycService,
            CustomEKycBankListRepository eKycBankListRepository,
            CustomEKycAdditionalInfoRepository eKycAdditionalInfoRepository,
            CustomBlockholderRepository blockholderRepository,
            CustomPublicCoopRepository publicCoopRepository,
            EContractInfoCustomRepository eContractInfoCustomRepository,
            CustomEKycQueryService eKycQueryService,
            AppConf appConf,
            CustomMatchingRateRepository matchingRateRepository
    ) {
        this.customEKycRepository = customEKycRepository;
        this.ttlOpenAccountService = ttlOpenAccountService;
        this.customEKycService = customEKycService;
        this.eKycBankListRepository = eKycBankListRepository;
        this.eKycAdditionalInfoRepository = eKycAdditionalInfoRepository;
        this.blockholderRepository = blockholderRepository;
        this.publicCoopRepository = publicCoopRepository;
        this.eContractInfoCustomRepository = eContractInfoCustomRepository;
        this.eKycQueryService = eKycQueryService;
        this.appConf = appConf;
        this.matchingRateRepository = matchingRateRepository;
    }

    @PutMapping("/ekyc-admin/ekyc/approve")
    public ResponseEntity<Map<Long, String>> approveEKyc(@Valid @RequestBody List<Long> idList) throws URISyntaxException {
        log.debug("REST request to approve EKyc - idList : {}", idList);
        Map<Long, String> results = new HashMap<>();
        idList.forEach(
            id -> {
                try {
                    customEKycService.approve(id);
                } catch (Exception e) {
                    results.put(id, e.getLocalizedMessage());
                }
            }
        );
        return ResponseEntity.ok().body(results);
    }

    @PutMapping("/ekyc-admin/ekyc/approveAndCreate")
    public ResponseEntity<Map<Long, String>> approveEKycAndCreateAccount(@Valid @RequestBody List<Long> idList) throws URISyntaxException {
        log.debug("REST request to approve EKyc - idList : {}", idList);
        Map<Long, String> results = new HashMap<>();
        idList.forEach(
            id -> {
                try {
                    EKycCreatorStatus eKycCreatorStatus = customEKycService.approveAndCreate(id);
                    if (eKycCreatorStatus != null && !TtlOpenAccountService.SUCCESS_STATUS.equals(eKycCreatorStatus.getStatus())) {
                        throw new RuntimeException(eKycCreatorStatus.getReason());
                    }
                } catch (Exception e) {
                    results.put(id, e.getLocalizedMessage());
                }
            }
        );
        return ResponseEntity.ok().body(results);
    }

    @PutMapping("/ekyc-admin/ekyc/reject")
    public ResponseEntity<String> rejectEKyc(@Valid @RequestBody List<Long> idList) throws URISyntaxException {
        log.debug("REST request to approve EKyc - idList : {}", idList);
        List<EKyc> eKycList = customEKycRepository.findAllById(idList);
        if (eKycList.size() > 0) {
            eKycList.forEach(
                eKyc -> {
                    if (!eKyc.getStatus().equals(Status.PENDING)) {
                        throw new BadRequestAlertException("Invalid status", ENTITY_NAME + " " + eKyc.getId(), "invalidstatus");
                    }
                    eKyc.setStatus(Status.REJECT);
                    eKyc.setUpdatedAt(ZonedDateTime.now());
                }
            );
            customEKycRepository.saveAll(eKycList);
            customEKycService.sendRejectedEmail(eKycList.stream().map(EKyc::getEmail).collect(Collectors.toList()));
        } else {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        return ResponseEntity.ok().body("success");
    }

    @PostMapping("/ekyc-admin/ekyc/reloadTtlCodeMap")
    public ResponseEntity<String> rejectEKyc() throws URISyntaxException {
        log.debug("REST request to reload ttl code mapping");
        ttlOpenAccountService.reloadTtlCodeMap();
        return ResponseEntity.ok().body("success");
    }

    @GetMapping("/ekyc-admin/ekyc/e-kyc-bank-lists/{id}")
    public List<EKycBankList> getEKycBankList(@PathVariable Long id) {
        log.debug("REST request to get EKycBankList : {}", id);
        return eKycBankListRepository.findByEKycId(id);
    }

    @GetMapping("/ekyc-admin/ekyc/e-kyc-additional-infos/{id}")
    public ResponseEntity<EKycAdditionalInfo> getEKycAdditionalInfo(@PathVariable Long id) {
        log.debug("REST request to get EKycAdditionalInfo : {}", id);
        Optional<EKycAdditionalInfo> otp = eKycAdditionalInfoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(otp);
    }

    @GetMapping("/ekyc-admin/ekyc/blockholder/{id}")
    public ResponseEntity<List<Blockholder>> getBlockholder(@PathVariable Long id) {
        log.debug("REST request to get Blockholder : {}", id);
        List<Blockholder> blockholder = blockholderRepository.findByEKycAdditionalInfoId(id);
        return ResponseEntity.ok().body(blockholder);
    }

    @GetMapping("/ekyc-admin/ekyc/public-coop/{id}")
    public ResponseEntity<List<PublicCoop>> getPublicCoop(@PathVariable Long id) {
        log.debug("REST request to get PublicCoop : {}", id);
        List<PublicCoop> publicCoop = publicCoopRepository.findByEKycAdditionalInfoId(id);
        return ResponseEntity.ok().body(publicCoop);
    }

    @GetMapping(value = "/ekyc-admin/ekyc/e-contract-info/{id}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<EContractInfoResponse> getEContractInfo(@PathVariable Long id) {
        log.debug("REST request to get EContractInfo : {}", id);
        Optional<IEContractInfo> opt = eContractInfoCustomRepository.findByEKycId(id);
        if (opt.isPresent()) {
            IEContractInfo eContractInfo = opt.get();
            return ResponseEntity.ok().body(new EContractInfoResponse(
                eContractInfo.getId(),
                eContractInfo.getContractStatus(),
                eContractInfo.getSignFileContent(),
                eContractInfo.getContractFileContent(),
                eContractInfo.getCustomerSignatueStatus(),
                eContractInfo.getSecuritiesSignatureStatus(),
                eContractInfo.getEcontract(),
                null));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/ekyc-admin/e-kycs")
    public ResponseEntity<List<EKyc>> getAllEKycs(EKycCriteria criteria) {
        log.debug("REST request to get EKycs by criteria: {}", criteria);
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<EKyc> entityList = eKycQueryService.findByCriteria(criteria, sort);
        return ResponseEntity.ok().body(entityList);
    }

    @GetMapping(value = "/ekyc-admin/ekyc/e-contract/download/{id}")
    @ResponseBody
    public ResponseEntity<EContractInfoResponse> downloadEContract(@PathVariable Long id) throws Exception {
        log.debug("REST request to get EContract");
        Optional<IEContractInfo> opt = eContractInfoCustomRepository.findRequestDataContractFileContentById(id);
        if (opt.isPresent()) {
            IEContractInfo eContractInfo = opt.get();
            return ResponseEntity
                .ok()
                .body(new EContractInfoResponse(null,
                    null,
                    null,
                    eContractInfo.getContractFileContent(),
                    null,
                    null,
                    null,
                    Util.getContractFileName(eContractInfo.getRequestData())));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/ekyc-admin/configurations/matching-rate")
    @Transactional
    public ResponseEntity<String> updateMatchingRate(@RequestBody MatchingRateRequest request) {
        log.debug("REST request to update matching rate: {}", request);
        Optional<MatchingRate> opt = matchingRateRepository.findByCore(appConf.getCore());
        MatchingRate matchingRate;
        if (opt.isPresent()) {
            matchingRate = opt.get();
        } else {
            matchingRate = new MatchingRate();
            matchingRate.setCore(appConf.getCore());
            matchingRate.setCreatedAt(ZonedDateTime.now());
        }
        matchingRate.setMatchingRate(request.getMatchingRate());
        matchingRate.setUpdatedAt(ZonedDateTime.now());
        matchingRate = matchingRateRepository.save(matchingRate);
        appConf.getMatchThresholdPercent().put(appConf.getCore(), matchingRate.getMatchingRate());
        return ResponseEntity.ok().body("success");
    }

    @GetMapping("/ekyc-admin/configurations/matching-rate")
    public ResponseEntity<Double> getMatchingRate() {
        log.debug("REST request to get matching rate");
        Double matchingRate = appConf.getMatchThresholdPercent().get(appConf.getCore());
        return ResponseEntity.ok().body(matchingRate);
    }

    @PostConstruct
    public void init() {
        List<MatchingRate> matchingRates = matchingRateRepository.findAll();
        matchingRates.forEach(matchingRate ->  {
            appConf.getMatchThresholdPercent().put(matchingRate.getCore(), matchingRate.getMatchingRate());
        });
    }

    @PutMapping("/ekyc-admin/ekyc/waiting-confirmation")
    public ResponseEntity<String> waitingConfirmationEKyc()
        throws URISyntaxException {
        log.debug("REST request to waiting-confirmation EKyc");
        String results = "success";
        try {
            customEKycRepository.updateStatus(Status.WAITING_CONFIRMATION);
        } catch (Exception e) {
            results = e.getLocalizedMessage();
        }
        return ResponseEntity
            .ok()
            .body(results);
    }
}
