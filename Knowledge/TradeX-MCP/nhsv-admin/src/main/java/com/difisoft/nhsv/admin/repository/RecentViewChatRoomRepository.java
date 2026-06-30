package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.RecentViewChatRoom;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the RecentViewChatRoom entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RecentViewChatRoomRepository extends JpaRepository<RecentViewChatRoom, Long> {}
