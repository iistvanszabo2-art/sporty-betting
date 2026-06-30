package com.sporty.outcome.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "events_gen")
    @SequenceGenerator(name = "events_gen", sequenceName = "events_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long eventId;

    @Column(nullable = false)
    private String name;

    private Long eventWinnerId;

    private LocalDateTime recordedAt;

    @Version
    private Long version;
}
