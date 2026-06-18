package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.command.LocalizedSubCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import discord4j.common.util.Snowflake;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Component("scar")
public class ScarCommand extends LocalizedSubCommandSpec {
    private final FindPlayerService findPlayerService;

    public ScarCommand(FindPlayerService findPlayerService) {
        super("scar");
        this.findPlayerService = findPlayerService;
        addPath("dot", ScarDotCommand.parser(), pullNameAndInvoke(ScarDotCommand::run));
    }

    private <T extends ScarIdentityParameters> Function<T, CommandResponse> pullNameAndInvoke(BiFunction<T, String, CommandResponse> func) {
        return in -> {
            var findName = findPlayerService.findPlayerCharacterName(in.getUserId(), in.getChannelId())
                    .orElse(in.getUserName());
            return func.apply(in, findName);
        };
    }

    @Data
    static class ScarIdentityParameters {
        private Snowflake userId;
        private String userName;
        private Snowflake channelId;

        public static <T extends ScarIdentityParameters> PojoParameterParser<T> parser(Supplier<T> constructor) {
            return new PojoParameterParser<>(constructor)
                    .withParameterField(PojoParameterParser.userId(), T::setUserId)
                    .withParameterField(PojoParameterParser.username(), T::setUserName)
                    .withParameterField(PojoParameterParser.channelId(), T::setChannelId);
        }
    }
}
