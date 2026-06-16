package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import discord4j.common.util.Snowflake;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayerManager {
    private static final String KEY = "player";
    private static final TypeReference<Map<Snowflake, Player>> TYPE =
            new TypeReference<>() {};

    private final Map<Snowflake, Player> players
            = Collections.synchronizedMap(new HashMap<>());
    private final Persister persister;

    @PostConstruct
    public void init() {
        this.persister.load(KEY, TYPE)
                .doOnSuccess(map -> {
                    if (map != null) {
                        players.putAll(map);
                    }
                })
                .subscribe();
    }

    public void persist() {
        this.persister.persist(KEY, this.players)
                .onErrorResume(ex -> {
                    log.error("Error persisting parties", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    public Player getPlayerOrCreate(Snowflake id) {
        return players.computeIfAbsent(id, k -> new Player(id));
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public Optional<ZoneId> getUsualPlayerTimezone(Snowflake id) {
        return Optional.ofNullable(this.players.get(id))
                .map(Player::getUsualTimezone);
    }

    public void setUsualPlayerTimezone(Snowflake id, ZoneId timezone) {
        this.players.computeIfAbsent(id, Player::new)
                .setUsualTimezone(timezone);
    }
}
