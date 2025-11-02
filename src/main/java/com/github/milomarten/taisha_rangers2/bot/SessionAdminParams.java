package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import discord4j.common.util.Snowflake;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class SessionAdminParams {
    private Snowflake userId;
    private Snowflake channelId;

    public static PojoParameterParser<SessionAdminParams> parser() {
        return new PojoParameterParser<>(SessionAdminParams::new)
                .withParameterField(PojoParameterParser.userId(), SessionAdminParams::setUserId)
                .withParameterField(PojoParameterParser.channelId(), SessionAdminParams::setChannelId);
    }

    public static <T extends SessionAdminParams> PojoParameterParser<T> parser(Supplier<T> constructor) {
        return new PojoParameterParser<>(constructor)
                .withParameterField(PojoParameterParser.userId(), SessionAdminParams::setUserId)
                .withParameterField(PojoParameterParser.channelId(), SessionAdminParams::setChannelId);
    }
}
