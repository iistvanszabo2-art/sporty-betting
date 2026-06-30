package com.sporty.settlement.repository;

import com.sporty.settlement.model.SettledBet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettledBetRepository extends JpaRepository<SettledBet, Long> {

    boolean existsByBetId(Long betId);
}
