package com.difisoft.nhsv.admin.repository;

import com.difisoft.nhsv.admin.domain.Holiday;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Holiday entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {}
