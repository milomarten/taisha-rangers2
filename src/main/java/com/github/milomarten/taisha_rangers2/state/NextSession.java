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
    private final Snowflake ping;
    private final Snowflake gm;
    private final int numberOfPlayers;
    private final Map<Snowflake, PlayerResponse> playerResponses = new HashMap<>();
    private final ZonedDateTime proposedStartTime;
    private ZonedDateTime startTime;

    @JsonIgnore
    public int getNumberOfPlayersResponded() {
        return (int) playerResponses.values()
                .stream()
                .filter(pr -> pr.getState() != PlayerResponse.State.MAYBE && pr.getState() != PlayerResponse.State.NO_RESPONSE)
                .count();
    }

    public boolean allPlayersResponded() {
        return getNumberOfPlayersResponded() == numberOfPlayers;
    }

    public boolean allPlayersRespondedYes() {
        return playerResponses.values()
                .stream()
                .filter(pr -> pr.getState() == PlayerResponse.State.YES)
                .count() == numberOfPlayers;
    }

    @JsonIgnore
    public boolean isFarOffSession() {
        var now = ZonedDateTime.now();
        var announcementTime = getAnnouncementTime();

        return now.isBefore(announcementTime);
    }

    @JsonIgnore
    public ZonedDateTime getAnnouncementTime() {
        return proposedStartTime.minusHours(HOURS_BEFORE_SESSION_TO_ANNOUNCE);
    }
}
