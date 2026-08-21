package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.command.LocalizedSubCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.dice.Dice;
import com.github.milomarten.taisha_rangers2.dice.DiceService;
import com.github.milomarten.taisha_rangers2.state.PlayerIdentity;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.entity.User;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.function.*;

@Component("scar")
public class ScarCommand extends LocalizedSubCommandSpec {
    private final FindPlayerService findPlayerService;
    private final DiceService diceService;

    public ScarCommand(FindPlayerService findPlayerService, DiceService diceService, ScarDoneCommand scarDoneCommand) {
        super("scar");
        this.findPlayerService = findPlayerService;
        this.diceService = diceService;
        addPath("dot", ScarDotCommand.parser(), pullNameAndInvokeDice(ScarDotCommand::run));
        addPath("initiative", ScarInitiativeCommand.parser(), pullNameAndInvokeDice(ScarInitiativeCommand::run));
        addPath("done", ScarDoneCommand.parser(), scarDoneCommand::run);
    }

    private <T extends ScarIdentityParameters> Function<T, CommandResponse> pullNameAndInvoke(BiFunction<T, FindPlayerService.PlayerContext, CommandResponse> func) {
        return in -> {
            var findName = findPlayerService.findPlayerCharacterName(in.getUserId(), in.getChannelId())
                    .orElse(new FindPlayerService.PlayerContext(in.getUserId(), new PlayerIdentity(in.getUserName()), null));
            return func.apply(in, findName);
        };
    }

    private <T extends ScarIdentityParameters> Function<T, CommandResponse> pullNameAndInvokeDice(TriFunction<T, FindPlayerService.PlayerContext, IntBinaryOperator, CommandResponse> func) {
        return in -> {
            var findName = findPlayerService.findPlayerCharacterName(in.getUserId(), in.getChannelId())
                    .orElse(new FindPlayerService.PlayerContext(in.getUserId(), new PlayerIdentity(in.getUserName()), null));
            IntBinaryOperator roller = (lower, upper) -> diceService.rollDice(
                    findName.user().asString(), lower, upper
            );
            return func.apply(in, findName, roller);
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

    private interface TriFunction<T, U, V, R> {
        public R apply(T one, U two, V three);
    }
}
