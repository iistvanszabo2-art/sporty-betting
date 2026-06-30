package com.sporty.settlement.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payout_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutOutboxEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payout_outbox_gen")
    @SequenceGenerator(name = "payout_outbox_gen", sequenceName = "payout_outbox_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    @Builder.Default
    @Column(nullable = false)
    private int retryCount = 0;
}
