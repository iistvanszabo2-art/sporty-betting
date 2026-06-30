package com.sporty.outcome.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "failed_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedOutboxEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "failed_outbox_gen")
    @SequenceGenerator(name = "failed_outbox_gen", sequenceName = "failed_outbox_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant failedAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;
}
