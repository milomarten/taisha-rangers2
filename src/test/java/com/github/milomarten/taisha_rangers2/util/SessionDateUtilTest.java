package com.github.milomarten.taisha_rangers2.util;

import com.github.milomarten.taisha_rangers2.state.PartyTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionDateUtilTest {
    private static final Clock CLOCK = Clock.fixed(
            ZonedDateTime.of(2026, 5, 20, 20, 0, 0, 0, ZoneId.of("America/Chicago")).toInstant(),
            ZoneId.of("America/Chicago")
    );

    private static PartyTime makePartyTime() {
        PartyTime.clock = CLOCK;
        return new PartyTime(
                DayOfWeek.SUNDAY,
                LocalTime.of(20, 0),
                ZoneId.of("America/Chicago")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"tuesday", "tue"})
    public void testProvidingJustDayOfWeek(String input) {
        var t = SessionDateUtil.parseDatePossibleOptions(input, makePartyTime());

        assertEquals(2026, t.getYear());
        assertEquals(5, t.getMonthValue());
        assertEquals(26, t.getDayOfMonth());
        assertEquals(20, t.getHour());
        assertEquals(0, t.getMinute());
        assertEquals(0, t.getSecond());
    }

    @ParameterizedTest
    @ValueSource(strings = {"6p", "6pm"})
    public void testProvidingJustATime(String input) {
        var t = SessionDateUtil.parseDatePossibleOptions(input, makePartyTime());

        assertEquals(2026, t.getYear());
        assertEquals(5, t.getMonthValue());
        assertEquals(24, t.getDayOfMonth());
        assertEquals(18, t.getHour());
        assertEquals(0, t.getMinute());
        assertEquals(0, t.getSecond());
    }
}