package com.github.milomarten.taisha_rangers2.command;

import com.github.milomarten.taisha_rangers2.command.parameter.ParameterInfo;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

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
