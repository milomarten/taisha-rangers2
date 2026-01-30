package com.github.milomarten.taisha_rangers2.repository;

import com.github.milomarten.taisha_rangers2.repository.model.SessionDB;
import discord4j.common.util.Snowflake;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<SessionDB, Long> {
    SessionDB findFirstByChannelOrderByCreatedDateDesc(Snowflake channel);
    boolean existsByChannelAndComplete(Snowflake channel, boolean complete);
}
