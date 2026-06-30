package com.sporty.matcher.repository;

import com.sporty.matcher.model.FailedOutboxEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedOutboxRepository extends JpaRepository<FailedOutboxEntry, Long> {
}
