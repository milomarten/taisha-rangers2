package com.github.milomarten.taisha_rangers2.command.parameter;

import com.github.milomarten.taisha_rangers2.command.localization.Localizer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * A ParameterInfo which extracts a Snowflake from the command usage.
 * Discord bots are allowed to accept, as parameters, four different types of Snowflakes:
 * - User: Must be the ID of a user in the server
 * - Role: Must be the ID of a role in the server
 * - Channel: Must be the ID of a channel is the server
 * - Mentionable: Must be the ID of either a user OR a role in the server
 * For the sake of the decorator, it is a requirement to specify one of these four options with type.
 */
@Builder
public class SnowflakeParameter implements ParameterInfo<Snowflake> {
    /**
     * The type of Snowflake expected
     * Must be specified. If null, decorate() will throw a NPE
     */
    private SnowflakeType type;
    /**
     * The default Snowflake. If null or unset, the parameter is considered REQUIRED.
     */
    private Snowflake defaultValue;

    @Override
    public Snowflake convert(ChatInputInteractionEvent event, String field) {
        return event.getOptionAsSnowflake(field)
                .or(() -> Optional.ofNullable(this.defaultValue))
                .orElseThrow();
    }

    @Override
    public ImmutableApplicationCommandOptionData.Builder decorate(ImmutableApplicationCommandOptionData.Builder builder, Localizer localizer) {
        return builder
                .type(this.type.value)
                .required(defaultValue == null);
    }

    @RequiredArgsConstructor
    public enum SnowflakeType {
        USER(ApplicationCommandOption.Type.USER.getValue()),
        ROLE(ApplicationCommandOption.Type.ROLE.getValue()),
        CHANNEL(ApplicationCommandOption.Type.CHANNEL.getValue()),
        MENTIONABLE(ApplicationCommandOption.Type.MENTIONABLE.getValue()),;

        private final int value;
    }
}
