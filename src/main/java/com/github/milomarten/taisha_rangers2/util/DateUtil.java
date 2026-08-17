package com.github.milomarten.taisha_rangers2.util;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Various utilities for handling dates via commands.
 * Most of these methods are "casual" parsing, which supports a number of different human-friendly
 * parsing strategies. All supported formats are described in each method.
 */
public class DateUtil {
    private static final Map<String, ZoneId> OVERRIDES = new HashMap<>();
    static {
        OVERRIDES.put("NT", ZoneId.of("America/St_Johns"));
        OVERRIDES.put("AT", ZoneId.of("America/Halifax"));
        OVERRIDES.put("ET", ZoneId.of("America/New_York"));
        OVERRIDES.put("CT", ZoneId.of("America/Chicago"));
        OVERRIDES.put("MT", ZoneId.of("America/Denver"));
        OVERRIDES.put("PT", ZoneId.of("America/Los_Angeles"));
        OVERRIDES.put("AKT", ZoneId.of("America/Anchorage"));
        OVERRIDES.put("HT", ZoneId.of("Pacific/Honolulu"));
        OVERRIDES.put("NST", ZoneId.of("America/St_Johns"));
        OVERRIDES.put("AST", ZoneId.of("America/Halifax"));
        OVERRIDES.put("EST", ZoneId.of("America/New_York"));
        OVERRIDES.put("CST", ZoneId.of("America/Chicago"));
        OVERRIDES.put("MST", ZoneId.of("America/Denver"));
        OVERRIDES.put("PST", ZoneId.of("America/Los_Angeles"));
        OVERRIDES.put("AKST", ZoneId.of("America/Anchorage"));
        OVERRIDES.put("HST", ZoneId.of("Pacific/Honolulu"));
        OVERRIDES.put("NDT", ZoneId.of("America/St_Johns"));
        OVERRIDES.put("ADT", ZoneId.of("America/Halifax"));
        OVERRIDES.put("EDT", ZoneId.of("America/New_York"));
        OVERRIDES.put("CDT", ZoneId.of("America/Chicago"));
        OVERRIDES.put("MDT", ZoneId.of("America/Denver"));
        OVERRIDES.put("PDT", ZoneId.of("America/Los_Angeles"));
        OVERRIDES.put("AKDT", ZoneId.of("America/Anchorage"));
        OVERRIDES.put("HDT", ZoneId.of("Pacific/Honolulu"));
    }

    /**
     * Parse a timezone in a few different formats:
     * - Any tzid is supported, i.e. America/Chicago
     * - timezones of NST, NDT, or NT all map to America/St_Johns
     * - timezones of AST, ADT, or AT all map to America/Halifax
     * - timezones of EST, EDT, or ET all map to America/New_York
     * - timezones of CST, CDT, or CT all map to America/Chicago
     * - timezones of MST, MDT, or MT all map to America/Denver
     * - timezones of PST, PDT, or PT all map to America/Los_Angeles
     * - timezones of AKST, AKDT, or AKT all map to America/Anchorage
     * - timezones of HST, HDT, or HT all map to Pacific/Honolulu
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

    private static final Pattern TIME_PATTERN = Pattern.compile("\\d{1,4}");

    /**
     * Casually parse a time
     * Whitespace and colons are ignored. Of the remaining value:
     * - If ends in PM or P (case-insensitive), the time is converted to PM (possible hours are 1-12, mapping to 12-23)
     * - If ends in AM or A (case-insensitive), the time is assumed as AM (possible hours are 1-12, mapping to 0-11)
     * - If neither are present, the time is assumed as 24 hour time (possible hours are 0-23)
     * Once the suffix is removed, the remaining value is parsed thusly:
     * - If 1 or 2 digits are present, it is assumed to be the hour, and minutes are 00. AM/PM markers are required.
     * - If 3 digits are present, the first digit is assumed to be the hour, and the last two are the minutes.
     * - If 4 digits are present, the first two digits are assumed to be the hour, and the last two are the minutes.
     * - Any other number of digits throw an exception.
     * If an illegal time is provided (unrecognized characters, or an impossible hour or minute), an exception is thrown.
     * There is no parsing of seconds, since it is unnecessary for what I am using this for.
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
        if (TIME_PATTERN.matcher(value).hasMatch()) {
            int hour, minute;
            if (value.length() <= 2) {
                if (timeType == TimeType.TWENTY_FOUR_HOUR_TIME) {
                    throw new DateTimeException("1- or 2-digit time requires AM/PM");
                }
                hour = timeType.normalizeHour(Integer.parseInt(value));
                minute = 0;
            } else if (value.length() == 3) {
                hour = timeType.normalizeHour(Integer.parseInt(value.substring(0, 1)));
                minute = Integer.parseInt(value.substring(1, 3));
            } else if (value.length() == 4) {
                hour = timeType.normalizeHour(Integer.parseInt(value.substring(0, 2)));
                minute = Integer.parseInt(value.substring(2, 4));
            } else {
                // cannot be reached, since the regex clamps on 1-4 digits.
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
     * The value is split up to 3 times on any non-numerical values, and the results are parsed as so:
     * - If 2 groups are present (i.e. 3/14), the current year will be used, unless that would result in a date
     * in the past, whereupon next year is used instead.
     * - If 3 groups are present and the last group is 2 digits (i.e 3/14/26), the year is assumed to be relative
     * to 2000 (in this example, 26 becomes 2026). Any years not within 1 year of the current year will throw an exception.
     * - If 3 groups are present and the last group is more than 2 digits (i.e. 3/14/2026), the year is read as-is.
     * Any years not within 1 year of the current year will throw an exception.
     * Note that the expected format is American-style (Month, Day, Year). Sorry World, I can't support them all
     * unambiguously.
     * @param value The value to parse
     * @return The parsed date
     */
    public static LocalDate parseCasualDate(String value) {
        return parseCasualDate(value, Clock.systemDefaultZone(), DateFormat.MDY);
    }

