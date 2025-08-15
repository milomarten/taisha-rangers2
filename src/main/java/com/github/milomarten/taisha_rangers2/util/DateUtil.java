package com.github.milomarten.taisha_rangers2.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DateUtil {
    private static final ZoneId CENTRAL_TIME = ZoneId.of("America/Chicago");
    private static final LocalTime DEFAULT_TIME = LocalTime.of(20, 0);

    private static final Map<String, ZoneId> OVERRIDES = new HashMap<>();
    static {
        OVERRIDES.put("ET", ZoneId.of("America/New_York"));
        OVERRIDES.put("CT", ZoneId.of("America/Chicago"));
        OVERRIDES.put("MT", ZoneId.of("America/Denver"));
        OVERRIDES.put("PT", ZoneId.of("America/Los_Angeles"));
        OVERRIDES.put("EST", ZoneId.of("America/New_York"));
        OVERRIDES.put("CST", ZoneId.of("America/Chicago"));
        OVERRIDES.put("MST", ZoneId.of("America/Denver"));
        OVERRIDES.put("PST", ZoneId.of("America/Los_Angeles"));
        OVERRIDES.put("EDT", ZoneId.of("America/New_York"));
        OVERRIDES.put("CDT", ZoneId.of("America/Chicago"));
        OVERRIDES.put("MDT", ZoneId.of("America/Denver"));
        OVERRIDES.put("PDT", ZoneId.of("America/Los_Angeles"));
    }

    public static ZonedDateTime parseCasualDate(String value) {
        return parseCasualDate(value, Clock.system(CENTRAL_TIME));
    }

    public static ZonedDateTime parseCasualDate(String value, Clock clock) {
        if (value.length() == 10) {
            // 2025-08-13. Assume default time and Central
            return LocalDate.parse(value)
                    .atTime(DEFAULT_TIME)
                    .atZone(clock.getZone());
        } else if (value.length() == 5) {
            // 08-13. Assume relevant year, default time, and Central
            var md = MonthDay.parse("--" + value);
            var today = LocalDate.now(clock);
            int yearOffset = 0;
            if (MonthDay.from(today).isAfter(md)) {
                yearOffset = 1;
            }
            return md.atYear(Year.from(today).getValue() + yearOffset)
                    .atTime(DEFAULT_TIME)
                    .atZone(clock.getZone());
        } else if (value.length() == 19 || value.length() == 16) {
            //2025-08-13 08:00:00. Seconds is optional. Assume Central
            if (value.length() == 16) {
                value += ":00";
            }
            value = value.replace(' ', 'T');
            return LocalDateTime.parse(value)
                    .atZone(clock.getZone());
        } else if (value.contains(",")) {
            // More simple way of specifying a time and a timezone together.
            // 2025-08-13 08:00:00,America/Chicago. Seconds is optional
            String[] tokens = value.split(",", 2);
            if (tokens[0].length() == 16) {
                tokens[0] = tokens[0] + ":00";
            }
            var timezone = parseCasualTimezone(tokens[1]);
            return LocalDateTime.parse(tokens[0].replace(' ', 'T'))
                    .atZone(timezone);
        } else if(value.contains("[")) {
            // Allows for a complete string if you so desire.
            //2025-08-13T08:00-05:00[America/Chicago]
            return ZonedDateTime.parse(value);
        } else {
            // Allows for a complete string if you so desire.
            // This one doesn't require the actual tzid.
            //2025-08-13T08:00-05:00
            return OffsetDateTime.parse(value).toZonedDateTime();
        }
    }

    public static ZoneId parseCasualTimezone(String value) {
        return Objects.requireNonNullElseGet(OVERRIDES.get(value), () -> ZoneId.of(value));
    }

    public static LocalTime parseCasualTime(String value) {
        if (value.isEmpty()) { return null; }
        value = StringUtils.deleteWhitespace(value.toUpperCase());
        TimeType timeType;
        if (value.endsWith("PM") || value.endsWith("AM")) {
            timeType = value.endsWith("PM") ? TimeType.PM : TimeType.AM;
            value = value.substring(0, value.length() - 2);
        } else if (value.endsWith("P") || value.endsWith("A")) {
            timeType = value.endsWith("P") ? TimeType.PM : TimeType.AM;
            value = value.substring(0, value.length() - 1);
        } else {
            timeType = TimeType.TWENTY_FOUR_HOUR_TIME;
        }

        if (!value.contains(":")) {
            // 3PM or 3p -> 15:00:00
            // 15 -> 15:00:00
            if (NumberUtils.isCreatable(value)) {
                var hour = timeType.normalizeHour(Integer.parseInt(value));
                return LocalTime.of(hour, 0);
            } else {
                throw new DateTimeException("Invalid hour, must be 0 to 23");
            }
        } else {
            // 3:30PM or 3:30p -> 15:30:00
            // 15:30 -> 15:3T0:00
            var tokens = value.split(":");
            if (NumberUtils.isCreatable(tokens[0]) && NumberUtils.isCreatable(tokens[1])) {
                var hour = timeType.normalizeHour(Integer.parseInt(tokens[0]));
                var minute = Integer.parseInt(tokens[1]);
                return LocalTime.of(hour, minute);
            } else {
                throw new DateTimeException("Invalid hour/minute, must be a properly formatted time");
            }
        }
    }

    private enum TimeType {
        AM {
            @Override
            public int normalizeHour(int number) {
                return number == 12 ? 0 : number;
            }
        },
        PM {
            @Override
            public int normalizeHour(int number) {
                return number == 12 ? 12 : 12 + number;
            }
        },
        TWENTY_FOUR_HOUR_TIME {
            @Override
            public int normalizeHour(int number) {
                return number;
            }
        };

        public abstract int normalizeHour(int number);
    }
}
