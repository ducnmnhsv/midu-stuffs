package com.difisoft.nhsv.admin.repository.primary;

import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.domain.enumeration.StatusEnum;
import com.difisoft.nhsv.admin.repository.CreatedChatRoomRepository;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface CreatedChatRoomPrimaryRepository extends CreatedChatRoomRepository {
        List<CreatedChatRoom> findAllByBrokerId(Long brokerId);

        List<CreatedChatRoom> findByGroupNameAndBrokerName(String groupName, String brokerName);

        @Query("select c from CreatedChatRoom c where c.brokerId = :brokerId and c.status = :status")
        List<CreatedChatRoom> findByBrokerIdAndStatus(@Param("brokerId") Long brokerId,
                        @Param("status") StatusEnum status);

        @Query("SELECT c from CreatedChatRoom c, Broker b" +
                        " WHERE c.brokerId = b.id AND b.status = true AND  c.status = :status AND" +
                        " (c.groupName LIKE :keyword OR c.introduction LIKE :keyword OR c.groupOwner LIKE :keyword)")
        Page<CreatedChatRoom> findAllByStatusWithKeyWord(@Param("status") StatusEnum status,
                        @Param("keyword") String keyword, Pageable pageable);

        @Query("SELECT c from CreatedChatRoom c, Broker b" +
                        " WHERE c.brokerId = b.id AND b.status = true AND c.status = :status")
        Page<CreatedChatRoom> findAllByStatus(@Param("status") StatusEnum status, Pageable pageable);

}
