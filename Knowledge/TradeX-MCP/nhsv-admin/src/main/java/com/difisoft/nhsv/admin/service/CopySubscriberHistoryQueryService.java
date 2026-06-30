package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopySubscriberHistory;
import com.difisoft.nhsv.admin.repository.CopySubscriberHistoryRepository;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberHistoryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopySubscriberHistoryMapper;
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
 * Service for executing complex queries for {@link CopySubscriberHistory} entities in the database.
 * The main input is a {@link CopySubscriberHistoryCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopySubscriberHistoryDTO} or a {@link Page} of {@link CopySubscriberHistoryDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopySubscriberHistoryQueryService extends QueryService<CopySubscriberHistory> {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberHistoryQueryService.class);

    private final CopySubscriberHistoryRepository copySubscriberHistoryRepository;

    private final CopySubscriberHistoryMapper copySubscriberHistoryMapper;

    public CopySubscriberHistoryQueryService(
        CopySubscriberHistoryRepository copySubscriberHistoryRepository,
        CopySubscriberHistoryMapper copySubscriberHistoryMapper
    ) {
        this.copySubscriberHistoryRepository = copySubscriberHistoryRepository;
        this.copySubscriberHistoryMapper = copySubscriberHistoryMapper;
    }

    /**
     * Return a {@link List} of {@link CopySubscriberHistoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopySubscriberHistoryDTO> findByCriteria(CopySubscriberHistoryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopySubscriberHistory> specification = createSpecification(criteria);
        return copySubscriberHistoryMapper.toDto(copySubscriberHistoryRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopySubscriberHistoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopySubscriberHistoryDTO> findByCriteria(CopySubscriberHistoryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopySubscriberHistory> specification = createSpecification(criteria);
        return copySubscriberHistoryRepository.findAll(specification, page).map(copySubscriberHistoryMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopySubscriberHistoryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopySubscriberHistory> specification = createSpecification(criteria);
        return copySubscriberHistoryRepository.count(specification);
    }

    /**
     * Function to convert {@link CopySubscriberHistoryCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopySubscriberHistory> createSpecification(CopySubscriberHistoryCriteria criteria) {
        Specification<CopySubscriberHistory> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopySubscriberHistory_.id));
            }
            if (criteria.getAccountNumber() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getAccountNumber(), CopySubscriberHistory_.accountNumber));
            }
            if (criteria.getSubNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSubNumber(), CopySubscriberHistory_.subNumber));
            }
            if (criteria.getUserName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUserName(), CopySubscriberHistory_.userName));
            }
            if (criteria.getAllocatedRatio() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getAllocatedRatio(), CopySubscriberHistory_.allocatedRatio));
            }
            if (criteria.getOrderSetType() != null) {
                specification = specification.and(buildSpecification(criteria.getOrderSetType(), CopySubscriberHistory_.orderSetType));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopySubscriberHistory_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), CopySubscriberHistory_.updatedAt));
            }
            if (criteria.getMlUserIdId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getMlUserIdId(),
                            root -> root.join(CopySubscriberHistory_.mlUserId, JoinType.LEFT).get(User_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
