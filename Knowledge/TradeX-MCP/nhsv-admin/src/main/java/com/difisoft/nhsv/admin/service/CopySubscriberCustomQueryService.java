package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*;
import com.difisoft.nhsv.admin.domain.CopySubscriber;
import com.difisoft.nhsv.admin.repository.CopySubscriberRepository;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberCriteria;
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
public class CopySubscriberCustomQueryService extends QueryService<CopySubscriber> {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberQueryService.class);

    private final CopySubscriberRepository copySubscriberRepository;

    public CopySubscriberCustomQueryService(CopySubscriberRepository copySubscriberRepository) {
        this.copySubscriberRepository = copySubscriberRepository;
    }

    @Transactional(readOnly = true)
    public List<CopySubscriber> findByCriteria(CopySubscriberCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopySubscriber> specification = createSpecification(criteria);
        return copySubscriberRepository.findAll(specification, sort);
    }

    @Transactional(readOnly = true)
    public Page<CopySubscriber> findByCriteria(CopySubscriberCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopySubscriber> specification = createSpecification(criteria);
        return copySubscriberRepository.findAll(specification, page);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(CopySubscriberCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopySubscriber> specification = createSpecification(criteria);
        return copySubscriberRepository.count(specification);
    }

    protected Specification<CopySubscriber> createSpecification(CopySubscriberCriteria criteria) {
        Specification<CopySubscriber> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopySubscriber_.id));
            }
            if (criteria.getAccountNumber() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getAccountNumber(), CopySubscriber_.accountNumber));
            }
            if (criteria.getSubNumber() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getSubNumber(), CopySubscriber_.subNumber));
            }
            if (criteria.getUserName() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getUserName(), CopySubscriber_.userName));
            }
            if (criteria.getAllocatedRatio() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getAllocatedRatio(), CopySubscriber_.allocatedRatio));
            }
            if (criteria.getOrderSetType() != null) {
                specification = specification
                        .and(buildSpecification(criteria.getOrderSetType(), CopySubscriber_.orderSetType));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getCreatedAt(), CopySubscriber_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getUpdatedAt(), CopySubscriber_.updatedAt));
            }
            if (criteria.getMlUserIdId() != null) {
                specification = specification.and(
                        buildSpecification(
                                criteria.getMlUserIdId(),
                                root -> root.join(CopySubscriber_.mlUserId, JoinType.LEFT).get(User_.id)));
            }
        }
        return specification;
    }
}
