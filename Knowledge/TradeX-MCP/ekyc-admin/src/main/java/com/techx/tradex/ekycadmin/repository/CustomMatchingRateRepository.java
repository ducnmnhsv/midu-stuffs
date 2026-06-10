package com.techx.tradex.ekycadmin.repository;

import com.techx.tradex.ekycadmin.domain.MatchingRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomMatchingRateRepository extends JpaRepository<MatchingRate, Long> {
    Optional<MatchingRate> findByCore(String core);
}
