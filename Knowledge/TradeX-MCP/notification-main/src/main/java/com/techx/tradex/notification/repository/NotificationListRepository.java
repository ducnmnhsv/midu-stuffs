package com.techx.tradex.notification.repository;

import com.techx.tradex.notification.model.db.NotificationList;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NotificationListRepository extends CrudRepository<NotificationList, Long> {
    List<NotificationList> findByType(String type);
}