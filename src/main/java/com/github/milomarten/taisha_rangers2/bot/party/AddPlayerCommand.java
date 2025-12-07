package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Component("add-player")
public class AddPlayerCommand extends AbstractPartyAdminCommand<AddPlayerCommand.Parameters> {
    public AddPlayerCommand() {
        super("add-player");

        setParameterParser(
                PartyIdentityParameters.parser(Parameters::new)
                        .withParameterField(
                                "player",
                                SnowflakeParameter.builder().type(SnowflakeParameter.SnowflakeType.USER).build(),
                                Parameters::setPlayerToAdd
                        )
        );
    }

    @Override
    protected CommandResponse doProtectedPartyAction(Party party, AddPlayerCommand.Parameters params) {
        var worked = party.getPlayers().add(params.playerToAdd);
        if (worked) {
            return localizationFactory.createResponse(
                    "command.add-player.response",
                    FormatUtils.pingUser(params.playerToAdd),
                    params.getPartyName()
            ).ephemeral(false);
        } else {
            return localizationFactory.createResponse("command.add-player.error.player-exists")
                    .ephemeral(true);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyIdentityParameters {
        private Snowflake playerToAdd;
    }
}
