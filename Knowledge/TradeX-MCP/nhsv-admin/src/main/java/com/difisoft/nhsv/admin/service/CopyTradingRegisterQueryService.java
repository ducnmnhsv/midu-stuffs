package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CopyTradingRegister;
import com.difisoft.nhsv.admin.domain.CopyTradingRegister_;
import com.difisoft.nhsv.admin.repository.CopyTradingRegisterRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyTradingRegisterCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyTradingRegisterDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyTradingRegisterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

import java.util.List;

/**
 * Service for executing complex queries for {@link CopyTradingRegister} entities in the database.
 * The main input is a {@link CopyTradingRegisterCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopyTradingRegisterDTO} or a {@link Page} of {@link CopyTradingRegisterDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopyTradingRegisterQueryService extends QueryService<CopyTradingRegister> {
    private final Logger log = LoggerFactory.getLogger(CopyTradingRegisterQueryService.class);
    private final CopyTradingRegisterRepository copyTradingRegisterRepository;
    private final CopyTradingRegisterMapper copyTradingRegisterMapper;

    public CopyTradingRegisterQueryService(
        CopyTradingRegisterRepository copyTradingRegisterRepository,
        CopyTradingRegisterMapper copyTradingRegisterMapper
    ) {
        this.copyTradingRegisterRepository = copyTradingRegisterRepository;
        this.copyTradingRegisterMapper = copyTradingRegisterMapper;
    }

    /**
     * Return a {@link List} of {@link CopyTradingRegisterDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopyTradingRegisterDTO> findByCriteria(CopyTradingRegisterCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyTradingRegister> specification = createSpecification(criteria);
        return copyTradingRegisterMapper.toDto(copyTradingRegisterRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopyTradingRegisterDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopyTradingRegisterDTO> findByCriteria(CopyTradingRegisterCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyTradingRegister> specification = createSpecification(criteria);
        return copyTradingRegisterRepository.findAll(specification, page).map(copyTradingRegisterMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopyTradingRegisterCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyTradingRegister> specification = createSpecification(criteria);
        return copyTradingRegisterRepository.count(specification);
    }

    /**
     * Function to convert {@link CopyTradingRegisterCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopyTradingRegister> createSpecification(CopyTradingRegisterCriteria criteria) {
        Specification<CopyTradingRegister> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyTradingRegister_.id));
            }
            if (criteria.getAccountNumber() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getAccountNumber(), CopyTradingRegister_.accountNumber));
            }
            if (criteria.getSubAccount() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSubAccount(), CopyTradingRegister_.subAccount));
            }
            if (criteria.getCustomerName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCustomerName(), CopyTradingRegister_.customerName));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), CopyTradingRegister_.status));
            }
            if (criteria.getCreateAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreateAt(), CopyTradingRegister_.createAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), CopyTradingRegister_.updatedAt));
            }
        }
        return specification;
    }
}
