package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.domain.*; // for static metamodels
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.repository.EKycRepository;
import com.techx.tradex.ekycadmin.service.criteria.EKycCriteria;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link EKyc} entities in the database.
 * The main input is a {@link EKycCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link EKyc} or a {@link Page} of {@link EKyc} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EKycQueryService extends QueryService<EKyc> {

    private final Logger log = LoggerFactory.getLogger(EKycQueryService.class);

    private final EKycRepository eKycRepository;

    public EKycQueryService(EKycRepository eKycRepository) {
        this.eKycRepository = eKycRepository;
    }

    /**
     * Return a {@link List} of {@link EKyc} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<EKyc> findByCriteria(EKycCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<EKyc> specification = createSpecification(criteria);
        return eKycRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link EKyc} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EKyc> findByCriteria(EKycCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<EKyc> specification = createSpecification(criteria);
        return eKycRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EKycCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<EKyc> specification = createSpecification(criteria);
        return eKycRepository.count(specification);
    }

    /**
     * Function to convert {@link EKycCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<EKyc> createSpecification(EKycCriteria criteria) {
        Specification<EKyc> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), EKyc_.id));
            }
            if (criteria.getIdentifierId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getIdentifierId(), EKyc_.identifierId));
            }
            if (criteria.getFullName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFullName(), EKyc_.fullName));
            }
            if (criteria.getPhoneNo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhoneNo(), EKyc_.phoneNo));
            }
            if (criteria.getGender() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGender(), EKyc_.gender));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), EKyc_.type));
            }
            if (criteria.getBirthDay() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getBirthDay(), EKyc_.birthDay));
            }
            if (criteria.getExpiredDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getExpiredDate(), EKyc_.expiredDate));
            }
            if (criteria.getIssueDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getIssueDate(), EKyc_.issueDate));
            }
            if (criteria.getIssuePlace() != null) {
                specification = specification.and(buildStringSpecification(criteria.getIssuePlace(), EKyc_.issuePlace));
            }
            if (criteria.getAddress() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAddress(), EKyc_.address));
            }
            if (criteria.getOccupation() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOccupation(), EKyc_.occupation));
            }
            if (criteria.getHomeTown() != null) {
                specification = specification.and(buildStringSpecification(criteria.getHomeTown(), EKyc_.homeTown));
            }
            if (criteria.getPermanentProvince() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPermanentProvince(), EKyc_.permanentProvince));
            }
            if (criteria.getPermanentDistrict() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPermanentDistrict(), EKyc_.permanentDistrict));
            }
            if (criteria.getPermanentAddress() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPermanentAddress(), EKyc_.permanentAddress));
            }
            if (criteria.getContactProvince() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContactProvince(), EKyc_.contactProvince));
            }
            if (criteria.getContactDistrict() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContactDistrict(), EKyc_.contactDistrict));
            }
            if (criteria.getContactAddress() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContactAddress(), EKyc_.contactAddress));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), EKyc_.email));
            }
            if (criteria.getReferrerIdName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getReferrerIdName(), EKyc_.referrerIdName));
            }
            if (criteria.getReferrerBranch() != null) {
                specification = specification.and(buildStringSpecification(criteria.getReferrerBranch(), EKyc_.referrerBranch));
            }
            if (criteria.getBankAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBankAccount(), EKyc_.bankAccount));
            }
            if (criteria.getAccountName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccountName(), EKyc_.accountName));
            }
            if (criteria.getBankName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBankName(), EKyc_.bankName));
            }
            if (criteria.getBranch() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBranch(), EKyc_.branch));
            }
            if (criteria.getNationality() != null) {
                specification = specification.and(buildStringSpecification(criteria.getNationality(), EKyc_.nationality));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), EKyc_.status));
            }
            if (criteria.getFrontImageUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFrontImageUrl(), EKyc_.frontImageUrl));
            }
            if (criteria.getBackImageUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBackImageUrl(), EKyc_.backImageUrl));
            }
            if (criteria.getPortraitImageUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPortraitImageUrl(), EKyc_.portraitImageUrl));
            }
            if (criteria.getSignatureImageUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSignatureImageUrl(), EKyc_.signatureImageUrl));
            }
            if (criteria.getTradingCodeImageUrl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTradingCodeImageUrl(), EKyc_.tradingCodeImageUrl));
            }
            if (criteria.getIsMargin() != null) {
                specification = specification.and(buildSpecification(criteria.getIsMargin(), EKyc_.isMargin));
            }
            if (criteria.getMatchingRate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMatchingRate(), EKyc_.matchingRate));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), EKyc_.updatedAt));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), EKyc_.createdAt));
            }
            if (criteria.getBranchId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBranchId(), EKyc_.branchId));
            }
            if (criteria.getChannelId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getChannelId(), EKyc_.channelId));
            }
            if (criteria.getContractNo() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContractNo(), EKyc_.contractNo));
            }
            if (criteria.getAccountNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccountNumber(), EKyc_.accountNumber));
            }
            //            if (criteria.getEContractId() != null) {
            //                specification =
            //                    specification.and(
            //                        buildSpecification(criteria.getEContractId(), root -> root.join(EKyc_.eContract, JoinType.LEFT).get(EContract_.id))
            //                    );
            //            }
            if (criteria.geteKycId() != null) {
                specification = specification.and(buildStringSpecification(criteria.geteKycId(), EKyc_.eKycId));
            }
            if (criteria.getTaxNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTaxNumber(), EKyc_.taxNumber));
            }
            if (criteria.getOnlineTrading() != null) {
                specification = specification.and(buildSpecification(criteria.getOnlineTrading(), EKyc_.onlineTrading));
            }
            if (criteria.getAuthenMethod() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAuthenMethod(), EKyc_.authenMethod));
            }
            if (criteria.getOtpReceiveMethod() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOtpReceiveMethod(), EKyc_.otpReceiveMethod));
            }
            if (criteria.getAdvancedCashIncluded() != null) {
                specification = specification.and(buildSpecification(criteria.getAdvancedCashIncluded(), EKyc_.advancedCashIncluded));
            }
            if (criteria.getSmsMethod() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSmsMethod(), EKyc_.smsMethod));
            }
            if (criteria.getEmailNotification() != null) {
                specification = specification.and(buildSpecification(criteria.getEmailNotification(), EKyc_.emailNotification));
            }
            if (criteria.getReferral() != null) {
                specification = specification.and(buildStringSpecification(criteria.getReferral(), EKyc_.referral));
            }
            if (criteria.getPartnerId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPartnerId(), EKyc_.partnerId));
            }
            if (criteria.getPartnerName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPartnerName(), EKyc_.partnerName));
            }
            if (criteria.getCustomerSupport() != null) {
                specification = specification.and(buildSpecification(criteria.getCustomerSupport(), EKyc_.customerSupport));
            }
            if (criteria.getCsPartnerId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCsPartnerId(), EKyc_.csPartnerId));
            }
            if (criteria.getCsName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCsName(), EKyc_.csName));
            }
            if (criteria.getAccountNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccountNumber(), EKyc_.accountNumber));
            }
            if (criteria.getContractId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContractId(), EKyc_.contractId));
            }
            if (criteria.getContractStatus() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContractStatus(), EKyc_.contractStatus));
            }
            if (criteria.getFatca() != null) {
                specification = specification.and(buildSpecification(criteria.getFatca(), EKyc_.fatca));
            }
        }
        return specification;
    }
}
