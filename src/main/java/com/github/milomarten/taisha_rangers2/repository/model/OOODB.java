package com.github.milomarten.taisha_rangers2.repository.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "OOO")
@Getter
@Setter
@NoArgsConstructor
public class OOODB {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAYER_ID")
    private PlayerDB player;
    @Column(name = "START_TIME", nullable = false)
    Instant start;
    @Column(name = "END_TIME", nullable = false)
    Instant end;
}
