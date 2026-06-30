package com.github.milomarten.taisha_rangers2.state;

import com.fasterxml.jackson.annotation.JsonSetter;
import discord4j.common.util.Snowflake;
import lombok.Data;

import java.util.*;

@Data
public class Party {
    private String name;
    private Snowflake dm;
    private Map<Snowflake, PlayerIdentity> playerIdentities = new HashMap<>();
    private Snowflake ping;
    private PartyTime usualTime;
    private Locale locale;
    private Set<Snowflake> relevantChannels;

    @JsonSetter
    public void setPlayers(Set<Snowflake> players) {
        players.forEach(player -> {
            playerIdentities.put(player, new PlayerIdentity());
        });
    }
}
