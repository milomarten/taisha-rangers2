package com.github.milomarten.taisha_rangers2.util;

import com.github.milomarten.taisha_rangers2.state.PartyTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionDateUtilTest {
    private static final Clock CLOCK = Clock.fixed(
            ZonedDateTime.of(2026, 6, 29, 9, 0, 0, 0, ZoneId.of("America/Chicago")).toInstant(),
            ZoneId.of("America/Chicago")
    );

    private static final PartyTime USUAL = new PartyTime(
            DayOfWeek.WEDNESDAY, LocalTime.of(20, 0), ZoneId.of("America/Chicago")
    );

    @Test
    public void parseDatePossibleOptions_Empty() {
        var date = SessionDateUtil.parseDatePossibleOptions("", USUAL, CLOCK);
        assertEquals(LocalDate.of(2026, 7, 1), date);
    }

    @Test
    public void parseDatePossibleOptions_DayOfWeek() {
        var date = SessionDateUtil.parseDatePossibleOptions("Friday", USUAL, CLOCK);
        assertEquals(LocalDate.of(2026, 7, 3), date);
    }

    @Test
    public void parseDatePossibleOptions_ConcreteDate() {
        var date = SessionDateUtil.parseDatePossibleOptions("7/2", USUAL, CLOCK);
        assertEquals(LocalDate.of(2026, 7, 2), date);
    }
}