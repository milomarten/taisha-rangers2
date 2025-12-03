package com.github.milomarten.taisha_rangers2.command.parameters;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
class PojoSetterParser<PARAM, FIELD> {
    private final ParameterParser<FIELD> parameter;
    private final BiConsumer<PARAM, FIELD> setter;

    public void set(ChatInputInteractionEvent event, PARAM param) {
        this.setter.accept(param, parameter.parse(event));
    }

    public List<ApplicationCommandOptionData> toDiscordSpec(Localizer localizer) {
        return parameter.toDiscordSpec(localizer);
    }
}
