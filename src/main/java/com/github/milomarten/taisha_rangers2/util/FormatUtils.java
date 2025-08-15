package com.github.milomarten.taisha_rangers2.util;

import discord4j.common.util.Snowflake;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;

public class FormatUtils {
    public static String pingRole(Snowflake id) {
        return "<@&" + id.asString() + ">";
    }

    public static String formatShortDateTime(TemporalAccessor temporal) {
        var seconds = Instant.from(temporal).getEpochSecond();
        return "<t:" + seconds + ":f>";
    }

    public static String formatShortTime(TemporalAccessor temporal) {
        var seconds = Instant.from(temporal).getEpochSecond();
        return "<t:" + seconds + ":t>";
    }
}
