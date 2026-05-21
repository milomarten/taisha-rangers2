package com.github.milomarten.taisha_rangers2.util;

import com.github.milomarten.taisha_rangers2.state.PartyTime;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiFunction;

public class SessionDateUtil {
    private static final List<BiFunction<String, PartyTime, ZonedDateTime>> FORMATS = Arrays.asList(
            DateUtil::parseCasualDateTime,
            (str, time) -> time.getNextPossibleTime(DateUtil.parseCasualTime(str)),
            (str, time) -> time.getNextPossibleTime(DateUtil.parseCasualDayOfWeek(str))
    );

    public static ZonedDateTime parseDatePossibleOptions(String value, PartyTime partyTime) {
        var errors = new StringJoiner(", ");
        for (var format : FORMATS) {
            try {
                return format.apply(value, partyTime);
            } catch (RuntimeException ex) {
                errors.add(ex.getMessage());
            }
        }
        throw new DateTimeException(errors.toString());
    }
}
