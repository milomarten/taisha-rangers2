package com.github.milomarten.taisha_rangers2.repository.model;

import discord4j.common.util.Snowflake;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "SESSION")
@Getter
@Setter
@NoArgsConstructor
public class SessionDB {
    @Id
    @GeneratedValue
    private long id;
    @Column(nullable = false)
    private Snowflake channel;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GROUP_ID")
    private PartyDB group;
    @OneToMany(mappedBy = "session", fetch = FetchType.EAGER)
    private List<PlayerResponseDB> playerResponses;
    @Column(nullable = false)
    private Instant proposedStartTime;
    private Instant startTime;
    private boolean complete;
    @CreationTimestamp
    private Instant createdDate;
}
