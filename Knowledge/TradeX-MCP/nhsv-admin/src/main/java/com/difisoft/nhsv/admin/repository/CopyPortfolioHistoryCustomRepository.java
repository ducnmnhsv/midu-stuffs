package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Predicate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@Primary
public interface CopyPortfolioHistoryCustomRepository extends CopyPortfolioHistoryRepository {
    default Page<CopyPortfolioHistory> findByMlUserIdId(Long cpId, Date fromDate, Date toDate, Pageable pageable) {
        return this.findAll((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("mlUserId").get("id"), cpId)));
                if(fromDate != null) {
                    ZonedDateTime zmFromDate = ZonedDateTime.ofInstant(fromDate.toInstant(), ZoneId.systemDefault()).with(LocalTime.MIN);
                    predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), zmFromDate)));
                }
                if(toDate != null) {
                    ZonedDateTime zmToDate = ZonedDateTime.ofInstant(toDate.toInstant(), ZoneId.systemDefault()).with(LocalTime.MAX);
                    predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), zmToDate)));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            },
            pageable);
    }
}
