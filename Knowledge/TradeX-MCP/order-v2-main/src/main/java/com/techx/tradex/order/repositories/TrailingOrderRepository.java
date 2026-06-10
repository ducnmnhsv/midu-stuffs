package com.techx.tradex.order.repositories;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.TrailingOrderStatusEnum;
import com.techx.tradex.order.model.db.TrailingOrder;
import com.techx.tradex.order.utils.Utils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public interface TrailingOrderRepository extends JpaRepository<TrailingOrder, Long>, JpaSpecificationExecutor<TrailingOrder> {
    List<TrailingOrder> findByStatus(TrailingOrderStatusEnum status);


    TrailingOrder findByUsernameAndId(String username, Long id);

    default Page<TrailingOrder> findBy(String username, String accountNumber, String code, SellBuyTypeEnum sellBuyType, TrailingOrderStatusEnum status, Date fromDate, Date toDate, Long lastTrailingOrderId, Pageable pageable) {
        return this.findAll(new Specification<TrailingOrder>() {
            @Override
            public Predicate toPredicate(Root<TrailingOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("username"), username)));
                if (lastTrailingOrderId != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("id"), lastTrailingOrderId)));
                }
                if (accountNumber != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("accountNumber"), accountNumber)));
                }
                if (code != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("code"), code)));
                }
                if (sellBuyType != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("sellBuyType"), sellBuyType)));
                }
                if (status != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), status)));
                }
                if (fromDate != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate)));
                }
                if (toDate != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate)));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
    }

    default List<TrailingOrder> findTodayPendingTrailingOrder() {
        return this.findAll(new Specification<TrailingOrder>() {
            @Override
            public Predicate toPredicate(Root<TrailingOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                ZonedDateTime today = Utils.getCurrentMarketDate();
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), TrailingOrderStatusEnum.PENDING)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), today)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), today)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }
}
