package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import discord4j.common.util.Snowflake;
import lombok.Data;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.*;

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
}
