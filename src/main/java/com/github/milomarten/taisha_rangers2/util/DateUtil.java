package com.github.milomarten.taisha_rangers2.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Various utilities for handling dates via commands
 */
public class DateUtil {
    private static final ZoneId CENTRAL_TIME = ZoneId.of("America/Chicago");
    private static final LocalTime DEFAULT_TIME = LocalTime.of(18, 0);

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

    /**
     * Parse a date time from a number of permissible formats:
     * - YYYY-MM-DD -> YYYY-MM-DDT18:00:00, using Chicago Time at that moment.
     * - MM-DD -> [computed year]-MM-DDT18:00:00, using Chicago Time at that moment. If
     * the input date is after the current date, the current year is assumed; otherwise, it is next year. Examples:
     *  - Input: 08-15, Today: 2025-08-01 -> Year is 2025 (since Aug 15th is after August 1st)
     *  - Input: 01-02, Today: 2025-12-25 -> Year is 2026 (since Jan 2nd is before December 25th, when not taking years into account)
     * - YYYY-MM-DD HH:MM -> YYYY-MM-DDTHH:MM:00, using Chicago Time at that moment.
     * - YYYY-MM-DD HH:MM:SS -> YYYY-MM-DDTHH:MM:SS, using Chicago Time at that moment.
     * - YYYY-MM-DD HH:MM,CST -> YYYY-MM-DDTHH:MM:00, using the timezone provided after the comma. This can be anything parsed by parseCasualTimezone()
     * - 2025-08-13T08:00-05:00 -> Fully formed date and time, no changes
     * - 2025-08-13T08:00-05:00[America/Chicago] -> Fully formed date and time, no changes
     * @param value The string to parse
     * @return The parsed ZonedDateTime
     */
    public static ZonedDateTime parseCasualDateTime(String value) {
        return parseCasualDateTime(value, Clock.system(CENTRAL_TIME));
    }

