package com.sporty.settlement.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "failed_payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "failed_payouts_gen")
    @SequenceGenerator(name = "failed_payouts_gen", sequenceName = "failed_payouts_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant failedAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;
}
