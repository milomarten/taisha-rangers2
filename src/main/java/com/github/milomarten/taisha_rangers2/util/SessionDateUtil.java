package com.github.milomarten.taisha_rangers2.util;

import com.github.milomarten.taisha_rangers2.state.PartyTime;

import java.time.*;

public class SessionDateUtil {

    /**
     * Parse a string into a date using a variety of possible formats.
     * Supported formats:
     * - Blank: Uses the next possible day, after today, which matches the party's usual day-of-week meetup.
     * - Day of Week: Uses the next possible day, after today, which matches this Day of Week.
     * - Date: Uses DateUtil.parseCasualDate to create a concrete date, which is used.
     * @param value The value to parse
     * @param timezone The timezone session takes place in (for determining "now" in some cases)
     * @param partyTime The usual party times.
     * @return A LocalDate that matches the input.
     * @throws DateTimeException None of the possible formats match, or an impossible date is provided.
     */
    public static LocalDate parseDatePossibleOptions(String value, ZoneId timezone, PartyTime partyTime) {
        return parseDatePossibleOptions(value, partyTime, Clock.system(timezone));
    }

    // For testing purposes. Allows a mock clock to be used
    public static LocalDate parseDatePossibleOptions(String value, PartyTime partyTime, Clock clock) {
        if (value.isEmpty()) {
            return DateUtil.getNextPossibleDate(LocalDate.now(clock), partyTime.getDayOfWeek());
        } else {
            var dow = DateUtil.parseCasualDayOfWeek(value);
            if (dow != null) {
                return DateUtil.getNextPossibleDate(LocalDate.now(clock), dow);
            } else {
                return DateUtil.parseCasualDate(value);
            }
        }
    }

    /**
     * Parse a string into a time using a variety of formats.
     * Supported formats:
     * - Blank: Uses the party's usual time of meetup.
     * - Time: Uses DateUtil.parseCasualTime to create a concrete time, which is used.
     * @param value The value to parse
     * @param partyTime The usual party times.
     * @return A LocalTime that matches the input
     * @throws DateTimeException None of the possible formats match, or an impossible time is provided.
     */
    public static LocalTime parseTimePossibleOptions(String value, PartyTime partyTime) {
        if (value.isEmpty()) {
            return partyTime.getTimeOfDay();
        } else {
            return DateUtil.parseCasualTime(value);
        }
    }

    /**
     * Parse a string into a timezone using a variety of formats.
     * Supported formats:
     * - Blank: Uses the party's usual timezone.
     * - Time: Uses DateUtil.parseCasualTimezone to create a concrete timezone, which is used.
     * @param value The value to parse
     * @param partyTime The usual party times.
     * @return A ZoneId that matches the input
     * @throws DateTimeException None of the possible formats match, or an unknown timezone is provided.
     */
    public static ZoneId parseZoneIdPossibleOptions(String value, PartyTime partyTime) {
        if (value.isEmpty()) {
            return partyTime.getTimezone();
        } else {
            return DateUtil.parseCasualTimezone(value);
        }
    }
}
