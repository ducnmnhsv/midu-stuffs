package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopySubscriber;
import com.difisoft.nhsv.admin.repository.CopySubscriberRepository;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberCriteria;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberMapper;
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
 * Service for executing complex queries for {@link CopySubscriber} entities in the database.
 * The main input is a {@link CopySubscriberCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopySubscriberDTO} or a {@link Page} of {@link CopySubscriberDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopySubscriberQueryService extends QueryService<CopySubscriber> {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberQueryService.class);

    private final CopySubscriberRepository copySubscriberRepository;

    private final CopySubscriberMapper copySubscriberMapper;

    public CopySubscriberQueryService(CopySubscriberRepository copySubscriberRepository, CopySubscriberMapper copySubscriberMapper) {
        this.copySubscriberRepository = copySubscriberRepository;
        this.copySubscriberMapper = copySubscriberMapper;
    }

    /**
     * Return a {@link List} of {@link CopySubscriberDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopySubscriberDTO> findByCriteria(CopySubscriberCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopySubscriber> specification = createSpecification(criteria);
        return copySubscriberMapper.toDto(copySubscriberRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopySubscriberDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopySubscriberDTO> findByCriteria(CopySubscriberCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopySubscriber> specification = createSpecification(criteria);
        return copySubscriberRepository.findAll(specification, page).map(copySubscriberMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopySubscriberCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopySubscriber> specification = createSpecification(criteria);
        return copySubscriberRepository.count(specification);
    }

    /**
     * Function to convert {@link CopySubscriberCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopySubscriber> createSpecification(CopySubscriberCriteria criteria) {
        Specification<CopySubscriber> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopySubscriber_.id));
            }
            if (criteria.getAccountNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAccountNumber(), CopySubscriber_.accountNumber));
            }
            if (criteria.getSubNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSubNumber(), CopySubscriber_.subNumber));
            }
            if (criteria.getUserName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUserName(), CopySubscriber_.userName));
            }
            if (criteria.getAllocatedRatio() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAllocatedRatio(), CopySubscriber_.allocatedRatio));
            }
            if (criteria.getOrderSetType() != null) {
                specification = specification.and(buildSpecification(criteria.getOrderSetType(), CopySubscriber_.orderSetType));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopySubscriber_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), CopySubscriber_.updatedAt));
            }
            if (criteria.getMlUserIdId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getMlUserIdId(),
                            root -> root.join(CopySubscriber_.mlUserId, JoinType.LEFT).get(User_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
