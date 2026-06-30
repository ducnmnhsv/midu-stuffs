package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.repository.CreatedChatRoomRepository;
import com.difisoft.nhsv.admin.service.criteria.CreatedChatRoomCriteria;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link CreatedChatRoom} entities in the database.
 * The main input is a {@link CreatedChatRoomCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CreatedChatRoom} or a {@link Page} of {@link CreatedChatRoom} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CreatedChatRoomQueryService extends QueryService<CreatedChatRoom> {

    private final Logger log = LoggerFactory.getLogger(CreatedChatRoomQueryService.class);

    private final CreatedChatRoomRepository createdChatRoomRepository;

    public CreatedChatRoomQueryService(CreatedChatRoomRepository createdChatRoomRepository) {
        this.createdChatRoomRepository = createdChatRoomRepository;
    }

    /**
     * Return a {@link List} of {@link CreatedChatRoom} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CreatedChatRoom> findByCriteria(CreatedChatRoomCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CreatedChatRoom> specification = createSpecification(criteria);
        return createdChatRoomRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link CreatedChatRoom} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CreatedChatRoom> findByCriteria(CreatedChatRoomCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CreatedChatRoom> specification = createSpecification(criteria);
        return createdChatRoomRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CreatedChatRoomCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CreatedChatRoom> specification = createSpecification(criteria);
        return createdChatRoomRepository.count(specification);
    }

    /**
     * Function to convert {@link CreatedChatRoomCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CreatedChatRoom> createSpecification(CreatedChatRoomCriteria criteria) {
        Specification<CreatedChatRoom> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CreatedChatRoom_.id));
            }
            if (criteria.getGroupName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGroupName(), CreatedChatRoom_.groupName));
            }
            if (criteria.getGroupOwner() != null) {
                specification = specification.and(buildStringSpecification(criteria.getGroupOwner(), CreatedChatRoom_.groupOwner));
            }
            if (criteria.getIntroduction() != null) {
                specification = specification.and(buildStringSpecification(criteria.getIntroduction(), CreatedChatRoom_.introduction));
            }
            if (criteria.getPhoto() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhoto(), CreatedChatRoom_.photo));
            }
            if (criteria.getBrokerName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBrokerName(), CreatedChatRoom_.brokerName));
            }
            if (criteria.getBrokerContact() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBrokerContact(), CreatedChatRoom_.brokerContact));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), CreatedChatRoom_.status));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), CreatedChatRoom_.createdBy));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CreatedChatRoom_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), CreatedChatRoom_.updatedAt));
            }
            if (criteria.getApprovedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getApprovedAt(), CreatedChatRoom_.approvedAt));
            }
            if (criteria.getApprovedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getApprovedBy(), CreatedChatRoom_.approvedBy));
            }
            if (criteria.getTotalView() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTotalView(), CreatedChatRoom_.totalView));
            }
        }
        return specification;
    }
}
