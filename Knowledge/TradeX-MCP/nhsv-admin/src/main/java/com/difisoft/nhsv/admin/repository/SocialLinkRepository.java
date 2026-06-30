package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.SocialLink;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SocialLink entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {}
