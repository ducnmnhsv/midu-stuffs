package com.techx.tradex.order.repositories;

import com.difisoft.model.constants.SellBuyTypeEnum;
import com.difisoft.model.constants.StopOrderStatusEnum;
import com.difisoft.model.constants.StopOrderTypeEnum;
import com.techx.tradex.order.model.db.StopOrder;
import com.techx.tradex.order.utils.Utils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface StopOrderRepository extends JpaRepository<StopOrder, Long>, JpaSpecificationExecutor<StopOrder> {
    StopOrder findByIdAndStatus(Long id, StopOrderStatusEnum status);

    StopOrder findByUsernameAndId(String username, Long id);

    default List<StopOrder> findExistedStopOrder(String code, double stopPrice, Long stopVolume,
                                                 SellBuyTypeEnum sellBuyType, Double orderPrice, String accountNumber,
                                                 String subNumber, ZonedDateTime fromDate, ZonedDateTime toDate) {
        return this.findAll(new Specification<StopOrder>() {
            @Override
            public Predicate toPredicate(Root<StopOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("code"), code)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("stopPrice"), stopPrice)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("sellBuyType"), sellBuyType)));
                if (orderPrice == null || orderPrice <= 0) {
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.isNull(root.get("orderPrice")),
                            criteriaBuilder.equal(root.get("orderType"), StopOrderTypeEnum.STOP)
                    ));
                } else {
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("orderPrice"), orderPrice),
                            criteriaBuilder.equal(root.get("orderType"), StopOrderTypeEnum.STOP_LIMIT)
                    ));
                }
                if (subNumber != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("subNumber"), subNumber)));
                } else {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("subNumber"))));
                }
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("accountNumber"), accountNumber)));
                if (stopVolume != null && stopVolume > 0) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("stopVolume"), stopVolume)));
                }
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), StopOrderStatusEnum.PENDING)));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.and(criteriaBuilder.between(root.get("fromDate"), fromDate, toDate)),
                        criteriaBuilder.and(criteriaBuilder.between(root.get("toDate"), fromDate, toDate)),
                        criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("fromDate"), fromDate), criteriaBuilder.greaterThanOrEqualTo(root.get("toDate"), toDate))
                ));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    default List<StopOrder> findStopOrderToSpeedCancel(
            String username, String accountNumber, String subNumber, SellBuyTypeEnum sellBuyType,
            String code, Double stopPrice, StopOrderStatusEnum status) {
        return this.findAll(new Specification<StopOrder>() {
            @Override
            public Predicate toPredicate(Root<StopOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("accountNumber"), accountNumber)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("orderType"), StopOrderTypeEnum.STOP)));
                if (username != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("username"), username)));
                }
                if (subNumber == null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("subNumber"), subNumber)));
                } else {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("subNumber"))));
                }
                if (sellBuyType != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("sellBuyType"), sellBuyType)));
                }
                if (code != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("code"), code)));
                }
                if (stopPrice != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("stopPrice"), stopPrice)));
                }
                if (status != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), status)));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    default Optional<StopOrder> findSpeedStopOrder(String code, double stopPrice, SellBuyTypeEnum sellBuyType,
                                                   String accountNumber, String subNumber, ZonedDateTime today) {
        return this.findOne((Specification<StopOrder>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("code"), code)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("stopPrice"), stopPrice)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("sellBuyType"), sellBuyType)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("orderType"), StopOrderTypeEnum.STOP)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("orderPrice"))));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("accountNumber"), accountNumber)));
            if (subNumber != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("subNumber"), subNumber)));
            } else {
                predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("subNumber"))));
            }
            predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get("fromDate"), today, today)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get("toDate"), today, today)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), StopOrderStatusEnum.PENDING)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    default List<StopOrder> findSameStopOrder(Long id, String code, double stopPrice, SellBuyTypeEnum sellBuyType,
                                              StopOrderTypeEnum orderType, Double orderPrice, String accountNumber,
                                              ZonedDateTime fromDate, ZonedDateTime toDate) {
        return this.findAll(new Specification<StopOrder>() {
            @Override
            public Predicate toPredicate(Root<StopOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("code"), code));
                predicates.add(criteriaBuilder.equal(root.get("stopPrice"), stopPrice));
                predicates.add(criteriaBuilder.equal(root.get("sellBuyType"), sellBuyType));
                predicates.add(criteriaBuilder.equal(root.get("orderType"), orderType));
                if (orderPrice == null || orderPrice <= 0) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("orderPrice"))));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("orderPrice"), orderPrice));
                }
                predicates.add(criteriaBuilder.equal(root.get("accountNumber"), accountNumber));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.and(criteriaBuilder.between(root.get("fromDate"), fromDate, toDate)),
                        criteriaBuilder.and(criteriaBuilder.between(root.get("toDate"), fromDate, toDate)),
                        criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("fromDate"), fromDate), criteriaBuilder.greaterThanOrEqualTo(root.get("toDate"), toDate))

                ));
                predicates.add(criteriaBuilder.equal(root.get("status"), StopOrderStatusEnum.PENDING));
                predicates.add(criteriaBuilder.notEqual(root.get("id"), id));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    default List<StopOrder> findSameStopOrderToModifySpeed(Long id, String code, double stopPrice, SellBuyTypeEnum sellBuyType, String accountNumber) {
        return this.findAll(new Specification<StopOrder>() {
            @Override
            public Predicate toPredicate(Root<StopOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get("id"), id)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("code"), code)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("stopPrice"), stopPrice)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("sellBuyType"), sellBuyType)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("orderType"), StopOrderTypeEnum.STOP)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("orderPrice"))));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("accountNumber"), accountNumber)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), StopOrderStatusEnum.PENDING)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    default List<StopOrder> findTodayPendingStopOrder() {
        return this.findAll(new Specification<StopOrder>() {
            @Override
            public Predicate toPredicate(Root<StopOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                ZonedDateTime today = Utils.getCurrentMarketDate();
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), StopOrderStatusEnum.PENDING)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("fromDate"), today)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("toDate"), today)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        }, Sort.by(Sort.Direction.ASC, "code").and(Sort.by(Sort.Direction.ASC, "id")));
    }

    default List<StopOrder> findExpiredStopOrder() {
        return this.findAll(new Specification<StopOrder>() {
            @Override
            public Predicate toPredicate(Root<StopOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                ZonedDateTime today = Utils.getCurrentMarketDate();
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), StopOrderStatusEnum.PENDING)));
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("toDate"), today)));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }

    default Page<StopOrder> findHistoryBy(String accountNumber, String username, String code, SellBuyTypeEnum sellBuyType, StopOrderTypeEnum orderType,
                                          StopOrderStatusEnum status, ZonedDateTime fromDate, ZonedDateTime toDate, Long lastStopOrderId, Pageable pageable) {
        return this.findAll(new Specification<StopOrder>() {
            @Override
            public Predicate toPredicate(Root<StopOrder> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (username != null) {
                    predicates.add(criteriaBuilder.equal(root.get("username"), username));
                }
                if (accountNumber != null) {
                    predicates.add(criteriaBuilder.equal(root.get("accountNumber"), accountNumber));
                }
                if (code != null) {
                    predicates.add(criteriaBuilder.equal(root.get("code"), code));
                }
                if (orderType != null) {
                    predicates.add(criteriaBuilder.equal(root.get("orderType"), orderType));
                }
                if (sellBuyType != null) {
                    predicates.add(criteriaBuilder.equal(root.get("sellBuyType"), sellBuyType));
                }
                if (status != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
                if (lastStopOrderId != null) {
                    predicates.add(criteriaBuilder.lessThan(root.get("id"), lastStopOrderId));
                }
                if (fromDate != null && toDate != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fromDate"), fromDate));
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("toDate"), toDate));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        }, pageable);
    }

    @Query(value = "FROM StopOrder s WHERE s.updatedAt >= :fromTime")
    List<StopOrder> findStopOrderLastUpdate(ZonedDateTime fromTime);
}
