package com.github.milomarten.taisha_rangers2.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {
    private Clock makeClock(int month, int day) {
        var zone = ZoneId.of("America/Chicago");
        var zdt = ZonedDateTime.of(2025, month, day, 0, 0, 0, 0, zone);
        return Clock.fixed(zdt.toInstant(), zone);
    }

    @Test
    public void testYMD() {
        var zdt = DateUtil.parseCasualDate("2025-08-13");
        assertEquals("2025-08-13T20:00-05:00[America/Chicago]", zdt.toString());
    }

    @Test
    public void testYM_ParseIsThisYear() {
        var zdt = DateUtil.parseCasualDate("08-13", makeClock(8, 1));
        assertEquals("2025-08-13T20:00-05:00[America/Chicago]", zdt.toString());
    }

    @Test
    public void testYM_ParseIsNextYear() {
        var zdt = DateUtil.parseCasualDate("08-13", makeClock(9, 1));
        assertEquals("2026-08-13T20:00-05:00[America/Chicago]", zdt.toString());
    }

    @Test
    public void testYM_ParseIsToday() {
        var zdt = DateUtil.parseCasualDate("08-13", makeClock(8, 13));
        assertEquals("2025-08-13T20:00-05:00[America/Chicago]", zdt.toString());
    }

    @Test
    public void testDayTimeNoSeconds() {
        var zdt = DateUtil.parseCasualDate("2025-08-13 20:00");
        assertEquals("2025-08-13T20:00-05:00[America/Chicago]", zdt.toString());
    }

    @Test
    public void testDayTimeWithSeconds() {
        var zdt = DateUtil.parseCasualDate("2025-08-13 20:00:00");
        assertEquals("2025-08-13T20:00-05:00[America/Chicago]", zdt.toString());
    }

    @Test
    public void testDayTimeTimezoneNoSeconds() {
        var zdt = DateUtil.parseCasualDate("2025-08-13 20:00,America/New_York");
        assertEquals("2025-08-13T20:00-04:00[America/New_York]", zdt.toString());
    }

    @Test
    public void testDayTimeTimezoneSeconds() {
        var zdt = DateUtil.parseCasualDate("2025-08-13 20:00:00,America/New_York");
        assertEquals("2025-08-13T20:00-04:00[America/New_York]", zdt.toString());
    }

    @Test
    public void testDayTimeCasualTimezoneNoSeconds() {
        var zdt = DateUtil.parseCasualDate("2025-08-13 20:00,MST");
        assertEquals("2025-08-13T20:00-06:00[America/Denver]", zdt.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3p", "3P", "3pm", "3PM", "3 PM", "15", "15:00", "3:00p", "3:00PM", "3:00 PM"})
    public void testManyWaysToSay3PM(String value) {
        var time = DateUtil.parseCasualTime(value);
        assertEquals("15:00", time.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3:30p", "3:30PM", "3:30 PM", "15:30"})
    public void testManyWaysToSay330PM(String value) {
        var time = DateUtil.parseCasualTime(value);
        assertEquals("15:30", time.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"chowder", "38", "42:00", "69pm", "12:77p"})
    public void testManyWrongWaysToSayTime(String value) {
        assertThrows(DateTimeException.class, () -> DateUtil.parseCasualTime(value));
    }

    @Test
    public void testNoonIsCorrect() {
        var time = DateUtil.parseCasualTime("12pm");
        assertEquals("12:00", time.toString());
    }

    @Test
    public void testMidnightIsCorrect() {
        var time = DateUtil.parseCasualTime("12am");
        assertEquals("00:00", time.toString());
    }

    @Test
    public void testNoonIsCorrect_24HourTime() {
        var time = DateUtil.parseCasualTime("12:00");
        assertEquals("12:00", time.toString());
    }

    @Test
    public void testMidnightIsCorrect_24HourTime() {
        var time = DateUtil.parseCasualTime("00:00");
        assertEquals("00:00", time.toString());
    }
}