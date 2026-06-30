package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CreatedChatRoom entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CreatedChatRoomRepository extends JpaRepository<CreatedChatRoom, Long>, JpaSpecificationExecutor<CreatedChatRoom> {}
