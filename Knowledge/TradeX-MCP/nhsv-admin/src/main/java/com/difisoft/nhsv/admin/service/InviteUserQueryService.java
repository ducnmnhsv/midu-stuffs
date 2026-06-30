package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.repository.InviteUserRepository;
import com.difisoft.nhsv.admin.service.criteria.InviteUserCriteria;
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
 * Service for executing complex queries for {@link InviteUser} entities in the database.
 * The main input is a {@link InviteUserCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link InviteUser} or a {@link Page} of {@link InviteUser} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class InviteUserQueryService extends QueryService<InviteUser> {

    private final Logger log = LoggerFactory.getLogger(InviteUserQueryService.class);

    private final InviteUserRepository inviteUserRepository;

    public InviteUserQueryService(InviteUserRepository inviteUserRepository) {
        this.inviteUserRepository = inviteUserRepository;
    }

    /**
     * Return a {@link List} of {@link InviteUser} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<InviteUser> findByCriteria(InviteUserCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<InviteUser> specification = createSpecification(criteria);
        return inviteUserRepository.findAll(specification);
    }

    /**
     * Return a {@link Page} of {@link InviteUser} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<InviteUser> findByCriteria(InviteUserCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<InviteUser> specification = createSpecification(criteria);
        return inviteUserRepository.findAll(specification, page);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(InviteUserCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<InviteUser> specification = createSpecification(criteria);
        return inviteUserRepository.count(specification);
    }

    /**
     * Function to convert {@link InviteUserCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<InviteUser> createSpecification(InviteUserCriteria criteria) {
        Specification<InviteUser> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), InviteUser_.id));
            }
            if (criteria.getLogin() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLogin(), InviteUser_.login));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), InviteUser_.email));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), InviteUser_.status));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), InviteUser_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), InviteUser_.updatedAt));
            }
            if (criteria.getCreatedId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedId(), InviteUser_.createdId));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), InviteUser_.createdBy));
            }
            if (criteria.getActivationKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getActivationKey(), InviteUser_.activationKey));
            }
            if (criteria.getActivationDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getActivationDate(), InviteUser_.activationDate));
            }
            if (criteria.getLangKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLangKey(), InviteUser_.langKey));
            }
            if (criteria.getAuthorities() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAuthorities(), InviteUser_.authorities));
            }
        }
        return specification;
    }
}
