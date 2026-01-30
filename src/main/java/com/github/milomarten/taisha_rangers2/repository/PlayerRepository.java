package com.github.milomarten.taisha_rangers2.repository;

import com.github.milomarten.taisha_rangers2.repository.model.PlayerDB;
import discord4j.common.util.Snowflake;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends CrudRepository<PlayerDB, Long> {
    Optional<PlayerDB> findBySnowflake(Snowflake snowflake);
}
