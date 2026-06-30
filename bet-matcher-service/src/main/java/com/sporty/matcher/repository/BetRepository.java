package com.sporty.matcher.repository;

import com.sporty.matcher.model.Bet;
import com.sporty.matcher.model.BetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    Page<Bet> findByEventIdAndStatus(Long eventId, BetStatus status, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Bet b SET b.status = :newStatus, b.version = b.version + 1 WHERE b.id IN :ids AND b.status = :expected")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("expected") BetStatus expected, @Param("newStatus") BetStatus newStatus);
}