    private static final Pattern NON_DIGITS = Pattern.compile("\\D+");

    static LocalDate parseCasualDate(String value, Clock clock, DateFormat dateFormat) {
        if (value.isEmpty()) {
            return null;
        }

        var elements = NON_DIGITS.split(value, 3);

        int year, month, day;
        if (elements.length == 2) {
            LocalDate now =  LocalDate.now(clock);

            month = Integer.parseInt(elements[dateFormat.getMonthIndex(true)]);
            day = Integer.parseInt(elements[dateFormat.getDayIndex(true)]);
            year = now.getYear();
            var requestedMD = MonthDay.of(month, day);
            var nowMD = MonthDay.from(now);
            if (requestedMD.isBefore(nowMD)) {
                year += 1;
            }
        } else if (elements.length == 3) {
            year = Integer.parseInt(elements[dateFormat.getYearIndex()]);
            month = Integer.parseInt(elements[dateFormat.getMonthIndex(false)]);
            day = Integer.parseInt(elements[dateFormat.getDayIndex(false)]);

            if (year < 100) {
                year += 2000;
            }

            var currentYear = Year.now(clock);
            if (year < currentYear.getValue() - 1 || year > currentYear.getValue() + 1) {
                throw new DateTimeException("Year should be two digits, or more than 1 year away");
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

    private static final Map<String, DayOfWeek> WEEK_ABBRS = new HashMap<>();
    static {
        for (var dow : DayOfWeek.values()) {
            WEEK_ABBRS.put(dow.toString().substring(0, 3), dow);
        }
    }

    /**
     * Parse a casual day of the week.
     * Format can be either the full name of the day of the week, or a
     * three-letter abbreviation of it. Value is case-insensitive.
     * @param value The value to parse
     * @return The parsed day of the week
     */
    public static DayOfWeek parseCasualDayOfWeek(String value) {
        var asCaps = value.toUpperCase();
        DayOfWeek parse;
        if (asCaps.length() == 3) {
            parse = WEEK_ABBRS.get(asCaps);
        } else {
            parse = EnumUtils.getEnum(DayOfWeek.class, asCaps);
        }
        return parse;
    }

    private static final DateTimeFormatter PRETTY
            = DateTimeFormatter.ofPattern("MMM dd");

    /**
     * Format a date in a nice way.
     * Discord does not natively have a way to do this without resolving to a full timestamp, which is
     * tricky when dealing with timezones. The format is "MMM dd", i.e. "Jul 3".
     * The months will be localized using the provided locale.
     * @param date The date to format
     * @param locale The locale to format into
     * @return The formatted string
     */
    public static String getPrettyDate(LocalDate date, Locale locale) {
        return date.format(PRETTY.localizedBy(locale));
    }

    /**
     * Get the next LocalDate after now that matches the provided day of the week.
     * Using Now as a reference point, scroll forward until a date is found that is on the specified DayOfWeek
     * This method will only move forward. Thus, if now happens to be on the starting day of week,
     * it will still advance by seven days exactly.
     * @param now The origin day
     * @param dowItNeedsToBe The day of the week it needs to be
     * @return The LocalDate matching this criteria
     */
    public static LocalDate getNextPossibleDate(LocalDate now, DayOfWeek dowItNeedsToBe) {
        // We need to scroll forward until we hit the requested date. with() doesn't work because
        // it can go backwards sometimes.
        int dayOffset = dowItNeedsToBe.getValue() - now.getDayOfWeek().getValue();
        if (dayOffset <= 0) {
            dayOffset += 7;
        }
        return now.plusDays(dayOffset);
    }
}
