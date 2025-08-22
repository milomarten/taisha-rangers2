package com.github.milomarten.taisha_rangers2.command.parameters;

import com.github.milomarten.taisha_rangers2.command.parameter.ParameterInfo;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;

/**
 * A ParameterParser for a command with one parameter
 * @param name The name of the parameter
 * @param description The description of the parameter
 * @param metadata The ParameterInfo, which describes the type and any validations
 * @param <PARAM> The type of the parameter
 */
public record OneParameterParser<PARAM>(String name, String description,
                                        ParameterInfo<PARAM> metadata) implements ParameterParser<PARAM> {
    @Override
    public PARAM parse(ChatInputInteractionEvent event) {
        return metadata.convert(event, name);
    }

    @Override
    public List<ApplicationCommandOptionData> toDiscordSpec() {
        var builder = ApplicationCommandOptionData.builder()
                .name(name)
                .description(description);
        builder = metadata.decorate(builder);
        return List.of(builder.build());
    }
}
