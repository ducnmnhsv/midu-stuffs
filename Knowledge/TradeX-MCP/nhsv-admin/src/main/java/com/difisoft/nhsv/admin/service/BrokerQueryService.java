package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.repository.BrokerRepository;
import com.difisoft.nhsv.admin.service.criteria.BrokerCriteria;
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
 * Service for executing complex queries for {@link Broker} entities in the database.
 * The main input is a {@link BrokerCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link Broker} or a {@link Page} of {@link Broker} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class BrokerQueryService extends QueryService<Broker> {

    private final Logger log = LoggerFactory.getLogger(BrokerQueryService.class);

    private final BrokerRepository brokerRepository;

    public BrokerQueryService(BrokerRepository brokerRepository) {
        this.brokerRepository = brokerRepository;
    }

    /**
     * Return a {@link List} of {@link Broker} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<Broker> findByCriteria(BrokerCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Broker> specification = createSpecification(criteria);
        return brokerRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link Broker} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<Broker> findByCriteria(BrokerCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Broker> specification = createSpecification(criteria);
        return brokerRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(BrokerCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Broker> specification = createSpecification(criteria);
        return brokerRepository.count(specification);
    }

    /**
     * Function to convert {@link BrokerCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Broker> createSpecification(BrokerCriteria criteria) {
        Specification<Broker> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Broker_.id));
            }
            if (criteria.getUsername() != null) {
                specification = specification.and(buildStringSpecification(criteria.getUsername(), Broker_.username));
            }
            if (criteria.getFullname() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFullname(), Broker_.fullname));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Broker_.status));
            }
            if (criteria.getTotalChatRoom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalChatRoom(), Broker_.totalChatRoom));
            }
            if (criteria.getCurrentRank() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCurrentRank(), Broker_.currentRank));
            }
            if (criteria.getIsDynamic() != null) {
                specification = specification.and(buildSpecification(criteria.getIsDynamic(), Broker_.isDynamic));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), Broker_.email));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), Broker_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), Broker_.updatedAt));
            }
            if (criteria.getDeactivatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDeactivatedAt(), Broker_.deactivatedAt));
            }
            if (criteria.getDeactivatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDeactivatedBy(), Broker_.deactivatedBy));
            }
            if (criteria.getInvitedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getInvitedBy(), Broker_.invitedBy));
            }
        }
        return specification;
    }
}
