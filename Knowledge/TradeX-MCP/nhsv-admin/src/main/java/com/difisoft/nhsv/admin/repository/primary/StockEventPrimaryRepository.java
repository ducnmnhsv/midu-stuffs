package com.difisoft.nhsv.admin.repository.primary;

import com.difisoft.nhsv.admin.domain.StockEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
@Primary
public interface StockEventPrimaryRepository extends JpaRepository<StockEvent, String> {
    List<StockEvent> findByIsAdjustedFalseAndEffectiveDateLessThanEqual(ZonedDateTime date);
}
