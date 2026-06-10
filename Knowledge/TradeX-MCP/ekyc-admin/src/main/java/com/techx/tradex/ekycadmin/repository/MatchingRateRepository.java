package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.MatchingRate;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the MatchingRate entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MatchingRateRepository extends JpaRepository<MatchingRate, Long> {}
