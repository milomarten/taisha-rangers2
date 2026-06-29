package com.github.milomarten.taisha_rangers2.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartyTime {
    private DayOfWeek dayOfWeek;
    private LocalTime timeOfDay;
    private ZoneId timezone;
}
