package com.github.milomarten.taisha_rangers2.model;

import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "RESPONSES")
@Data
public class PlayerResponseDB {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SESSION_ID")
    private SessionDB session;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PLAYER_ID")
    private PlayerDB player;
    private PlayerResponse.State state;
    private ZoneId timezone;
    private Instant timeOne;
    private Instant timeTwo;
}
