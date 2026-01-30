package com.github.milomarten.taisha_rangers2.model;

import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "PARTY")
@Data
public class PartyDB {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    private Snowflake dm;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "PARTY_PLAYERS",
            joinColumns = @JoinColumn(name = "PARTY", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "PLAYER", referencedColumnName = "ID")
    )
    private List<PlayerDB> players;
    private Snowflake ping;
    private Locale locale;
    private DayOfWeek usualDayOfWeek;
    private LocalTime usualTime;
    private ZoneId usualTimezone;
}
