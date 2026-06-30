package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*;
import com.difisoft.nhsv.admin.domain.CopySubscriberHistory;
import com.difisoft.nhsv.admin.repository.CopySubscriberHistoryRepository;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberHistoryCriteria;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

@Service
@Transactional(readOnly = true)
public class CopySubscriberHistoryCustomQueryService extends QueryService<CopySubscriberHistory> {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberHistoryQueryService.class);

    private final CopySubscriberHistoryRepository copySubscriberHistoryRepository;

    public CopySubscriberHistoryCustomQueryService(
            CopySubscriberHistoryRepository copySubscriberHistoryRepository) {
        this.copySubscriberHistoryRepository = copySubscriberHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CopySubscriberHistory> findByCriteria(CopySubscriberHistoryCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopySubscriberHistory> specification = createSpecification(criteria);
        return copySubscriberHistoryRepository.findAll(specification, sort);
    }

    @Transactional(readOnly = true)
    public Page<CopySubscriberHistory> findByCriteria(CopySubscriberHistoryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopySubscriberHistory> specification = createSpecification(criteria);
        return copySubscriberHistoryRepository.findAll(specification, page);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(CopySubscriberHistoryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopySubscriberHistory> specification = createSpecification(criteria);
        return copySubscriberHistoryRepository.count(specification);
    }

    protected Specification<CopySubscriberHistory> createSpecification(CopySubscriberHistoryCriteria criteria) {
        Specification<CopySubscriberHistory> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopySubscriberHistory_.id));
            }
            if (criteria.getAccountNumber() != null) {
                specification = specification.and(
                        buildStringSpecification(criteria.getAccountNumber(), CopySubscriberHistory_.accountNumber));
            }
            if (criteria.getSubNumber() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getSubNumber(), CopySubscriberHistory_.subNumber));
            }
            if (criteria.getUserName() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getUserName(), CopySubscriberHistory_.userName));
            }
            if (criteria.getAllocatedRatio() != null) {
                specification = specification.and(
                        buildRangeSpecification(criteria.getAllocatedRatio(), CopySubscriberHistory_.allocatedRatio));
            }
            if (criteria.getOrderSetType() != null) {
                specification = specification
                        .and(buildSpecification(criteria.getOrderSetType(), CopySubscriberHistory_.orderSetType));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getCreatedAt(), CopySubscriberHistory_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getUpdatedAt(), CopySubscriberHistory_.updatedAt));
            }
            if (criteria.getMlUserIdId() != null) {
                specification = specification.and(
                        buildSpecification(
                                criteria.getMlUserIdId(),
                                root -> root.join(CopySubscriberHistory_.mlUserId, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }
}
