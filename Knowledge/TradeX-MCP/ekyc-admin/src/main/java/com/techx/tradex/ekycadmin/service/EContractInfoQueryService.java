package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.domain.*; // for static metamodels
import com.techx.tradex.ekycadmin.domain.EContractInfo;
import com.techx.tradex.ekycadmin.repository.EContractInfoRepository;
import com.techx.tradex.ekycadmin.service.criteria.EContractInfoCriteria;
import com.techx.tradex.ekycadmin.service.dto.EContractInfoDTO;
import com.techx.tradex.ekycadmin.service.mapper.EContractInfoMapper;
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
 * Service for executing complex queries for {@link EContractInfo} entities in the database.
 * The main input is a {@link EContractInfoCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link EContractInfoDTO} or a {@link Page} of {@link EContractInfoDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EContractInfoQueryService extends QueryService<EContractInfo> {

    private final Logger log = LoggerFactory.getLogger(EContractInfoQueryService.class);

    private final EContractInfoRepository eContractInfoRepository;

    private final EContractInfoMapper eContractInfoMapper;

    public EContractInfoQueryService(EContractInfoRepository eContractInfoRepository, EContractInfoMapper eContractInfoMapper) {
        this.eContractInfoRepository = eContractInfoRepository;
        this.eContractInfoMapper = eContractInfoMapper;
    }

    /**
     * Return a {@link List} of {@link EContractInfoDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<EContractInfoDTO> findByCriteria(EContractInfoCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<EContractInfo> specification = createSpecification(criteria);
        return eContractInfoMapper.toDto(eContractInfoRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link EContractInfoDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EContractInfoDTO> findByCriteria(EContractInfoCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<EContractInfo> specification = createSpecification(criteria);
        return eContractInfoRepository.findAll(specification, page).map(eContractInfoMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EContractInfoCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<EContractInfo> specification = createSpecification(criteria);
        return eContractInfoRepository.count(specification);
    }

    /**
     * Function to convert {@link EContractInfoCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<EContractInfo> createSpecification(EContractInfoCriteria criteria) {
        Specification<EContractInfo> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), EContractInfo_.id));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), EContractInfo_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), EContractInfo_.updatedAt));
            }
            if (criteria.getTemplateId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTemplateId(), EContractInfo_.templateId));
            }
            if (criteria.getContactId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContactId(), EContractInfo_.contactId));
            }
            if (criteria.getContractStatus() != null) {
                specification = specification.and(buildStringSpecification(criteria.getContractStatus(), EContractInfo_.contractStatus));
            }
            if (criteria.getSignFileContent() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSignFileContent(), EContractInfo_.signFileContent));
            }
            if (criteria.getContractFileContent() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getContractFileContent(), EContractInfo_.contractFileContent));
            }
            if (criteria.getEContractId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getEContractId(),
                            root -> root.join(EContractInfo_.eContract, JoinType.LEFT).get(EContract_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
