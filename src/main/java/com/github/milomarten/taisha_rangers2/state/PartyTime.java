package com.github.milomarten.taisha_rangers2.state;

import com.github.milomarten.taisha_rangers2.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.temporal.ChronoField;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyTime {
    public static Clock clock;

    private DayOfWeek dayOfWeek;
    private LocalTime timeOfDay;
    private ZoneId timezone;

    private ZonedDateTime now() {
        return clock == null ? ZonedDateTime.now(timezone) : ZonedDateTime.now(clock);
    }

    public ZonedDateTime getNextPossibleTime() {
        return DateUtil.getNextPossibleTime(now(), dayOfWeek, timeOfDay);
    }

    public ZonedDateTime getNextPossibleTime(LocalTime atTime) {
        return DateUtil.getNextPossibleTime(now(), dayOfWeek, atTime);
    }

    public ZonedDateTime getNextPossibleTime(DayOfWeek atDow) {
        return DateUtil.getNextPossibleTime(now(), atDow, timeOfDay);
    }

    public ZonedDateTime getNextPossibleTime(Clock clock) {
        return DateUtil.getNextPossibleTime(ZonedDateTime.now(clock), dayOfWeek, timeOfDay);
    }
}
