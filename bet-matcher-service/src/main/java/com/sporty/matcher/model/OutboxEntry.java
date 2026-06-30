package com.sporty.matcher.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_gen")
    @SequenceGenerator(name = "outbox_gen", sequenceName = "outbox_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long betId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    private String correlationId;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    @Builder.Default
    @Column(nullable = false)
    private int retryCount = 0;
}
