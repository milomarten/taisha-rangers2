package com.github.milomarten.taisha_rangers2.model;

import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "SESSION")
@Data
public class SessionDB {
    @Id
    @GeneratedValue
    private long id;
    private Snowflake channel;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GROUP_ID")
    private PartyDB group;
    @OneToMany(mappedBy = "session", fetch = FetchType.EAGER)
    private List<PlayerResponseDB> playerResponses;
    private Instant proposedStartTime;
    private Instant startTime;
    @CreationTimestamp
    private Instant createdDate;
}
