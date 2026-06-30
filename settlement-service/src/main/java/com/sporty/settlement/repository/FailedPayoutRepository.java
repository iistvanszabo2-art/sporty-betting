package com.sporty.settlement.repository;

import com.sporty.settlement.model.FailedPayout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedPayoutRepository extends JpaRepository<FailedPayout, Long> {
}
