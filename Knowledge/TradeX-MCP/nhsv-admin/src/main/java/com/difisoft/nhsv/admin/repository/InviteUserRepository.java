package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.InviteUser;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InviteUser entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InviteUserRepository extends JpaRepository<InviteUser, Long>, JpaSpecificationExecutor<InviteUser> {}
