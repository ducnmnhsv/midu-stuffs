package com.techx.tradex.notification.repository;

import com.techx.tradex.notification.model.db.FptSmsHistory;
import org.springframework.data.repository.CrudRepository;


public interface FptSmsHistoryRepository extends CrudRepository<FptSmsHistory, Long> {
}