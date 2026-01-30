package com.github.milomarten.taisha_rangers2.model;

import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Entity
@Table(name = "PLAYER")
@Data
public class PlayerDB {
    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;
    private Snowflake snowflake;
    private ZoneId usualTimezone;
    @OneToMany(mappedBy = "player")
    private List<OOODB> ooos;
}
