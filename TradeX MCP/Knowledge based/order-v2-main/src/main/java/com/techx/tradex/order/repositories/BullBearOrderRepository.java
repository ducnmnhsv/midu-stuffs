package com.techx.tradex.order.repositories;

import com.difisoft.model.constants.BullBearOrderStatusEnum;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.techx.tradex.order.model.db.BullBearOrder;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BullBearOrderRepository extends JpaRepository<BullBearOrder, Long>, JpaSpecificationExecutor<BullBearOrder> {
    BullBearOrder findByIdAndStatus(Long id, BullBearOrderStatusEnum status);

    List<BullBearOrder> findByStatus(BullBearOrderStatusEnum status);

    BullBearOrder findByUsernameAndId(String username, Long id);

    Optional<BullBearOrder> findById(Long id);

    default Page<BullBearOrder> findBy(String username, String accountNumber, String code, SellBuyTypeEnum sellBuyType, BullBearOrderStatusEnum status, Date fromDate, Date toDate, Pageable pageable) {
        return this.findAll(new Specification<BullBearOrder>() {
            @Override
            public Predicate toPredicate(Root<BullBearOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("username"), username)));
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
}
