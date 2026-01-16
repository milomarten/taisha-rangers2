package com.github.milomarten.taisha_rangers2.state;

import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;

@Data
@NoArgsConstructor
public class Player {
    private Snowflake id;
    private ZoneId usualTimezone;

    public Player(Snowflake id) {
        this.id = id;
    }
}
