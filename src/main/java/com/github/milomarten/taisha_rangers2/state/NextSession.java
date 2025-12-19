package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import discord4j.common.util.Snowflake;
import lombok.Data;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

@Data
public class NextSession {
    private static final int HOURS_BEFORE_SESSION_TO_ANNOUNCE = 24 * 7;

    private final Snowflake channel;
    private final Party party;
    private final Map<Snowflake, PlayerResponse> playerResponses = new HashMap<>();
    private final ZonedDateTime proposedStartTime;
    private ZonedDateTime startTime;

    // Adaptions from the old way
    public Snowflake getPing() { return party.getPing(); }
    public Snowflake getGm() { return party.getDm(); }
    public int getNumberOfPlayers() { return party.getPlayers().size(); }
    public Locale getLocale() { return Objects.requireNonNullElse(party.getLocale(), Locale.US); }

    @JsonIgnore
    public int getNumberOfPlayersResponded() {
        return (int) playerResponses.values()
                .stream()
                .filter(pr -> pr.getState() != PlayerResponse.State.MAYBE && pr.getState() != PlayerResponse.State.NO_RESPONSE)
                .count();
    }

    public boolean allPlayersResponded() {
        return getNumberOfPlayersResponded() == getNumberOfPlayers();
    }

    public boolean allPlayersRespondedYes() {
        return playerResponses.values()
                .stream()
                .filter(pr -> pr.getState() == PlayerResponse.State.YES)
                .count() == getNumberOfPlayers();
    }

    /***
     * Get a list of all player responses, even those who haven't responded
     * This combines the party members with their corresponding status, using the special
     * NO_RESPONSE status for party members who haven't responded yes.
     * @return A stream of PlayerResponses, including those who haven't responded yet.
     */
    @JsonIgnore
    public Stream<PlayerResponse> getHydratedPlayerResponses() {
        return getParty().getPlayers()
                .stream()
                .map(id -> {
                    var playerResponse = getPlayerResponses().get(id);
                    return Objects.requireNonNullElseGet(playerResponse, () -> new PlayerResponse(id));
                });
    }
}
