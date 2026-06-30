package com.difisoft.nhsv.admin.repository.primary;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.repository.ChatRoomRepository;

@Repository
@Primary
public interface ChatRoomPrimaryRepository extends ChatRoomRepository {
    @Query("select c from ChatRoom c,Broker b where c.brokerName = b.username and b.id = :brokerId")
    List<ChatRoom> findByBrokerId(@Param("brokerId") Long brokerId);

    List<ChatRoom> findByGroupName(String groupName);

    List<ChatRoom> findByGroupNameAndBrokerName(String groupName, String brokerName);

    List<User> findByBrokerName(String name);
}
