package com.github.milomarten.taisha_rangers2.command.localization;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A combination of a default string, and a group of localized translations of that string
 * @param key The default string
 * @param translations A map of translations, keyed on locale.
 */
public record LocalizedStrings (String key, Map<Locale, String> translations) {
    /**
     * Adapter from a hard-coded string with no translations
     * @param raw The raw string. This will always be used regardless of locale
     * @return A LocalizedStrings of just the raw string
     */
    public static LocalizedStrings of(String raw) {
        return new LocalizedStrings(raw, null);
    }

    /**
     * Convert the translation map for use in Discord
     * @return A map that is the same as translations(), but with Discord locales
     */
    public Map<String, String> getDiscordifiedTranslations() {
        if (translations == null) { return null; }

        return translations.keySet()
                .stream()
                .collect(Collectors.toMap(
                        DiscordLocales::toDiscord,
                        translations::get
                ));
    }
}
