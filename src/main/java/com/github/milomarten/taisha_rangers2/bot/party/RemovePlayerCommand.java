package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Component("remove-player")
public class RemovePlayerCommand extends AbstractPartyAdminCommand<RemovePlayerCommand.Parameters> {
    public RemovePlayerCommand() {
        super("remove-player");
        setParameterParser(
                PartyIdentityParameters.parser(Parameters::new)
                        .withParameterField(
                                "player",
                                SnowflakeParameter.builder().type(SnowflakeParameter.SnowflakeType.USER).build(),
                                Parameters::setPlayerToRemove
                        )
        );
    }

    @Override
    protected CommandResponse doProtectedPartyAction(Party party, Parameters params) {
        var worked = party.getPlayers().remove(params.playerToRemove);
        if (worked) {
            return localizationFactory.createResponse(
                    "command.remove-player.response",
                    FormatUtils.pingUser(params.playerToRemove),
                    params.getPartyName()
            ).ephemeral(false);
        } else {
            return localizationFactory.createResponse("command.remove-player.error.player-doesnt-exist")
                    .ephemeral(true);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyIdentityParameters {
        private Snowflake playerToRemove;
    }
}
