package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import com.difisoft.nhsv.admin.repository.CopyPortfolioHistoryRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioHistoryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyPortfolioHistoryMapper;
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
 * Service for executing complex queries for {@link CopyPortfolioHistory} entities in the database.
 * The main input is a {@link CopyPortfolioHistoryCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopyPortfolioHistoryDTO} or a {@link Page} of {@link CopyPortfolioHistoryDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopyPortfolioHistoryQueryService extends QueryService<CopyPortfolioHistory> {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioHistoryQueryService.class);

    private final CopyPortfolioHistoryRepository copyPortfolioHistoryRepository;

    private final CopyPortfolioHistoryMapper copyPortfolioHistoryMapper;

    public CopyPortfolioHistoryQueryService(
        CopyPortfolioHistoryRepository copyPortfolioHistoryRepository,
        CopyPortfolioHistoryMapper copyPortfolioHistoryMapper
    ) {
        this.copyPortfolioHistoryRepository = copyPortfolioHistoryRepository;
        this.copyPortfolioHistoryMapper = copyPortfolioHistoryMapper;
    }

    /**
     * Return a {@link List} of {@link CopyPortfolioHistoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopyPortfolioHistoryDTO> findByCriteria(CopyPortfolioHistoryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyPortfolioHistory> specification = createSpecification(criteria);
        return copyPortfolioHistoryMapper.toDto(copyPortfolioHistoryRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopyPortfolioHistoryDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopyPortfolioHistoryDTO> findByCriteria(CopyPortfolioHistoryCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyPortfolioHistory> specification = createSpecification(criteria);
        return copyPortfolioHistoryRepository.findAll(specification, page).map(copyPortfolioHistoryMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopyPortfolioHistoryCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyPortfolioHistory> specification = createSpecification(criteria);
        return copyPortfolioHistoryRepository.count(specification);
    }

    /**
     * Function to convert {@link CopyPortfolioHistoryCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopyPortfolioHistory> createSpecification(CopyPortfolioHistoryCriteria criteria) {
        Specification<CopyPortfolioHistory> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyPortfolioHistory_.id));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopyPortfolioHistory_.createdAt));
            }
            if (criteria.getCopyPortfolioDetailHistoryId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getCopyPortfolioDetailHistoryId(),
                            root ->
                                root
                                    .join(CopyPortfolioHistory_.copyPortfolioDetailHistories, JoinType.LEFT)
                                    .get(CopyPortfolioDetailHistory_.id)
                        )
                    );
            }
            if (criteria.getMlUserIdId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getMlUserIdId(),
                            root -> root.join(CopyPortfolioHistory_.mlUserId, JoinType.LEFT).get(User_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
