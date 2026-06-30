package com.sporty.settlement.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settled_bets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SettledBet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "settled_bets_gen")
    @SequenceGenerator(name = "settled_bets_gen", sequenceName = "settled_bets_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long betId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private boolean won;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime settledAt;
}
