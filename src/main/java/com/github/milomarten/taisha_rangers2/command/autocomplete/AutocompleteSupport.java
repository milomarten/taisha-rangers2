package com.github.milomarten.taisha_rangers2.command.autocomplete;

import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;

import java.util.List;

public interface AutocompleteSupport {
    List<ApplicationCommandOptionChoiceData> getSuggestions(String parameterName, String valueSoFar);
}
