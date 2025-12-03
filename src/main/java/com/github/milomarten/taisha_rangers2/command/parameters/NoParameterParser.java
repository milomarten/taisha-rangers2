package com.github.milomarten.taisha_rangers2.command.parameters;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A ParameterParser for a command with no parameters
 * All calls to `parse()` return null, and `toDiscordSpec()` returns an empty list
 * @param <T> The "type" parsed from the command. Can be anything, to allow for generics
 */
@NoArgsConstructor(staticName = "create")
public class NoParameterParser<T> implements ParameterParser<T> {
    @Override
    public T parse(ChatInputInteractionEvent event) {
        return null;
    }

    @Override
    public List<ApplicationCommandOptionData> toDiscordSpec(Localizer localizer) {
        return List.of();
    }
}
