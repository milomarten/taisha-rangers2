package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component("no")
public class NoCommand extends CommandSpec<NoCommand.Parameters> {
    private final NextSessionManager nextSessionManager;

    public NoCommand(NextSessionManager nextSessionManager) {
        super("no", "Indicate that you won't make it to session");
        this.nextSessionManager = nextSessionManager;

        this.setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withInteractionField(PojoParameterParser.user(Parameters::setUser))
                .withInteractionField(PojoParameterParser.channelId(Parameters::setChannelId))
        );
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        var worked = nextSessionManager.playerDo(params.channelId, params.getUser().getId(),
                (session, response) -> response.no());
        if (worked) {
            return CommandResponse.reply(params.getUser().getUsername() + " will NOT be able to attend session this week.", false);
        } else {
            return CommandResponse.reply("No upcoming session???", true);
        }
    }

    @Data
    public static class Parameters {
        Snowflake channelId;
        User user;
    }
}
