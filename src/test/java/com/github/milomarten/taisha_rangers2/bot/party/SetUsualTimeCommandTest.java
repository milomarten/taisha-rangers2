package com.github.milomarten.taisha_rangers2.bot.party;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SetUsualTimeCommandTest {
    private static final DateTimeFormatter DAY_OF_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");

    @Test
    void testAlgorithm() {
        var locale = Locale.FRANCE;

        var params = new SetUsualTimeCommand.Parameters();
        params.setDayOfWeek(DayOfWeek.TUESDAY);
        params.setTime(LocalTime.of(12+7, 0));
        params.setTimezone(ZoneId.of("America/Chicago"));

        var dayFormatted = DAY_OF_WEEK_FORMATTER
                .withLocale(locale)
                .format(params.getDayOfWeek());
        var timeFormatted = DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale)
                .format(params.getTime());
        var timezoneFormatted = params.getTimezone().getDisplayName(TextStyle.SHORT_STANDALONE, locale);

        System.out.println(dayFormatted);
        System.out.println(timeFormatted);
        System.out.println(timezoneFormatted);
    }
}