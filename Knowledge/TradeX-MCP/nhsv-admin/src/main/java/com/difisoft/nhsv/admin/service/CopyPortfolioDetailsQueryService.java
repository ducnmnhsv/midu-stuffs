package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopyPortfolioDetails;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailsRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioDetailsCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioDetailsMapper;
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
 * Service for executing complex queries for {@link CopyPortfolioDetails} entities in the database.
 * The main input is a {@link CopyPortfolioDetailsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopyPortfolioDetailsDTO} or a {@link Page} of {@link CopyPortfolioDetailsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopyPortfolioDetailsQueryService extends QueryService<CopyPortfolioDetails> {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioDetailsQueryService.class);

    private final CopyPortfolioDetailsRepository copyPortfolioDetailsRepository;

    private final CopyPortfolioDetailsMapper copyPortfolioDetailsMapper;

    public CopyPortfolioDetailsQueryService(
        CopyPortfolioDetailsRepository copyPortfolioDetailsRepository,
        CopyPortfolioDetailsMapper copyPortfolioDetailsMapper
    ) {
        this.copyPortfolioDetailsRepository = copyPortfolioDetailsRepository;
        this.copyPortfolioDetailsMapper = copyPortfolioDetailsMapper;
    }

    /**
     * Return a {@link List} of {@link CopyPortfolioDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopyPortfolioDetailsDTO> findByCriteria(CopyPortfolioDetailsCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyPortfolioDetails> specification = createSpecification(criteria);
        return copyPortfolioDetailsMapper.toDto(copyPortfolioDetailsRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopyPortfolioDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopyPortfolioDetailsDTO> findByCriteria(CopyPortfolioDetailsCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyPortfolioDetails> specification = createSpecification(criteria);
        return copyPortfolioDetailsRepository.findAll(specification, page).map(copyPortfolioDetailsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopyPortfolioDetailsCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyPortfolioDetails> specification = createSpecification(criteria);
        return copyPortfolioDetailsRepository.count(specification);
    }

    /**
     * Function to convert {@link CopyPortfolioDetailsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopyPortfolioDetails> createSpecification(CopyPortfolioDetailsCriteria criteria) {
        Specification<CopyPortfolioDetails> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyPortfolioDetails_.id));
            }
            if (criteria.getSymbol() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSymbol(), CopyPortfolioDetails_.symbol));
            }
            if (criteria.getWeight() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getWeight(), CopyPortfolioDetails_.weight));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopyPortfolioDetails_.createdAt));
            }
            if (criteria.getCopyPortfolioIdId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCopyPortfolioIdId(),
                            root -> root.join(CopyPortfolioDetails_.copyPortfolioId, JoinType.LEFT).get(CopyPortfolio_.id)
                        )
                    );
            }
            if (criteria.getCopyPortfolioId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCopyPortfolioId(),
                            root -> root.join(CopyPortfolioDetails_.copyPortfolioId, JoinType.LEFT).get(CopyPortfolio_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
