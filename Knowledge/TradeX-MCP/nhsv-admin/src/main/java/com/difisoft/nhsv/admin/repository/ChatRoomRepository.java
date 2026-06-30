package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.ChatRoom;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ChatRoom entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, JpaSpecificationExecutor<ChatRoom> {}
