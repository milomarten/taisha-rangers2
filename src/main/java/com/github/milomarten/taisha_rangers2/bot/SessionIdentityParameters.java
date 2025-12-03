package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import discord4j.common.util.Snowflake;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionIdentityParameters {
    private Snowflake userId;
    private Snowflake channelId;

    public static PojoParameterParser<SessionIdentityParameters> parser() {
        return new PojoParameterParser<>(SessionIdentityParameters::new)
                .withParameterField(PojoParameterParser.userId(), SessionIdentityParameters::setUserId)
                .withParameterField(PojoParameterParser.channelId(), SessionIdentityParameters::setChannelId);
    }

    public static <T extends SessionIdentityParameters> PojoParameterParser<T> parser(Supplier<T> constructor) {
        return new PojoParameterParser<>(constructor)
                .withParameterField(PojoParameterParser.userId(), SessionIdentityParameters::setUserId)
                .withParameterField(PojoParameterParser.channelId(), SessionIdentityParameters::setChannelId);
    }
}
