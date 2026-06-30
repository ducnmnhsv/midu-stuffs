package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import com.difisoft.nhsv.admin.repository.CopyMarketLeaderDetailsRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyMarketLeaderDetailsCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyMarketLeaderDetailsMapper;
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
 * Service for executing complex queries for {@link CopyMarketLeaderDetails} entities in the database.
 * The main input is a {@link CopyMarketLeaderDetailsCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopyMarketLeaderDetailsDTO} or a {@link Page} of {@link CopyMarketLeaderDetailsDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopyMarketLeaderDetailsQueryService extends QueryService<CopyMarketLeaderDetails> {

    private final Logger log = LoggerFactory.getLogger(CopyMarketLeaderDetailsQueryService.class);

    private final CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository;

    private final CopyMarketLeaderDetailsMapper copyMarketLeaderDetailsMapper;

    public CopyMarketLeaderDetailsQueryService(
        CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository,
        CopyMarketLeaderDetailsMapper copyMarketLeaderDetailsMapper
    ) {
        this.copyMarketLeaderDetailsRepository = copyMarketLeaderDetailsRepository;
        this.copyMarketLeaderDetailsMapper = copyMarketLeaderDetailsMapper;
    }

    /**
     * Return a {@link List} of {@link CopyMarketLeaderDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopyMarketLeaderDetailsDTO> findByCriteria(CopyMarketLeaderDetailsCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyMarketLeaderDetails> specification = createSpecification(criteria);
        return copyMarketLeaderDetailsMapper.toDto(copyMarketLeaderDetailsRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopyMarketLeaderDetailsDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopyMarketLeaderDetailsDTO> findByCriteria(CopyMarketLeaderDetailsCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyMarketLeaderDetails> specification = createSpecification(criteria);
        return copyMarketLeaderDetailsRepository.findAll(specification, page).map(copyMarketLeaderDetailsMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopyMarketLeaderDetailsCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyMarketLeaderDetails> specification = createSpecification(criteria);
        return copyMarketLeaderDetailsRepository.count(specification);
    }

    /**
     * Function to convert {@link CopyMarketLeaderDetailsCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopyMarketLeaderDetails> createSpecification(CopyMarketLeaderDetailsCriteria criteria) {
        Specification<CopyMarketLeaderDetails> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyMarketLeaderDetails_.id));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopyMarketLeaderDetails_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), CopyMarketLeaderDetails_.updatedAt));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildStringSpecification(criteria.getType(), CopyMarketLeaderDetails_.type));
            }
            if (criteria.getLabel() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLabel(), CopyMarketLeaderDetails_.label));
            }
            if (criteria.getKey() != null) {
                specification = specification.and(buildStringSpecification(criteria.getKey(), CopyMarketLeaderDetails_.key));
            }
            if (criteria.getValue() != null) {
                specification = specification.and(buildStringSpecification(criteria.getValue(), CopyMarketLeaderDetails_.value));
            }
            if (criteria.getMlUserIdId() != null) {
                specification =
                    specification.and(
                        buildSpecification(
                            criteria.getMlUserIdId(),
                            root -> root.join(CopyMarketLeaderDetails_.mlUserId, JoinType.LEFT).get(User_.id)
                        )
                    );
            }
        }
        return specification;
    }
}
