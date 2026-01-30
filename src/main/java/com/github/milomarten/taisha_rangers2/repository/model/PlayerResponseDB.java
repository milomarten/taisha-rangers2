package com.github.milomarten.taisha_rangers2.repository.model;

import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;

@Entity
@Table(name = "RESPONSES")
@Getter
@Setter
@NoArgsConstructor
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
