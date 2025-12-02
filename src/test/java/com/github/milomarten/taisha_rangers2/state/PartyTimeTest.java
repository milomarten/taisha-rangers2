package com.github.milomarten.taisha_rangers2.state;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class PartyTimeTest {
    private static final ZoneId TIMEZONE = ZoneId.of("America/Chicago");
    private static final LocalTime TIME = LocalTime.of(18, 0);

    @Test
    public void testNormalCase() {
        var time = new PartyTime(DayOfWeek.FRIDAY, TIME, TIMEZONE);
        var nextTime = time.getNextPossibleTime(makeClock(2025, 12, 1, 10, 0));

        assertEquals(LocalDate.of(2025, 12, 5), nextTime.toLocalDate());
        assertEquals(TIME, nextTime.toLocalTime());
        assertEquals(TIMEZONE, nextTime.getZone());
    }

    @Test
    public void testSameDayBeforeTime() {
        var time = new PartyTime(DayOfWeek.MONDAY, TIME, TIMEZONE);
        var nextTime = time.getNextPossibleTime(makeClock(2025, 12, 1, 10, 0));

        assertEquals(LocalDate.of(2025, 12, 1), nextTime.toLocalDate());
        assertEquals(TIME, nextTime.toLocalTime());
        assertEquals(TIMEZONE, nextTime.getZone());
    }

    @Test
    public void testSameDayAfterTime() {
        var time = new PartyTime(DayOfWeek.MONDAY, TIME, TIMEZONE);
        var nextTime = time.getNextPossibleTime(makeClock(2025, 12, 1, 20, 0));

        assertEquals(LocalDate.of(2025, 12, 8), nextTime.toLocalDate());
        assertEquals(TIME, nextTime.toLocalTime());
        assertEquals(TIMEZONE, nextTime.getZone());
    }

    private Clock makeClock(int year, int month, int day, int hour, int minute) {
        ZonedDateTime time = ZonedDateTime.of(
                year, month, day,
                hour, minute, 0, 0, ZoneId.of("America/Chicago")
        );
        return Clock.fixed(time.toInstant(), time.getZone());
    }
}