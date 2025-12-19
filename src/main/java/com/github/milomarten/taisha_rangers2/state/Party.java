package com.github.milomarten.taisha_rangers2.state;

import discord4j.common.util.Snowflake;
import lombok.Data;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Data
public class Party {
    private String name;
    private Snowflake dm;
    private Set<Snowflake> players = new HashSet<>();
    private Snowflake ping;
    private PartyTime usualTime;
    private Locale locale;
}
