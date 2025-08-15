package com.github.milomarten.taisha_rangers2.bot;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class YesCommandTest {
    private static final ZoneId CENTRAL = ZoneId.of("America/Chicago");
    private static final ZoneId EASTERN = ZoneId.of("America/New_York");
    private static final ZonedDateTime EIGHT_PM =
            ZonedDateTime.of(2025, 8, 15, 20, 0, 0, 0, CENTRAL);

    @Test
    public void testComputeTime_OneHourAfter_SameTZ() {
        var newTime = LocalTime.of(20, 30);

        var adjusted = YesCommand.computeContextualTime(EIGHT_PM, newTime, CENTRAL);
        assertEquals("2025-08-15T20:30-05:00[America/Chicago]", adjusted.toString());
    }

    @Test
    public void testComputeTime_OneHourBefore_SameTZ() {
        var newTime = LocalTime.of(19, 30);

        var adjusted = YesCommand.computeContextualTime(EIGHT_PM, newTime, CENTRAL);
        assertEquals("2025-08-15T19:30-05:00[America/Chicago]", adjusted.toString());
    }

    @Test
    public void testComputeTime_PastMidnight_SameTZ() {
        var newTime = LocalTime.of(0, 30);

        var adjusted = YesCommand.computeContextualTime(EIGHT_PM, newTime, CENTRAL);
        assertEquals("2025-08-16T00:30-05:00[America/Chicago]", adjusted.toString());
    }

    @Test
    public void testComputeTime_HalfHourBefore_NewTZ() {
        // "I can start at 8:30pm Eastern Time" -> 7:30pm Central
        var newTime = LocalTime.of(20, 30);

        var adjusted = YesCommand.computeContextualTime(EIGHT_PM, newTime, EASTERN);
        assertEquals("2025-08-15T19:30-05:00[America/Chicago]", adjusted.withZoneSameInstant(CENTRAL).toString());
    }

    @Test
    public void testComputeTime_HalfHourAfter_NewTZ() {
        // "I can start at 9:30pm Eastern Time" -> 8:30pm Central
        var newTime = LocalTime.of(21, 30);

        var adjusted = YesCommand.computeContextualTime(EIGHT_PM, newTime, EASTERN);
        assertEquals("2025-08-15T20:30-05:00[America/Chicago]", adjusted.withZoneSameInstant(CENTRAL).toString());
    }
}