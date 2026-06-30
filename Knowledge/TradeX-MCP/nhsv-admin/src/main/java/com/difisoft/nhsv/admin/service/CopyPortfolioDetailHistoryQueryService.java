package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailHistoryRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioDetailHistoryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioDetailHistoryMapper;
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
 * Service for executing complex queries for {@link CopyPortfolioDetailHistory} entities in the database.
 * The main input is a {@link CopyPortfolioDetailHistoryCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopyPortfolioDetailHistoryDTO} or a {@link Page} of {@link CopyPortfolioDetailHistoryDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopyPortfolioDetailHistoryQueryService extends QueryService<CopyPortfolioDetailHistory> {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioDetailHistoryQueryService.class);

    private final CopyPortfolioDetailHistoryRepository copyPortfolioDetailHistoryRepository;

    private final CopyPortfolioDetailHistoryMapper copyPortfolioDetailHistoryMapper;

    public CopyPortfolioDetailHistoryQueryService(
        CopyPortfolioDetailHistoryRepository copyPortfolioDetailHistoryRepository,
        CopyPortfolioDetailHistoryMapper copyPortfolioDetailHistoryMapper
    ) {
        this.copyPortfolioDetailHistoryRepository = copyPortfolioDetailHistoryRepository;
        this.copyPortfolioDetailHistoryMapper = copyPortfolioDetailHistoryMapper;
    }

    /**
     * Return a {@link List} of {@link CopyPortfolioDetailHistoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopyPortfolioDetailHistoryDTO> findByCriteria(CopyPortfolioDetailHistoryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyPortfolioDetailHistory> specification = createSpecification(criteria);
        return copyPortfolioDetailHistoryMapper.toDto(copyPortfolioDetailHistoryRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopyPortfolioDetailHistoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopyPortfolioDetailHistoryDTO> findByCriteria(CopyPortfolioDetailHistoryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyPortfolioDetailHistory> specification = createSpecification(criteria);
        return copyPortfolioDetailHistoryRepository.findAll(specification, page).map(copyPortfolioDetailHistoryMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopyPortfolioDetailHistoryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyPortfolioDetailHistory> specification = createSpecification(criteria);
        return copyPortfolioDetailHistoryRepository.count(specification);
    }

    /**
     * Function to convert {@link CopyPortfolioDetailHistoryCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopyPortfolioDetailHistory> createSpecification(CopyPortfolioDetailHistoryCriteria criteria) {
        Specification<CopyPortfolioDetailHistory> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyPortfolioDetailHistory_.id));
            }
            if (criteria.getSymbol() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSymbol(), CopyPortfolioDetailHistory_.symbol));
            }
            if (criteria.getWeight() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getWeight(), CopyPortfolioDetailHistory_.weight));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopyPortfolioDetailHistory_.createdAt));
            }
            if (criteria.getCopyPortfolioHistoryIdId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCopyPortfolioHistoryIdId(),
                            root ->
                                root.join(CopyPortfolioDetailHistory_.copyPortfolioHistoryId, JoinType.LEFT).get(CopyPortfolioHistory_.id)
                        )
                    );
            }
            if (criteria.getCopyPortfolioHistoryId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCopyPortfolioHistoryId(),
                            root -> root.join(CopyPortfolioDetailHistory_.copyPortfolioHistoryId, JoinType.LEFT).get(CopyPortfolioHistory_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
