package com.sporty.outcome.repository;

import com.sporty.outcome.model.OutboxEntry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntry, Long> {
    List<OutboxEntry> findBySentAtIsNull(PageRequest page);
    void deleteBySentAtBefore(Instant cutoff);
}
