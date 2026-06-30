package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopySubscriberDetails;
import com.difisoft.nhsv.admin.repository.CopySubscriberDetailsRepository;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberDetailsCriteria;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberDetailsMapper;
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
 * Service for executing complex queries for {@link CopySubscriberDetails} entities in the database.
 * The main input is a {@link CopySubscriberDetailsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopySubscriberDetailsDTO} or a {@link Page} of {@link CopySubscriberDetailsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopySubscriberDetailsQueryService extends QueryService<CopySubscriberDetails> {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberDetailsQueryService.class);

    private final CopySubscriberDetailsRepository copySubscriberDetailsRepository;

    private final CopySubscriberDetailsMapper copySubscriberDetailsMapper;

    public CopySubscriberDetailsQueryService(
        CopySubscriberDetailsRepository copySubscriberDetailsRepository,
        CopySubscriberDetailsMapper copySubscriberDetailsMapper
    ) {
        this.copySubscriberDetailsRepository = copySubscriberDetailsRepository;
        this.copySubscriberDetailsMapper = copySubscriberDetailsMapper;
    }

    /**
     * Return a {@link List} of {@link CopySubscriberDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopySubscriberDetailsDTO> findByCriteria(CopySubscriberDetailsCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopySubscriberDetails> specification = createSpecification(criteria);
        return copySubscriberDetailsMapper.toDto(copySubscriberDetailsRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopySubscriberDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopySubscriberDetailsDTO> findByCriteria(CopySubscriberDetailsCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopySubscriberDetails> specification = createSpecification(criteria);
        return copySubscriberDetailsRepository.findAll(specification, page).map(copySubscriberDetailsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopySubscriberDetailsCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopySubscriberDetails> specification = createSpecification(criteria);
        return copySubscriberDetailsRepository.count(specification);
    }

    /**
     * Function to convert {@link CopySubscriberDetailsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopySubscriberDetails> createSpecification(CopySubscriberDetailsCriteria criteria) {
        Specification<CopySubscriberDetails> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopySubscriberDetails_.id));
            }
            if (criteria.getUsername() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUsername(), CopySubscriberDetails_.username));
            }
            if (criteria.getIdentifierNumber() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getIdentifierNumber(), CopySubscriberDetails_.identifierNumber));
            }
            if (criteria.getBranchCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBranchCode(), CopySubscriberDetails_.branchCode));
            }
            if (criteria.getMngDeptCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMngDeptCode(), CopySubscriberDetails_.mngDeptCode));
            }
            if (criteria.getDeptCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDeptCode(), CopySubscriberDetails_.deptCode));
            }
            if (criteria.getAgencyNumber() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getAgencyNumber(), CopySubscriberDetails_.agencyNumber));
            }
            if (criteria.getAccountNumbers() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getAccountNumbers(), CopySubscriberDetails_.accountNumbers));
            }
            if (criteria.getUserLevel() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUserLevel(), CopySubscriberDetails_.userLevel));
            }
            if (criteria.getCopySubscriberId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCopySubscriberId(),
                            root -> root.join(CopySubscriberDetails_.copySubscriberId, JoinType.LEFT).get(CopySubscriber_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
