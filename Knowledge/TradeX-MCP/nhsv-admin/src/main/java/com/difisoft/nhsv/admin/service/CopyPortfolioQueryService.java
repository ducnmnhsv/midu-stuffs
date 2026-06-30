package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import com.difisoft.nhsv.admin.repository.CopyPortfolioRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioMapper;
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
 * Service for executing complex queries for {@link CopyPortfolio} entities in the database.
 * The main input is a {@link CopyPortfolioCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopyPortfolioDTO} or a {@link Page} of {@link CopyPortfolioDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopyPortfolioQueryService extends QueryService<CopyPortfolio> {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioQueryService.class);

    private final CopyPortfolioRepository copyPortfolioRepository;

    private final CopyPortfolioMapper copyPortfolioMapper;

    public CopyPortfolioQueryService(CopyPortfolioRepository copyPortfolioRepository, CopyPortfolioMapper copyPortfolioMapper) {
        this.copyPortfolioRepository = copyPortfolioRepository;
        this.copyPortfolioMapper = copyPortfolioMapper;
    }

    /**
     * Return a {@link List} of {@link CopyPortfolioDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopyPortfolioDTO> findByCriteria(CopyPortfolioCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyPortfolio> specification = createSpecification(criteria);
        return copyPortfolioMapper.toDto(copyPortfolioRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopyPortfolioDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopyPortfolioDTO> findByCriteria(CopyPortfolioCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyPortfolio> specification = createSpecification(criteria);
        return copyPortfolioRepository.findAll(specification, page).map(copyPortfolioMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopyPortfolioCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyPortfolio> specification = createSpecification(criteria);
        return copyPortfolioRepository.count(specification);
    }

    /**
     * Function to convert {@link CopyPortfolioCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopyPortfolio> createSpecification(CopyPortfolioCriteria criteria) {
        Specification<CopyPortfolio> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyPortfolio_.id));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopyPortfolio_.createdAt));
            }
            if (criteria.getCopyPortfolioDetailsId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCopyPortfolioDetailsId(),
                            root -> root.join(CopyPortfolio_.copyPortfolioDetails, JoinType.LEFT).get(CopyPortfolioDetails_.id)
                        )
                    );
            }
            if (criteria.getMlUserIdId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getMlUserIdId(),
                            root -> root.join(CopyPortfolio_.mlUserId, JoinType.LEFT).get(User_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
