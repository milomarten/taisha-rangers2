package com.github.milomarten.taisha_rangers2.util;

import com.github.milomarten.taisha_rangers2.state.PartyTime;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

/**
 * Various utilities for handling dates via commands
 */
public class DateUtil {
    @Deprecated private static final ZoneId CENTRAL_TIME = ZoneId.of("America/Chicago");
    @Deprecated private static final LocalTime DEFAULT_TIME = LocalTime.of(18, 0);

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
     * - YYYY-MM-DD -> Assumes the party's standard time and timezone
     * - MM-DD -> Assumes the party's standard time and timezone, and the year that yields the soonest future date. Examples:
     *  - Input: 08-15, Today: 2025-08-01 -> Year is 2025 (since Aug 15th is after August 1st)
     *  - Input: 01-02, Today: 2025-12-25 -> Year is 2026 (since Jan 2nd is before December 25th, when not taking years into account)
     * - YYYY-MM-DD HH:MM -> YYYY-MM-DDTHH:MM:00, using the party's standard timezone.
     * - YYYY-MM-DD HH:MM:SS -> YYYY-MM-DDTHH:MM:SS, using the party's standard timezone.
     * - YYYY-MM-DD HH:MM,CST -> YYYY-MM-DDTHH:MM:00, using the timezone provided after the comma. This can be anything parsed by parseCasualTimezone()
     * - 2025-08-13T08:00-05:00 -> Fully formed date and time, no changes
     * - 2025-08-13T08:00-05:00[America/Chicago] -> Fully formed date and time, no changes
     * If the usual time is not provided, and a format requires it, a NPE is thrown.
     * @param value The string to parse
     * @return The parsed ZonedDateTime
     */
    public static ZonedDateTime parseCasualDateTime(String value, PartyTime usual) {
        return parseCasualDateTime(
                value,
                () -> Clock.system(usual.getTimezone()),
                usual::getTimeOfDay);
    }

    /**
     * Legacy parseCasualDateTime
     * Uses a default timezone of America/Chicago, and a default starting time of 6pm.
     * This should only be used in tests, and will be deprecated.
     * @param value The value to parse
     * @return The parsed ZonedDateTime
     */
    public static ZonedDateTime parseCasualDateTime(String value) {
        return parseCasualDateTime(value,
                () -> Clock.system(CENTRAL_TIME),
                () -> DEFAULT_TIME);
    }

    /**
     * Testing parseCasualDateTime
     * Used in test classes, to control contextual year tests. The default time of 6pm is used.
     * This should only be used in tests, and will be deprecated
     * @param value The value to parse
     * @param clock A clock which will be used in computation
     * @return The parsed ZonedDateTime
     */
    public static ZonedDateTime parseCasualDateTime(String value, Clock clock) {
        return parseCasualDateTime(value, () -> clock, () -> DEFAULT_TIME);
    }

