package com.github.milomarten.taisha_rangers2.command;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utilities for working with Locales in Discord.
 * Discord supports only a subset of locales that Java supports. As such, converting from Discord
 * to Java will always work. When going in reverse, the best Discord option is provided,
 * falling back to US English if none can be found.
 * <br>
 * Discord supports Spanish LATAM, which is `es-419`, which does not have a direct correlation
 * in Java. This service converts it to and from `es_US`, like at my own job.
 * <br>
 * Almost all Discord locales are just a language, not tied to any country. As such, if the input locale contains
 * that language, that will be the match.
 * For those languages that are tied to a country, the input locale must match both language and country. If the country
 * does not match and options, there is a default country for these locales that will fallback instead:
 * - English -> en-US
 * - Spanish -> es-ES
 * - Chinese -> zh-CN
 * Swedish and Portuguese are tied to countries, but there is only one option at this time for those languages
 * (sv_SE and pt_BR).
 */
public class DiscordLocales {
    public static final Locale INDONESIAN_LOCALE = Locale.of("id");
    public static final Locale DANISH_LOCALE = Locale.of("da");
    public static final Locale GERMAN_LOCALE = Locale.of("de");
    public static final Locale ENGLISH_UK_LOCALE = Locale.of("en", "GB");
    public static final Locale ENGLISH_US_LOCALE = Locale.of("en", "US");
    public static final Locale SPANISH_LOCALE = Locale.of("es", "ES");
    public static final Locale FRENCH_LOCALE = Locale.of("fr");
    public static final Locale CROATIAN_LOCALE = Locale.of("hr");
    public static final Locale ITALIAN_LOCALE = Locale.of("it");
    public static final Locale LITHUANIAN_LOCALE = Locale.of("lt");
    public static final Locale HUNGARIAN_LOCALE = Locale.of("hu");
    public static final Locale DUTCH_LOCALE = Locale.of("nl");
    public static final Locale NORWEGIAN_LOCALE = Locale.of("no");
    public static final Locale POLISH_LOCALE = Locale.of("pl");
    public static final Locale PORTUGUESE_BRAZILIAN_LOCALE = Locale.of("pt", "BR");
    public static final Locale ROMANIAN_ROMANIA_LOCALE = Locale.of("ro");
    public static final Locale FINNISH_LOCALE = Locale.of("fi");
    public static final Locale SWEDISH_LOCALE = Locale.of("sv", "SE");
    public static final Locale VIETNAMESE_LOCALE = Locale.of("vi");
    public static final Locale TURKISH_LOCALE = Locale.of("tr");
    public static final Locale CZECH_LOCALE = Locale.of("cs");
    public static final Locale GREEK_LOCALE = Locale.of("el");
    public static final Locale BULGARIAN_LOCALE = Locale.of("bg");
    public static final Locale RUSSIAN_LOCALE = Locale.of("ru");
    public static final Locale UKRAINIAN_LOCALE = Locale.of("uk");
    public static final Locale HINDI_LOCALE = Locale.of("hi");
    public static final Locale THAI_LOCALE = Locale.of("th");
    public static final Locale CHINESE_CHINA_LOCALE = Locale.of("zh", "CN");
    public static final Locale JAPANESE_LOCALE = Locale.of("ja");
    public static final Locale CHINESE_TAIWAN_LOCALE = Locale.of("zh", "TW");
    public static final Locale KOREAN_LOCALE = Locale.of("ko");
    public static final Locale LATAM_LOCALE = Locale.of("es", "US");

    public static final String LATAM = "es-419";

    private static final LocaleChain CHAIN = new LocaleChain(Locale.US)
            .add(INDONESIAN_LOCALE)
            .add(DANISH_LOCALE)
            .add(GERMAN_LOCALE)
            .add(ENGLISH_UK_LOCALE)
            .add(ENGLISH_US_LOCALE, true)
            .add(SPANISH_LOCALE, true)
            .add(FRENCH_LOCALE)
            .add(CROATIAN_LOCALE)
            .add(ITALIAN_LOCALE)
            .add(LITHUANIAN_LOCALE)
            .add(HUNGARIAN_LOCALE)
            .add(DUTCH_LOCALE)
            .add(NORWEGIAN_LOCALE)
            .add(POLISH_LOCALE)
            .add(PORTUGUESE_BRAZILIAN_LOCALE, true)
            .add(ROMANIAN_ROMANIA_LOCALE)
            .add(FINNISH_LOCALE)
            .add(SWEDISH_LOCALE, true)
            .add(VIETNAMESE_LOCALE)
            .add(TURKISH_LOCALE)
            .add(CZECH_LOCALE)
            .add(GREEK_LOCALE)
            .add(BULGARIAN_LOCALE)
            .add(RUSSIAN_LOCALE)
            .add(UKRAINIAN_LOCALE)
            .add(HINDI_LOCALE)
            .add(THAI_LOCALE)
            .add(CHINESE_CHINA_LOCALE, true)
            .add(JAPANESE_LOCALE)
            .add(CHINESE_TAIWAN_LOCALE)
            .add(KOREAN_LOCALE);

    /**
     * Convert the Discord locale into its corresponding Java Locale
     * @param discordLocale The locale in Discord's format
     * @return The locale in Java's format
     */
    public static Locale fromDiscord(String discordLocale) {
        if (LATAM.equals(discordLocale)) { return LATAM_LOCALE; }
        return Locale.forLanguageTag(discordLocale);
    }

    /**
     * Convert the Java locale into its corresponding Discord Locale
     * @param locale The locale in Java's format
     * @return The locale in Discord's format
     */
    public static String toDiscord(Locale locale) {
        if (LATAM_LOCALE.equals(locale)) { return LATAM; }
        return CHAIN.get(locale).toLanguageTag();
    }

    @RequiredArgsConstructor
    private static class LocaleChain {
        private final Locale fallback;
        private final Map<String, Locale> languageOnly = new HashMap<>();
        private final Map<String, Locale> countryLanguage = new HashMap<>();

        public LocaleChain add(Locale locale) {
            return add(locale, false);
        }

        public LocaleChain add(Locale locale, boolean defaultForLanguageOnly) {
            var languageCode = locale.getLanguage();
            var countryCode = locale.getCountry();

            if (countryCode.isEmpty()) {
                languageOnly.put(languageCode, locale);
            } else {
                countryLanguage.put(languageCode + "-" + countryCode, locale);
                if (defaultForLanguageOnly) {
                    languageOnly.put(languageCode, locale);
                }
            }

            return this;
        }

        public Locale get(Locale start) {
            var languageCode = start.getLanguage();
            var countryCode = start.getCountry();

            if (countryCode.isEmpty()) {
                return languageOnly.getOrDefault(languageCode, fallback);
            } else {
                var firstPass = countryLanguage.get(languageCode + "-" + countryCode);
                if (firstPass == null) {
                    return languageOnly.getOrDefault(languageCode, fallback);
                }
                return firstPass;
            }
        }
    }
}
