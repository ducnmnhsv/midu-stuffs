package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.repository.EKycRepository;
import com.techx.tradex.ekycadmin.utils.Util;
import com.techx.tradex.ekycadmin.service.FileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing {@link EKyc}.
 */
@Service
@Transactional
public class EKycService {

    private final Logger log = LoggerFactory.getLogger(EKycService.class);

    private final EKycRepository eKycRepository;
    private final FileService fileService;
    private final AppConf appConf;
    
    public EKycService(EKycRepository eKycRepository, FileService fileService, AppConf appConf) {
        this.eKycRepository = eKycRepository;
        this.fileService = fileService;
        this.appConf = appConf;
    }

    /**
     * Save a eKyc.
     *
     * @param eKyc the entity to save.
     * @return the persisted entity.
     */
    public EKyc save(EKyc eKyc) {
        log.debug("Request to save EKyc : {}", eKyc);
        return eKycRepository.save(eKyc);
    }

    /**
     * Partially update a eKyc.
     *
     * @param eKyc the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<EKyc> partialUpdate(EKyc eKyc) {
        log.debug("Request to partially update EKyc : {}", eKyc);

        return eKycRepository
            .findById(eKyc.getId())
            .map(
                existingEKyc -> {
                    if (eKyc.getIdentifierId() != null) {
                        existingEKyc.setIdentifierId(eKyc.getIdentifierId());
                    }
                    if (eKyc.getFullName() != null) {
                        existingEKyc.setFullName(eKyc.getFullName());
                    }
                    if (eKyc.getPhoneNo() != null) {
                        existingEKyc.setPhoneNo(eKyc.getPhoneNo());
                    }
                    if (eKyc.getGender() != null) {
                        existingEKyc.setGender(eKyc.getGender());
                    }
                    if (eKyc.getType() != null) {
                        existingEKyc.setType(eKyc.getType());
                    }
                    if (eKyc.getBirthDay() != null) {
                        existingEKyc.setBirthDay(eKyc.getBirthDay());
                    }
                    if (eKyc.getExpiredDate() != null) {
                        existingEKyc.setExpiredDate(eKyc.getExpiredDate());
                    }
                    if (eKyc.getIssueDate() != null) {
                        existingEKyc.setIssueDate(eKyc.getIssueDate());
                    }
                    if (eKyc.getIssuePlace() != null) {
                        existingEKyc.setIssuePlace(eKyc.getIssuePlace());
                    }
                    if (eKyc.getAddress() != null) {
                        existingEKyc.setAddress(eKyc.getAddress());
                    }
                    if (eKyc.getOccupation() != null) {
                        existingEKyc.setOccupation(eKyc.getOccupation());
                    }
                    if (eKyc.getHomeTown() != null) {
                        existingEKyc.setHomeTown(eKyc.getHomeTown());
                    }
                    if (eKyc.getPermanentProvince() != null) {
                        existingEKyc.setPermanentProvince(eKyc.getPermanentProvince());
                    }
                    if (eKyc.getPermanentDistrict() != null) {
                        existingEKyc.setPermanentDistrict(eKyc.getPermanentDistrict());
                    }
                    if (eKyc.getPermanentAddress() != null) {
                        existingEKyc.setPermanentAddress(eKyc.getPermanentAddress());
                    }
                    if (eKyc.getContactProvince() != null) {
                        existingEKyc.setContactProvince(eKyc.getContactProvince());
                    }
                    if (eKyc.getContactDistrict() != null) {
                        existingEKyc.setContactDistrict(eKyc.getContactDistrict());
                    }
                    if (eKyc.getContactAddress() != null) {
                        existingEKyc.setContactAddress(eKyc.getContactAddress());
                    }
                    if (eKyc.getEmail() != null) {
                        existingEKyc.setEmail(eKyc.getEmail());
                    }
                    if (eKyc.getReferrerIdName() != null) {
                        existingEKyc.setReferrerIdName(eKyc.getReferrerIdName());
                    }
                    if (eKyc.getReferrerBranch() != null) {
                        existingEKyc.setReferrerBranch(eKyc.getReferrerBranch());
                    }
                    if (eKyc.getBankAccount() != null) {
                        existingEKyc.setBankAccount(eKyc.getBankAccount());
                    }
                    if (eKyc.getAccountName() != null) {
                        existingEKyc.setAccountName(eKyc.getAccountName());
                    }
                    if (eKyc.getBankName() != null) {
                        existingEKyc.setBankName(eKyc.getBankName());
                    }
                    if (eKyc.getBranch() != null) {
                        existingEKyc.setBranch(eKyc.getBranch());
                    }
                    if (eKyc.getNationality() != null) {
                        existingEKyc.setNationality(eKyc.getNationality());
                    }
                    if (eKyc.getStatus() != null) {
                        existingEKyc.setStatus(eKyc.getStatus());
                    }
                    if (eKyc.getFrontImageUrl() != null) {
                        existingEKyc.setFrontImageUrl(eKyc.getFrontImageUrl());
                    }
                    if (eKyc.getBackImageUrl() != null) {
                        existingEKyc.setBackImageUrl(eKyc.getBackImageUrl());
                    }
                    if (eKyc.getPortraitImageUrl() != null) {
                        existingEKyc.setPortraitImageUrl(eKyc.getPortraitImageUrl());
                    }
                    if (eKyc.getSignatureImageUrl() != null) {
                        existingEKyc.setSignatureImageUrl(eKyc.getSignatureImageUrl());
                    }
                    if (eKyc.getTradingCodeImageUrl() != null) {
                        existingEKyc.setTradingCodeImageUrl(eKyc.getTradingCodeImageUrl());
                    }
                    if (eKyc.getIsMargin() != null) {
                        existingEKyc.setIsMargin(eKyc.getIsMargin());
                    }
                    if (eKyc.getMatchingRate() != null) {
                        existingEKyc.setMatchingRate(eKyc.getMatchingRate());
                    }
                    if (eKyc.getUpdatedAt() != null) {
                        existingEKyc.setUpdatedAt(eKyc.getUpdatedAt());
                    }
                    if (eKyc.getCreatedAt() != null) {
                        existingEKyc.setCreatedAt(eKyc.getCreatedAt());
                    }
                    if (eKyc.getBranchId() != null) {
                        existingEKyc.setBranchId(eKyc.getBranchId());
                    }
                    if (eKyc.getChannelId() != null) {
                        existingEKyc.setChannelId(eKyc.getChannelId());
                    }
                    if (eKyc.geteKycId() != null) {
                        existingEKyc.seteKycId(eKyc.geteKycId());
                    }
                    if (eKyc.getTaxNumber() != null) {
                        existingEKyc.setTaxNumber(eKyc.getTaxNumber());
                    }
                    if (eKyc.getOnlineTrading() != null) {
                        existingEKyc.setOnlineTrading(eKyc.getOnlineTrading());
                    }
                    if (eKyc.getAuthenMethod() != null) {
                        existingEKyc.setAuthenMethod(eKyc.getAuthenMethod());
                    }
                    if (eKyc.getOtpReceiveMethod() != null) {
                        existingEKyc.setOtpReceiveMethod(eKyc.getOtpReceiveMethod());
                    }
                    if (eKyc.getAdvancedCashIncluded() != null) {
                        existingEKyc.setAdvancedCashIncluded(eKyc.getAdvancedCashIncluded());
                    }
                    if (eKyc.getSmsMethod() != null) {
                        existingEKyc.setSmsMethod(eKyc.getSmsMethod());
                    }
                    if (eKyc.getEmailNotification() != null) {
                        existingEKyc.setEmailNotification(eKyc.getEmailNotification());
                    }
                    if (eKyc.getReferral() != null) {
                        existingEKyc.setReferral(eKyc.getReferral());
                    }
                    if (eKyc.getPartnerId() != null) {
                        existingEKyc.setPartnerId(eKyc.getPartnerId());
                    }
                    if (eKyc.getPartnerName() != null) {
                        existingEKyc.setPartnerName(eKyc.getPartnerName());
                    }
                    if (eKyc.getCustomerSupport() != null) {
                        existingEKyc.setCustomerSupport(eKyc.getCustomerSupport());
                    }
                    if (eKyc.getCsPartnerId() != null) {
                        existingEKyc.setCsPartnerId(eKyc.getCsPartnerId());
                    }
                    if (eKyc.getCsName() != null) {
                        existingEKyc.setCsName(eKyc.getCsName());
                    }
                    if (eKyc.getContractId() != null) {
                        existingEKyc.setContractId(eKyc.getContractId());
                    }
                    if (eKyc.getContractStatus() != null) {
                        existingEKyc.setContractStatus(eKyc.getContractStatus());
                    }
                    if (eKyc.getFatca() != null) {
                        existingEKyc.setFatca(eKyc.getFatca());
                    }
                    if (eKyc.getContractNo() != null) {
                        existingEKyc.setContractNo(eKyc.getContractNo());
                    }
                    if (eKyc.getAccountNumber() != null) {
                        existingEKyc.setAccountNumber(eKyc.getAccountNumber());
                    }
                    if (eKyc.getOcrLogId() != null) {
                        existingEKyc.setOcrLogId(eKyc.getOcrLogId());
                    }
                    if (eKyc.getCardLivenessLogId() != null) {
                        existingEKyc.setCardLivenessLogId(eKyc.getCardLivenessLogId());
                    }
                    if (eKyc.getCardRearLogId() != null) {
                        existingEKyc.setCardRearLogId(eKyc.getCardRearLogId());
                    }
                    if (eKyc.getCompareLogId() != null) {
                        existingEKyc.setCompareLogId(eKyc.getCompareLogId());
                    }
                    if (eKyc.getFaceLivenessLogId() != null) {
                        existingEKyc.setFaceLivenessLogId(eKyc.getFaceLivenessLogId());
                    }
                    if (eKyc.getFaceMaskLogId() != null) {
                        existingEKyc.setFaceMaskLogId(eKyc.getFaceMaskLogId());
                    }

                    return existingEKyc;
                }
            )
            .map(eKycRepository::save);
    }

    /**
     * Get all the eKycs.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<EKyc> findAll() {
        log.debug("Request to get all EKycs");
        return eKycRepository.findAll();
    }

    /**
     * Get one eKyc by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<EKyc> findOne(Long id) {
        log.debug("Request to get EKyc : {}", id);
        return eKycRepository.findById(id).map(ekyc -> {
            ekyc.setFrontImageUrl(
                fileService.getPresignedURLForDownloadFile(
                    appConf.getFileStorage().getEkycImagesBucket(), 
                    Util.getKeyForStorageServiceForFrontIDCardImage(
                        ekyc.getIdentifierId(), appConf.getFileStorage().getFrontIDCardImageSuffix()
                    ), 
                    appConf.getFileStorage().getPresignedURLDurationInSeconds())
            );
            ekyc.setBackImageUrl(
                fileService.getPresignedURLForDownloadFile(
                    appConf.getFileStorage().getEkycImagesBucket(), 
                    Util.getKeyForStorageServiceForBackIDCardImage(
                        ekyc.getIdentifierId(), appConf.getFileStorage().getBackIDCardImageSuffix()
                    ), 
                    appConf.getFileStorage().getPresignedURLDurationInSeconds()
                )
            );
            return ekyc;
        });
    }

    /**
     * Delete the eKyc by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete EKyc : {}", id);
        eKycRepository.deleteById(id);
    }

    /**
     * Search for the eKyc corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<EKyc> search(String query) {
        log.debug("Request to search EKycs for query {}", query);
        //        return eKycRepository.search(EKyc.PREFIX, query);
        return new ArrayList<>();
    }
}
