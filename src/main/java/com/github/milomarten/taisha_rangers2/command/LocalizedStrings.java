package com.github.milomarten.taisha_rangers2.command;

import java.util.Locale;
import java.util.Map;

public record LocalizedStrings (String def, Map<Locale, String> translations) {
}
