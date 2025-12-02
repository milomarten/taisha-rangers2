package com.github.milomarten.taisha_rangers2.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.temporal.ChronoField;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyTime {
    private DayOfWeek dayOfWeek;
    private LocalTime timeOfDay;
    private ZoneId timezone;

    public ZonedDateTime getNextPossibleTime() {
        return getNextPossibleTime(Clock.system(timezone));
    }

    public ZonedDateTime getNextPossibleTime(Clock clock) {
        var now = ZonedDateTime.now(clock);
        if (now.getDayOfWeek() == this.dayOfWeek && now.toLocalTime().isBefore(this.timeOfDay)) {
            return now.with(this.timeOfDay);
        }

        // We need to scroll forward until we hit the requested date. with() doesn't work because
        // it can go backwards sometimes.
        int dayOffset = this.dayOfWeek.getValue() - now.plusDays(1).getDayOfWeek().getValue();
        if (dayOffset < 0) {
            dayOffset += 7;
        }
        return now
                .plusDays(dayOffset + 1)
                .with(timeOfDay);
    }
}
