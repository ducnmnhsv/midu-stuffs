package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.enumeration.InviteStatusEnum;
import com.difisoft.nhsv.admin.repository.InviteUserRepository;
import com.difisoft.nhsv.admin.repository.primary.InviteUserPrimaryRepository;
import com.difisoft.nhsv.admin.service.criteria.InviteUserCriteria;
import com.difisoft.nhsv.admin.service.criteria.InviteUserPrimaryCriteria;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for executing complex queries for {@link InviteUser} entities in the
 * database.
 * The main input is a {@link InviteUserCriteria} which gets converted to
 * {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link InviteUser} or a {@link Page} of
 * {@link InviteUser} which fulfills the criteria.
 */
@Service
@Primary
public class InviteUserQueryPrimaryService extends InviteUserQueryService {

    private final Logger log = LoggerFactory.getLogger(InviteUserQueryService.class);

    private final InviteUserPrimaryRepository inviteUserPrimaryRepository;

    public InviteUserQueryPrimaryService(InviteUserRepository inviteUserRepository,
            InviteUserPrimaryRepository inviteUserPrimaryRepository) {
        super(inviteUserRepository);
        this.inviteUserPrimaryRepository = inviteUserPrimaryRepository;
    }

    @Transactional
    public List<InviteUser> findByCriteria(InviteUserPrimaryCriteria criteria) {
        log.info("find by criteria : {}", criteria);
        inviteUserPrimaryRepository.findAllByStatus(InviteStatusEnum.PENDING).forEach(inviteUser -> {
            if (inviteUser.getActivationDate().plus(1, ChronoUnit.DAYS).isBefore(ZonedDateTime.now())) {
                inviteUser.setStatus(InviteStatusEnum.EXPIRED);
                inviteUser.setActivationKey(null);
                inviteUser.setUpdatedAt(ZonedDateTime.now());
                inviteUserPrimaryRepository.save(inviteUser);
            }
        });
        final Specification<InviteUser> specification = createSpecification(criteria);
        return inviteUserPrimaryRepository.findAll(specification);
    }

    @Transactional
    public Page<InviteUser> findByCriteria(InviteUserPrimaryCriteria criteria, Pageable page) {
        log.info("find by criteria : {}, page: {}", criteria, page);
        List<InviteUser> inviteUsers = inviteUserPrimaryRepository.findAllByStatus(InviteStatusEnum.PENDING);
        inviteUsers.forEach(inviteUser -> {
            if (inviteUser.getActivationDate().plus(1, ChronoUnit.DAYS).isBefore(ZonedDateTime.now())) {
                inviteUser.setStatus(InviteStatusEnum.EXPIRED);
                inviteUser.setActivationKey(null);
                inviteUser.setUpdatedAt(ZonedDateTime.now());
                inviteUserPrimaryRepository.save(inviteUser);
            }
        });
        final Specification<InviteUser> specification = createSpecification(criteria);
        return inviteUserPrimaryRepository.findAll(specification, page);
    }

    protected Specification<InviteUser> createSpecification(InviteUserPrimaryCriteria criteria) {
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
                specification = specification
                        .and(buildRangeSpecification(criteria.getCreatedAt(), InviteUser_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getUpdatedAt(), InviteUser_.updatedAt));
            }
            if (criteria.getCreatedId() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getCreatedId(), InviteUser_.createdId));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getCreatedBy(), InviteUser_.createdBy));
            }
            if (criteria.getActivationKey() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getActivationKey(), InviteUser_.activationKey));
            }
            if (criteria.getActivationDate() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getActivationDate(), InviteUser_.activationDate));
            }
            if (criteria.getLangKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLangKey(), InviteUser_.langKey));
            }
            if (criteria.getAuthorities() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getAuthorities(), InviteUser_.authorities));
            }
            if (criteria.getSearch() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSearch(), InviteUser_.email)
                        .or(buildStringSpecification(criteria.getSearch(), InviteUser_.login)));
            }
        }
        return specification;
    }
}
