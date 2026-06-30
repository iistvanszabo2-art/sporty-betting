package com.sporty.outcome.repository;

import com.sporty.outcome.model.FailedOutboxEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedOutboxRepository extends JpaRepository<FailedOutboxEntry, Long> {
}
