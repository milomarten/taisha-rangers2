package com.github.milomarten.taisha_rangers2.command.parameters;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;

/**
 * Indicates a way to parse multiple fields from a command usage into one object
 * @param <PARAM> The parameter object to be created
 */
public interface ParameterParser<PARAM> {
    /**
     * Parse the command usage into an object
     * @param event The event to parse from
     * @return The created parameter object
     */
    PARAM parse(ChatInputInteractionEvent event);

    /**
     * Create the parameter specs for the parameters that make up this object
     * @return The list of ApplicationCommandOptionData specs for the various fields desired
     */
    List<ApplicationCommandOptionData> toDiscordSpec(Localizer localizer);
}
