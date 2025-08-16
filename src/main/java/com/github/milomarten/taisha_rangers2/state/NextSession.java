package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import discord4j.common.util.Snowflake;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class NextSession {
    private final Snowflake channel;
    private final Snowflake ping;
    private final Snowflake gm;
    private final int numberOfPlayers;
    private final Map<Snowflake, PlayerResponse> playerResponses = new HashMap<>();
    private final ZonedDateTime proposedStartTime;
    private ZonedDateTime startTime;

    public boolean allPlayersResponded() {
        return playerResponses.values()
                .stream()
                .filter(pr -> pr.getState() != PlayerResponse.State.MAYBE)
                .count() == numberOfPlayers;
    }

    public boolean allPlayersRespondedYes() {
        return playerResponses.values()
                .stream()
                .filter(pr -> pr.getState() == PlayerResponse.State.YES)
                .count() == numberOfPlayers;
    }
}