    /**
     * See parseCasualDateTime
     * Provided Clock argument for test case support
     * @param value The value to parse
     * @param clock The clock to use, to allow test cases to hard-code "now"
     * @return The parsed ZonedDateTime
     */
    public static ZonedDateTime parseCasualDateTime(String value, Clock clock) {
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

    /**
     * Parse a timezone in a few different formats:
     * - Any tzid is supported, i.e. America/Chicago
     * - timezones of EST, EDT, or ET all map to America/New_York
     * - timezones of CST, CDT, or CT all map to America/Chicago
     * - timezones of MST, MDT, or MT all map to America/Denver
     * - timezones of PST, PDT, or PT all map to America/Los_Angeles
     * There is no enforcement of if you are using Standard or Daylight time correctly. Thus,
     * 8PM EST, 8PM EDT, and 8PM ET all refer to the same Instant, assuming they are all on the same date. This also
     * means that you should NOT use "MST" if your times should be relative to Arizona. Instead, use America/Phoenix.
     * @param value The value to parse
     * @return The ZoneId parsed
     */
    public static ZoneId parseCasualTimezone(String value) {
        return Objects.requireNonNullElseGet(OVERRIDES.get(value), () -> ZoneId.of(value));
    }

    /**
     * Parse a time object in a few different formats:
     * - 8 PM
     * - 8p
     * - 8pm
     * - 8:00pm
     * - 8:00 PM
     * - 8:00p
     * - 20:00
     * - 20
     * All refer to 8pm. There is no parsing of seconds, since it is unnecessary for what I am using this for.
     * @param value The value to parse
     * @return The parsed time
     */
    public static LocalTime parseCasualTime(String value) {
        if (value.isEmpty()) { return null; }
        value = StringUtils.deleteWhitespace(value.toUpperCase()).replace(":", "");
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

        // 3PM or 3p -> 15:00:00
        // 15 -> 15:00:00
        if (NumberUtils.isCreatable(value)) {
            int hour, minute;
            if (value.length() <= 2) {
                hour = timeType.normalizeHour(Integer.parseInt(value));
                minute = 0;
            } else if (value.length() == 3) {
                hour = timeType.normalizeHour(Integer.parseInt(value.substring(0, 1)));
                minute = Integer.parseInt(value.substring(1, 3));
            } else if (value.length() == 4) {
                hour = timeType.normalizeHour(Integer.parseInt(value.substring(0, 2)));
                minute = Integer.parseInt(value.substring(2, 4));
            } else {
                throw new DateTimeException("Invalid time, can't be more than four numbers");
            }
            return LocalTime.of(hour, minute);
        } else {
            throw new DateTimeException("Invalid time format, can only contain numbers, whitespace, or colons");
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

    public static LocalDate parseCasualDate(String value) {
        return parseCasualDate(value, Clock.systemDefaultZone());
    }

    public static LocalDate parseCasualDate(String value, Clock clock) {
        if (value.isEmpty()) {
            return null;
        }

        List<String> elements = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (!sb.isEmpty()) {
                elements.add(sb.toString());
                sb.setLength(0);
            }
        }
        if (!sb.isEmpty()) {
            elements.add(sb.toString());
        }

        int year, month, day;
        if (elements.size() == 2) {
            LocalDate now =  LocalDate.now(clock);

            month = Integer.parseInt(elements.get(0));
            day = Integer.parseInt(elements.get(1));
            year = now.getYear();
            var requestedMD = MonthDay.of(month, day);
            var nowMD = MonthDay.from(now);
            if (requestedMD.isBefore(nowMD)) {
                year += 1;
            }
        } else if (elements.size() == 3) {
            year = Integer.parseInt(elements.get(0));
            month = Integer.parseInt(elements.get(1));
            day = Integer.parseInt(elements.get(2));

            if (year < 2000) {
                year += 2000;
            }
        } else {
            throw new DateTimeException("Supports MM/DD or YYYY/MM/DD");
        }

        return LocalDate.of(year, month, day);
    }

    private static final Set<String> DAYS = Set.of("DAY", "DAYS", "D");
    private static final Set<String> HOURS = Set.of("H", "HR", "HRS", "HOURS", "HOUR");
    private static final Set<String> MINUTES = Set.of("M", "MIN", "MINS", "MINUTES");

    public static Duration parseCasualDuration(String value) {
        var val = StringUtils.deleteWhitespace(value);
        if (val.isEmpty()) {
            throw new IllegalArgumentException("Can't be empty");
        }
        val += '\0'; // sentinel
        int days = 0;
        int hours = 0;
        int minutes = 0;

        var runningValue = new StringBuilder();
        int runningNumber = -1;
        boolean expectingNumber = true;
        for (char c : val.toCharArray()) {
            if (expectingNumber && !Character.isDigit(c)) {
                runningNumber = Integer.parseInt(runningValue.toString());
                runningValue.setLength(0);
                expectingNumber = false;
            } else if (!expectingNumber && (Character.isDigit(c) || c == '\0')) {
                if (runningNumber == -1) {
                    throw new IllegalArgumentException("Invalid format, should be # days # hours # minutes");
                }
                var unit = runningValue.toString().toUpperCase();
                if (DAYS.contains(unit)) {
                    if (days != 0) { throw new IllegalArgumentException("Can't repeat units"); }
                    days = runningNumber;
                } else if (HOURS.contains(unit)) {
                    if (hours != 0) { throw new IllegalArgumentException("Can't repeat units"); }
                    hours = runningNumber;
                } else if (MINUTES.contains(unit)) {
                    if (minutes != 0) { throw new IllegalArgumentException("Can't repeat units"); }
                    minutes = runningNumber;
                } else {
                    throw new IllegalArgumentException("Invalid format, should be # days # hours # minutes");
                }
                runningValue.setLength(0);
                runningNumber = -1;
                expectingNumber = true;
            }
            runningValue.append(c);
        }

        if (!expectingNumber) {
            throw new IllegalArgumentException("Invalid format, should be # days # hours # minutes");
        }

        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes);
    }

    private static final DateTimeFormatter PRETTY
            = DateTimeFormatter.ofPattern("MMM dd");

    public static String getPrettyDate(LocalDate date) {
        return date.format(PRETTY);
    }
}
