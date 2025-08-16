package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.taisha_rangers2.exception.NoSessionException;
import com.github.milomarten.taisha_rangers2.exception.TooManyPlayers;
import com.github.milomarten.taisha_rangers2.persistence.JsonFilePersister;
import com.github.milomarten.taisha_rangers2.persistence.NoOpPersister;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import discord4j.common.util.Snowflake;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@Slf4j
public class NextSessionManager {
    private static final String KEY = "session";
    private static final TypeReference<Map<Snowflake, NextSession>> MAP_TYPE
            = new TypeReference<Map<Snowflake, NextSession>>() {};

    private final Map<Snowflake, NextSession> nextSessions = Collections.synchronizedMap(new HashMap<>());
    private final Persister persister;
    private final List<NextSessionListener> listeners;

    @Autowired
    public NextSessionManager(
            @Value("${persistence.session-manager.base-path:}") String path,
            ObjectMapper om,
            List<NextSessionListener> listeners) {
        this.persister = StringUtils.isEmpty(path) ?
                new NoOpPersister() :
                new JsonFilePersister(path, om);
        this.listeners = listeners;
    }

    @PostConstruct
    public void init() {
        this.listeners.forEach(nsl -> nsl.setNextSessionManager(this));
        this.persister.load(KEY, MAP_TYPE)
                .doOnSuccess(n -> {
                    if (n != null) {
                        log.info("Loaded stored next sessions");
                        this.nextSessions.putAll(n);
                    }
                    this.nextSessions.values()
                            .forEach(ns ->
                                    this.listeners.forEach(nsl -> nsl.onLoad(ns)));
                })
                .block();
    }

    private void persist() {
        this.persister.persist(KEY, this.nextSessions)
                .onErrorResume(ex -> {
                    log.error("Error persisting session", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    public Optional<NextSession> getNextSession(Snowflake id) {
        return Optional.ofNullable(this.nextSessions.get(id));
    }

    public void createSession(Snowflake channel, Snowflake ping, Snowflake gm, int numPlayers, ZonedDateTime proposedStart) {
        var session = new NextSession(channel, ping, gm, numPlayers, proposedStart);
        if (this.nextSessions.containsKey(channel)) {
            this.listeners.forEach(c -> c.onDelete(channel));
        }
        this.nextSessions.put(channel, session);
        this.listeners.forEach(c -> c.onCreate(session));

        persist();
    }

    public boolean setSessionDate(Snowflake channel, ZonedDateTime date) {
        return update(channel, s -> s.setStartTime(date));
    }

    public boolean setSessionDate(Snowflake channel, Function<NextSession, ZonedDateTime> function) {
        return update(channel, ns -> ns.setStartTime(function.apply(ns)));
    }

    public boolean cancelSession(Snowflake channel) {
        var worked = this.nextSessions.remove(channel) != null;
        if (worked) {
            this.listeners.forEach(c -> c.onDelete(channel));

            persist();
            return true;
        } else {
            return false;
        }
    }

    public boolean playerDo(Snowflake channel, Snowflake player, BiConsumer<NextSession, PlayerResponse> action) {
        return update(channel, s -> {
            var response = s.getPlayerResponses().get(player);
            if (response != null) {
                action.accept(s, response);
            } else if (s.getPlayerResponses().size() < s.getNumberOfPlayers()) {
                var newPlayer = new PlayerResponse(player);
                action.accept(s, newPlayer);
                s.getPlayerResponses().put(player, newPlayer);
            } else {
                throw new TooManyPlayers(s.getNumberOfPlayers());
            }
        });
    }

    private boolean update(Snowflake channel, Consumer<NextSession> update) {
        var session = nextSessions.get(channel);
        if (session == null) {
            return false;
        }
        update.accept(session);
        listeners.forEach(c -> c.onUpdate(session));

        persist();
        return true;
    }
}
