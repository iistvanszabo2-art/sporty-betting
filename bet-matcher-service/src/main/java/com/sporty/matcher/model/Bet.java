package com.sporty.matcher.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bets_gen")
    @SequenceGenerator(name = "bets_gen", sequenceName = "bets_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long eventMarketId;

    @Column(nullable = false)
    private Long eventWinnerId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status;

    @Version
    private Long version;
}
