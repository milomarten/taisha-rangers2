package com.github.milomarten.taisha_rangers2.state;

import discord4j.common.util.Snowflake;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OutOfOffice {
    private final Snowflake player;
    private final LocalDate start;
    private final LocalDate end;
}
