package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.CopyTradingOrder;
import com.difisoft.nhsv.admin.repository.CopyTradingOrderRepository;
import com.difisoft.nhsv.admin.service.criteria.CopyTradingOrderCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
import com.difisoft.nhsv.admin.service.mapper.CopyTradingOrderMapper;
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
 * Service for executing complex queries for {@link CopyTradingOrder} entities in the database.
 * The main input is a {@link CopyTradingOrderCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CopyTradingOrderDTO} or a {@link Page} of {@link CopyTradingOrderDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CopyTradingOrderQueryService extends QueryService<CopyTradingOrder> {

    private final Logger log = LoggerFactory.getLogger(CopyTradingOrderQueryService.class);

    private final CopyTradingOrderRepository copyTradingOrderRepository;

    private final CopyTradingOrderMapper copyTradingOrderMapper;

    public CopyTradingOrderQueryService(
        CopyTradingOrderRepository copyTradingOrderRepository,
        CopyTradingOrderMapper copyTradingOrderMapper
    ) {
        this.copyTradingOrderRepository = copyTradingOrderRepository;
        this.copyTradingOrderMapper = copyTradingOrderMapper;
    }

    /**
     * Return a {@link List} of {@link CopyTradingOrderDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CopyTradingOrderDTO> findByCriteria(CopyTradingOrderCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<CopyTradingOrder> specification = createSpecification(criteria);
        return copyTradingOrderMapper.toDto(copyTradingOrderRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link CopyTradingOrderDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<CopyTradingOrderDTO> findByCriteria(CopyTradingOrderCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<CopyTradingOrder> specification = createSpecification(criteria);
        return copyTradingOrderRepository.findAll(specification, page).map(copyTradingOrderMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CopyTradingOrderCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<CopyTradingOrder> specification = createSpecification(criteria);
        return copyTradingOrderRepository.count(specification);
    }

    /**
     * Function to convert {@link CopyTradingOrderCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CopyTradingOrder> createSpecification(CopyTradingOrderCriteria criteria) {
        Specification<CopyTradingOrder> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), CopyTradingOrder_.id));
            }
            if (criteria.getJobId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getJobId(), CopyTradingOrder_.jobId));
            }
            if (criteria.getSymbol() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSymbol(), CopyTradingOrder_.symbol));
            }
            if (criteria.getFee() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFee(), CopyTradingOrder_.fee));
            }
            if (criteria.getTax() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTax(), CopyTradingOrder_.tax));
            }
            if (criteria.getOrderNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOrderNumber(), CopyTradingOrder_.orderNumber));
            }
            if (criteria.getSellBuyType() != null) {
                specification = specification.and(buildSpecification(criteria.getSellBuyType(), CopyTradingOrder_.sellBuyType));
            }
            if (criteria.getExchangeType() != null) {
                specification = specification.and(buildSpecification(criteria.getExchangeType(), CopyTradingOrder_.exchangeType));
            }
            if (criteria.getOrderType() != null) {
                specification = specification.and(buildSpecification(criteria.getOrderType(), CopyTradingOrder_.orderType));
            }
            if (criteria.getOrderQuantity() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrderQuantity(), CopyTradingOrder_.orderQuantity));
            }
            if (criteria.getOrderPrice() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOrderPrice(), CopyTradingOrder_.orderPrice));
            }
            if (criteria.getApiParam() != null) {
                specification = specification.and(buildStringSpecification(criteria.getApiParam(), CopyTradingOrder_.apiParam));
            }
            if (criteria.getApiStatusCode() != null) {
                specification = specification.and(buildStringSpecification(criteria.getApiStatusCode(), CopyTradingOrder_.apiStatusCode));
            }
            if (criteria.getApiErrorMessage() != null) {
                specification =
                    specification.and(buildStringSpecification(criteria.getApiErrorMessage(), CopyTradingOrder_.apiErrorMessage));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedAt(), CopyTradingOrder_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getUpdatedAt(), CopyTradingOrder_.updatedAt));
            }
            if (criteria.getCopySubscriberId() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getCopySubscriberId(), CopyTradingOrder_.copySubscriberId));
            }
            if (criteria.getCopyPortfolioId() != null) {
                specification =
                    specification.and(buildRangeSpecification(criteria.getCopyPortfolioId(), CopyTradingOrder_.copyPortfolioId));
            }
        }
        return specification;
    }
}
