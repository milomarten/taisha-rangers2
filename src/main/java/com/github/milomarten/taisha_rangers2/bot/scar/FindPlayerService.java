package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import com.github.milomarten.taisha_rangers2.state.PlayerIdentity;
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
    private static final PlayerIdentity STORYTELLER = new PlayerIdentity("Storyteller");

    private final NextSessionManager nextSessionManager;
    private final PartyManager partyManager;

    public Optional<PlayerContext> findPlayerCharacterName(Snowflake user, Snowflake channel) {
        // if this channel has a session, that's the one!
        var sessionMaybe = nextSessionManager.getNextSession(channel);
        if (sessionMaybe.isPresent()) {
            var session = sessionMaybe.get();
            var identity = session.getParty().getPlayerIdentities().get(user);
            if (identity != null) {
                return Optional.of(new PlayerContext(user, identity, session.getParty()));
            }
            if (Objects.equals(user, session.getGm())) {
                return Optional.of(new PlayerContext(user, STORYTELLER, session.getParty()));
            }
        }

        var matching = partyManager.getParties().stream()
                .<PlayerContext>mapMulti((party, cons) -> {
                    if (party.getRelevantChannels() == null || party.getRelevantChannels().contains(channel)) {
                        if (party.getPlayerIdentities().containsKey(user)) {
                            cons.accept(new PlayerContext(user, party.getPlayerIdentities().get(user), party));
                        } else if (Objects.equals(party.getDm(), user)) {
                            cons.accept(new PlayerContext(user, STORYTELLER, party));
                        }
                    }
                }).toList();

        // Check all existing parties. If only one matches, that's them!
        if (matching.size() == 1) {
            return Optional.of(matching.getFirst());
        } else if (matching.size() > 1) {
            // If only one of these parties has specific channels, those have precedence over generic.
            var specificOne = matching.stream()
                    .filter(pc -> pc.party.getRelevantChannels() != null)
                    .toList();
            if (specificOne.size() == 1) {
                return Optional.of(specificOne.getFirst());
            }
        }

        // If multiple match, find the one currently in session!
        Set<String> partyPool = matching.stream()
                .map(PlayerContext::party)
                .map(Party::getName)
                .collect(Collectors.toSet());
        var now = Instant.now();
        var activeNextSessions = nextSessionManager.getNextSessions().stream()
                .filter(ns -> partyPool.contains(ns.getParty().getName()))
                .filter(ns -> ns.getStartTime() != null)
                .filter(ns -> now.isAfter(ns.getStartTime().toInstant()))
                .toList();

        if (activeNextSessions.size() == 1) {
            var party = activeNextSessions.getFirst().getParty();
            if (party.getPlayerIdentities().containsKey(user)) {
                return Optional.of(new PlayerContext(user, party.getPlayerIdentities().get(user), party));
            } else {
                return Optional.of(new PlayerContext(user, STORYTELLER, party));
            }
        }

        // No luck!
        return Optional.empty();
    }

    public record PlayerContext(Snowflake user, PlayerIdentity identity, Party party) {}
}
