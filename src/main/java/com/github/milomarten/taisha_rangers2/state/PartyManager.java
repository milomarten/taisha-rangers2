package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import discord4j.common.util.Snowflake;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class PartyManager {
    private static final String KEY = "party";
    private static final TypeReference<Map<String, Party>> TYPE =
            new TypeReference<>() {};

    private final Map<String, Party> parties
            = Collections.synchronizedMap(new HashMap<>());
    private final Persister persister;

    @PostConstruct
    public void init() {
        this.persister.load(KEY, TYPE)
                .doOnSuccess(map -> {
                    if (map != null) {
                        parties.putAll(map);
                    }
                })
                .subscribe();
    }

    private void persist() {
        this.persister.persist(KEY, this.parties)
                .onErrorResume(ex -> {
                    log.error("Error persisting parties", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    public boolean createParty(String name, Snowflake dm, Snowflake ping) {
        if (this.parties.containsKey(name)) {
            return false;
        }
        var party = new Party();
        party.setName(name);
        party.setDm(dm);
        party.setPing(ping);

        parties.put(name, party);
        persist();
        return true;
    }

    public Optional<Party> getParty(String name) {
        return Optional.ofNullable(this.parties.get(name));
    }

    public List<Party> getParties() {
        return new ArrayList<>(this.parties.values());
    }

    public <T> Optional<T> updatePartyAndReturn(String name, Function<Party, T> func) {
        var response = getParty(name)
                .map(func);
        if (response.isPresent()) {
            persist();
        }
        return response;
    }
}
