package com.sporty.matcher.repository;

import com.sporty.matcher.model.OutboxEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntry, Long> {

    List<OutboxEntry> findBySentAtIsNull(Pageable pageable);
    void deleteBySentAtBefore(Instant cutoff);
}
