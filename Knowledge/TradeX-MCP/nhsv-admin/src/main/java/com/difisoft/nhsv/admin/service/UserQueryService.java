package com.difisoft.nhsv.admin.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.difisoft.nhsv.admin.domain.Authority_;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.domain.User_;
import com.difisoft.nhsv.admin.service.criteria.UserCriteria;

import tech.jhipster.service.QueryService;

@Service
@Transactional(readOnly = true)
public class UserQueryService extends QueryService<User> {

    private final Logger log = LoggerFactory.getLogger(UserQueryService.class);

    private final com.difisoft.nhsv.admin.repository.UserRepository UserRepository;

    public UserQueryService(com.difisoft.nhsv.admin.repository.UserRepository UserRepository) {
        this.UserRepository = UserRepository;
    }

    @Transactional(readOnly = true)
    public List<User> findByCriteria(UserCriteria criteria) {
        log.info("find by criteria : {}", criteria);
        final Specification<User> specification = createSpecification(criteria);
        distinct(true);

        return UserRepository.findAll(specification);
    }

    @Transactional(readOnly = true)
    public Page<User> findByCriteria(UserCriteria criteria, Pageable page) {
        log.info("find by criteria : {}, page: {}", criteria, page);

        final Specification<User> specification = createSpecification(criteria);
        return UserRepository.findAll(specification, page);

    }

    @Transactional(readOnly = true)
    public long countByCriteria(UserCriteria criteria) {
        log.info("count by criteria : {}", criteria);

        final Specification<User> specification = createSpecification(criteria);

        return UserRepository.count(specification);
    }

    protected Specification<User> createSpecification(UserCriteria criteria) {
        Specification<User> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getFullName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFullName(), User_.fullName));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), User_.activated));
            }
            if (criteria.getRoles() != null) {
                specification = specification.and(buildSpecification(criteria.getRoles(),
                        root -> root.join(User_.authorities, JoinType.LEFT).get(Authority_.name)));
            }
            if (criteria.getIsSuperAdmin() != null) {
                specification = specification.and(buildSpecification(criteria.getIsSuperAdmin(),
                        root -> root.join(User_.authorities, JoinType.LEFT).get(Authority_.name)));
            }
        }
        specification = specification.and((root, query, builder) -> {
            query.distinct(true);
            return null;
        });
        return specification;
    }

}
