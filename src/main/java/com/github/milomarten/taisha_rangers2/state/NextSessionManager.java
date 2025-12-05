package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.taisha_rangers2.bot.SessionIdentityParameters;
import com.github.milomarten.taisha_rangers2.exception.*;
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
import java.util.function.BiFunction;
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

    public NextSession createSession(Snowflake channel, Party party, ZonedDateTime proposedStart) {
        if (this.nextSessions.containsKey(channel)) {
            throw new SessionAlreadyExistsException(channel);
        }

        var session = new NextSession(channel, party, proposedStart);
        this.nextSessions.put(channel, session);
        this.listeners.forEach(c -> c.onCreate(session));

        persist();

        return session;
    }

    public boolean cancelSession(SessionIdentityParameters params) {
        var session = this.nextSessions.get(params.getChannelId());
        if (session == null) {
            return false;
        } else if (Objects.equals(params.getUserId(), session.getGm())) {
             this.nextSessions.remove(params.getChannelId());
             this.listeners.forEach(c -> c.onDelete(params.getChannelId()));
             persist();
             return true;
        } else {
            throw new NotDM();
        }
    }

    public boolean cancelSessionUnprotected(Snowflake channelId) {
        var session = this.nextSessions.get(channelId);
        if (session == null) {
            return false;
        } else {
            this.nextSessions.remove(channelId);
            this.listeners.forEach(c -> c.onDelete(channelId));
            persist();
            return true;
        }
    }

    public boolean playerDo(Snowflake channel, Snowflake player, BiConsumer<NextSession, PlayerResponse> action) {
        return playerDoAndReturn(channel, player, (ns, pr) -> {
            action.accept(ns, pr);
            return true;
        }).orElse(false);
    }

    public <T> Optional<T> playerDoAndReturn(Snowflake channel, Snowflake player, BiFunction<NextSession, PlayerResponse, T> action) {
        return updateAndReturn(channel, s -> {
            if (!s.getParty().getPlayers().contains(player)) {
                throw new NotInParty();
            }

            var response = s.getPlayerResponses().get(player);
            T returnValue;
            if (response != null) {
                returnValue = action.apply(s, response);
            } else {
                var newPlayer = new PlayerResponse(player);
                returnValue = action.apply(s, newPlayer);
                s.getPlayerResponses().put(player, newPlayer);
            }
            return returnValue;
        });
    }

    public boolean update(Snowflake channel, Consumer<NextSession> update) {
        return updateAndReturn(channel, ns -> {
            update.accept(ns);
            return true;
        }).orElse(false);
    }

    public <T> Optional<T> updateAndReturn(Snowflake channel, Function<NextSession, T> update) {
        var session = nextSessions.get(channel);
        if (session == null) {
            return Optional.empty();
        }
        var ret = update.apply(session);
        listeners.forEach(c -> c.onUpdate(session));

        persist();
        return Optional.of(ret);
    }
}
