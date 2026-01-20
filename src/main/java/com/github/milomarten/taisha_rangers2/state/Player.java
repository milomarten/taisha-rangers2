package com.github.milomarten.taisha_rangers2.state;

import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.util.List;

@Data
@NoArgsConstructor
public class Player {
    private Snowflake id;
    private ZoneId usualTimezone;
    private List<OutOfOffice> outOfOffices;

    public Player(Snowflake id) {
        this.id = id;
    }
}
