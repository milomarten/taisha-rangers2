package com.github.milomarten.taisha_rangers2.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class OOODB {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAYER_ID")
    private PlayerDB player;
    @Column(name = "START_TIME")
    Instant start;
    @Column(name = "END_TIME")
    Instant end;
}
