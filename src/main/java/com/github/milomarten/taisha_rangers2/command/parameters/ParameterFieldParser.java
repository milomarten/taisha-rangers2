package com.github.milomarten.taisha_rangers2.command.parameters;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class ParameterFieldParser<PARAM, FIELD> {
    private final OneParameterParser<FIELD> parameter;
    private final BiConsumer<PARAM, FIELD> setter;

    public void set(ChatInputInteractionEvent event, PARAM param) {
        this.setter.accept(param, parameter.parse(event));
    }

    public ApplicationCommandOptionData toDiscordSpec() {
        return parameter.toDiscordSpec().getFirst();
    }
}
