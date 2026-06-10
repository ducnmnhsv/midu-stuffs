package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.TtlIssuePlaceCodeMap;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the TtlIssuePlaceCodeMap entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TtlIssuePlaceCodeMapRepository extends JpaRepository<TtlIssuePlaceCodeMap, Long> {}
