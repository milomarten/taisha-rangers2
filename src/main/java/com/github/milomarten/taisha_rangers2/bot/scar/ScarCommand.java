package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.command.LocalizedSubCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.PlayerIdentity;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.entity.User;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Component("scar")
public class ScarCommand extends LocalizedSubCommandSpec {
    private final FindPlayerService findPlayerService;

    public ScarCommand(FindPlayerService findPlayerService, ScarDoneCommand scarDoneCommand) {
        super("scar");
        this.findPlayerService = findPlayerService;
        addPath("dot", ScarDotCommand.parser(), pullNameAndInvoke(ScarDotCommand::run));
        addPath("initiative", ScarInitiativeCommand.parser(), pullNameAndInvoke(ScarInitiativeCommand::run));
        addPath("done", ScarDoneCommand.parser(), scarDoneCommand::run);
    }

    private <T extends ScarIdentityParameters> Function<T, CommandResponse> pullNameAndInvoke(BiFunction<T, FindPlayerService.PlayerContext, CommandResponse> func) {
        return in -> {
            var findName = findPlayerService.findPlayerCharacterName(in.getUserId(), in.getChannelId())
                    .orElse(new FindPlayerService.PlayerContext(in.getUserId(), new PlayerIdentity(in.getUserName()), null));
            return func.apply(in, findName);
        };
    }

    @Data
    static class ScarIdentityParameters {
        private User user;
        private String userName;
        private Snowflake channelId;

        public static <T extends ScarIdentityParameters> PojoParameterParser<T> parser(Supplier<T> constructor) {
            return new PojoParameterParser<>(constructor)
                    .withParameterField(InteractionCreateEvent::getUser, T::setUser)
                    .withParameterField(PojoParameterParser.username(), T::setUserName)
                    .withParameterField(PojoParameterParser.channelId(), T::setChannelId);
        }

        public Snowflake getUserId() {
            return user.getId();
        }
    }
}
