package com.github.milomarten.taisha_rangers2.util;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * Various utilities to format things for Discord
 */
public class FormatUtils {
    /**
     * Ping a user by their ID
     * @param id The snowflake for that user
     * @return A string that represents a ping to that user
     */
    public static String pingUser(Snowflake id) {
        return "<@" + id.asString() + ">";
    }

    /**
     * Ping a role by their ID
     * @param id The snowflake for that role
     * @return A string that represents a ping to that role
     */
    public static String pingRole(Snowflake id) {
        return "<@&" + id.asString() + ">";
    }

    /**
     * Formats a time as a short date
     * Taking into account the user's timezones, the format displayed is:
     * [Month] [Day], [Year] at [Hour]:[Minute] [AM/PM]
     * @param temporal The time object to use. Must support get(INSTANT_SECONDS)
     * @return A string that represents a timestamp with the short date format
     */
    public static String formatShortDateTime(TemporalAccessor temporal) {
        return "<t:" + temporal.getLong(ChronoField.INSTANT_SECONDS) + ":f>";
    }

    /**
     * Formats a time as a short time
     * Taking into account the user's timezones, the format displayed is:
     * [Hour]:[Minute] [AM/PM]
     * @param temporal The time object to use. Must support get(INSTANT_SECONDS)
     * @return A string that represents a timestamp with the short date format
     */
    public static String formatShortTime(TemporalAccessor temporal) {
        return "<t:" + temporal.getLong(ChronoField.INSTANT_SECONDS) + ":t>";
    }
}
