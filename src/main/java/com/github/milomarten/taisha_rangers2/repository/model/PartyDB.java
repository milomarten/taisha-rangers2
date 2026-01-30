package com.github.milomarten.taisha_rangers2.repository.model;

import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "PARTY")
@Getter
@Setter
@NoArgsConstructor
public class PartyDB {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false)
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
