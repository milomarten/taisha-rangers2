package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FindPlayerService {
    private final NextSessionManager nextSessionManager;
    private final PartyManager partyManager;

    public Optional<String> findPlayerCharacterName(Snowflake user, Snowflake channel) {
        // if this channel has a session, that's the one!
        var sessionMaybe = nextSessionManager.getNextSession(channel);
        if (sessionMaybe.isPresent()) {
            var session = sessionMaybe.get();
            var identity = session.getParty().getPlayerIdentities().get(user);
            if (identity != null) {
                return Optional.of(identity.getName());
            }
            if (Objects.equals(user, session.getGm())) {
                return Optional.of("Storyteller");
            }
        }

        var matching = partyManager.getParties().stream()
                .<Found>mapMulti((party, cons) -> {
                    if (party.getPlayerIdentities().containsKey(user)) {
                        cons.accept(new Found(party.getName(), party.getPlayerIdentities().get(user).getName()));
                    } else if (Objects.equals(party.getDm(), user)) {
                        cons.accept(new Found(party.getName(), "Storyteller"));
                    }
                }).toList();

        // Check all existing parties. If only one matches, that's them!
        if (matching.size() == 1) {
            return Optional.of(matching.getFirst().identity);
        }

        // If multiple match, find the one currently in session!
        Set<String> partyPool = matching.stream().map(Found::partyName).collect(Collectors.toSet());
        var now = Instant.now();
        var activeNextSessions = nextSessionManager.getNextSessions().stream()
                .filter(ns -> partyPool.contains(ns.getParty().getName()))
                .filter(ns -> ns.getStartTime() != null)
                .filter(ns -> now.isAfter(ns.getStartTime().toInstant()))
                .toList();

        if (activeNextSessions.size() == 1) {
            var party = activeNextSessions.getFirst().getParty();
            if (party.getPlayerIdentities().containsKey(user)) {
                return Optional.of(party.getPlayerIdentities().get(user).getName());
            } else {
                return Optional.of("Storyteller");
            }
        }

        // No luck!
        return Optional.empty();
    }

    private record Found(String partyName, String identity) {}
}
