package com.sporty.settlement.repository;

import com.sporty.settlement.model.PayoutOutboxEntry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PayoutOutboxRepository extends JpaRepository<PayoutOutboxEntry, Long> {
    List<PayoutOutboxEntry> findBySentAtIsNull(PageRequest page);
    void deleteBySentAtBefore(Instant cutoff);
}
