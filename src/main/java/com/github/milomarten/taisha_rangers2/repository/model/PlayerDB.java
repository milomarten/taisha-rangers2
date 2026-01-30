package com.github.milomarten.taisha_rangers2.repository.model;

import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZoneId;
import java.util.List;

@Entity
@Table(name = "PLAYER")
@Getter
@Setter
@NoArgsConstructor
public class PlayerDB {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    @Column(unique = true, nullable = false)
    private Snowflake snowflake;
    private ZoneId usualTimezone;
    @OneToMany(mappedBy = "player")
    private List<OOODB> ooos;
}
