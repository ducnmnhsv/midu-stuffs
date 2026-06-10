package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.domain.*; // for static metamodels
import com.techx.tradex.ekycadmin.domain.EContract;
import com.techx.tradex.ekycadmin.repository.EContractRepository;
import com.techx.tradex.ekycadmin.service.criteria.EContractCriteria;
import com.techx.tradex.ekycadmin.service.dto.EContractDTO;
import com.techx.tradex.ekycadmin.service.mapper.EContractMapper;
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
 * Service for executing complex queries for {@link EContract} entities in the database.
 * The main input is a {@link EContractCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link EContractDTO} or a {@link Page} of {@link EContractDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EContractQueryService extends QueryService<EContract> {

    private final Logger log = LoggerFactory.getLogger(EContractQueryService.class);

    private final EContractRepository eContractRepository;

    private final EContractMapper eContractMapper;

    public EContractQueryService(EContractRepository eContractRepository, EContractMapper eContractMapper) {
        this.eContractRepository = eContractRepository;
        this.eContractMapper = eContractMapper;
    }

    /**
     * Return a {@link List} of {@link EContractDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<EContractDTO> findByCriteria(EContractCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<EContract> specification = createSpecification(criteria);
        return eContractMapper.toDto(eContractRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link EContractDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EContractDTO> findByCriteria(EContractCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<EContract> specification = createSpecification(criteria);
        return eContractRepository.findAll(specification, page).map(eContractMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EContractCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<EContract> specification = createSpecification(criteria);
        return eContractRepository.count(specification);
    }

    /**
     * Function to convert {@link EContractCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<EContract> createSpecification(EContractCriteria criteria) {
        Specification<EContract> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), EContract_.id));
            }
            if (criteria.getRefId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRefId(), EContract_.refId));
            }
            if (criteria.getEnvelopeId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEnvelopeId(), EContract_.envelopeId));
            }
            if (criteria.getIdentifierId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getIdentifierId(), EContract_.identifierId));
            }
            if (criteria.getTemplateId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTemplateId(), EContract_.templateId));
            }
            if (criteria.getAlias() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAlias(), EContract_.alias));
            }
            if (criteria.getCompanyType() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCompanyType(), EContract_.companyType));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), EContract_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), EContract_.updatedAt));
            }
            if (criteria.getEKycId() != null) {
                specification =
                    specification.and(
                        buildSpecification(criteria.getEKycId(), root -> root.join(EContract_.eKyc, JoinType.LEFT).get(EKyc_.id))
                    );
            }
        }
        return specification;
    }
}