    /**
     * parseCasualDateTime with possibly-necessary context.
     * A number of different formats are supported. In some of those formats, extra context is required, but in
     * others, it is unnecessary. The usage of Suppliers is to encourage that the parameters are created lazily. If
     * they do not exist, a NPE is thrown, which can be handled by the calling services.
     * timeFunc is used to get the default time, for formats that omit the time.
     * clockFunc is used to get the timezone, as well as for implicit year computation.
     * @param value The value to parse
     * @param clockFunc A supplier which will lazily create a clock if necessary.
     * @return The parsed ZonedDateTime
     */
    public static ZonedDateTime parseCasualDateTime(String value, Supplier<Clock> clockFunc, Supplier<LocalTime> timeFunc) {
        if (value.isEmpty()) {
            return null;
        } else if (value.length() == 10) {
            // 2025-08-13. Assume default time and Central
            return LocalDate.parse(value)
                    .atTime(timeFunc.get())
                    .atZone(clockFunc.get().getZone());
        } else if (value.length() == 5) {
            // 08-13. Assume relevant year, default time, and Central
            var md = MonthDay.parse("--" + value);
            var today = LocalDate.now(clockFunc.get());
            int yearOffset = 0;
            if (MonthDay.from(today).isAfter(md)) {
                yearOffset = 1;
            }
            return md.atYear(Year.from(today).getValue() + yearOffset)
                    .atTime(timeFunc.get())
                    .atZone(clockFunc.get().getZone());
        } else if (value.length() == 19 || value.length() == 16) {
            //2025-08-13 08:00:00. Seconds is optional. Assume Central
            if (value.length() == 16) {
                value += ":00";
            }
            value = value.replace(' ', 'T');
            return LocalDateTime.parse(value)
                    .atZone(clockFunc.get().getZone());
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
            //2025-08-13T08:00:00-05:00[America/Chicago]
            return ZonedDateTime.parse(value);
        } else {
            // Allows for a complete string if you so desire.
            // This one doesn't require the actual tzid.
            //2025-08-13T08:00:00-05:00
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
        if (value.isEmpty()) return null;
        return Objects.requireNonNullElseGet(OVERRIDES.get(value.toUpperCase()), () -> ZoneId.of(value));
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

    /**
     * Parse a timezone-less date casually
     * Format can be in mm/dd, yy/mm/dd, or yyyy/mm/dd. Any non-number can
     * be used in place of the /.
     * If year is omitted, the nearest future year will be used. The system timezone
     * will be used to determine the "present", and thus, the future. This is only a problem
     * when the parsed date is very close to the actual date, and may result in the next year being
     * used instead of the current.
     * If a two-digit year is used, it is assumed to be relative to the year 2000.
     * @param value The value to parse
     * @return The parsed date
     */
    public static LocalDate parseCasualDate(String value) {
        return parseCasualDate(value, Clock.systemDefaultZone());
    }

    /**
     * Parse a timezone-less date casually, using a provided clock for computations
     * The same as parseCasualDate(value), but the provided Clock will be used when computing
     * omitted years. This is intended for testing, to keep values static.
     * @param value The value to parse
     * @param clock The clock to use to compute the present
     * @return The parsed date
     */
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

            if (year < 100) {
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

    private static boolean isNumberCharacter(char c) {
        return Character.isDigit(c) || c == '-' || c == '+' || c == '.';
    }

    /**
     * Parse a casual duration.
     * Format can be any combination of `# days`, `# hours` and `# minutes`, with any amount of whitespace
     * in between. Units are case insensitive, and the following are accepted:
     * - DAY, DAYS, D
     * - H, HR, HRS, HOURS, HOUR
     * - M, MIN, MINS, MINUTES
     * Seconds and milliseconds are not supported, since that level of granularity is overkill for a reminder
     * system. Units cannot be repeated in the same string, and each unit is required to have an associated number.
     * <br>
     * Numbers may be negative. If representing a time in the past, all numbers should be negative, i.e. -1 day -8 hours.
     * if a postive is provided, it will advance the duration closer to 0, rather than away.
     * @param value The string to parse
     * @return The yielded Duration
     */
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
            if (expectingNumber && !isNumberCharacter(c)) {
                runningNumber = parseAndValidateNumber(runningValue.toString());
                runningValue.setLength(0);
                expectingNumber = false;
            } else if (!expectingNumber && (isNumberCharacter(c) || c == '\0')) {
                if (runningNumber == -1) {
                    throw new IllegalArgumentException("Encountered unit " + runningValue + " without number");
                }
                var unit = runningValue.toString().toUpperCase();
                if (DAYS.contains(unit)) {
                    if (days != 0) { throw new IllegalArgumentException("Can't repeat days"); }
                    days = runningNumber;
                } else if (HOURS.contains(unit)) {
                    if (hours != 0) { throw new IllegalArgumentException("Can't repeat hours"); }
                    hours = runningNumber;
                } else if (MINUTES.contains(unit)) {
                    if (minutes != 0) { throw new IllegalArgumentException("Can't repeat minutes"); }
                    minutes = runningNumber;
                } else {
                    throw new IllegalArgumentException("Unknown unit " + runningValue + ", I accept days, hours, and minutes");
                }
                runningValue.setLength(0);
                runningNumber = -1;
                expectingNumber = true;
            }
            runningValue.append(c);
        }

        if (!expectingNumber) {
            throw new IllegalArgumentException("Encountered number without unit");
        }

        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes);
    }

    private static int parseAndValidateNumber(String value) {
        BigInteger bi;
        try {
            bi = new BigInteger(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Number " + value + "is malformed: " + ex.getMessage());
        }
        try {
            var asInt = bi.intValueExact();
            if (asInt < 0) {
                throw new IllegalArgumentException("Number can't be negative");
            }
            return asInt;
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Number " + value + " is too big");
        }
    }

    private static final DateTimeFormatter PRETTY
            = DateTimeFormatter.ofPattern("MMM dd");

    /**
     * Format a date in a nice way.
     * Discord does not natively have a way to do this without resolving to a full timestamp, which is
     * tricky when dealing with timezones. The format is "MMM dd", i.e. "Jul 3".
     * Future enhancement: localize
     * @param date The date to format.
     * @return The formatted date.
     */
    public static String getPrettyDate(LocalDate date) {
        return date.format(PRETTY);
    }
}
