package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.BooleanParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.state.PlayerIdentity;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("add-player")
public class AddPlayerCommand extends AbstractPartyAdminCommand<AddPlayerCommand.Parameters> {
    private final GatewayDiscordClient gdc;

    public AddPlayerCommand(@Autowired(required = false) GatewayDiscordClient gdc) {
        super("add-player");
        this.gdc = gdc;

        setParameterParser(
                PartyIdentityParameters.parser(Parameters::new)
                        .withParameterField(
                                c -> c.getInteraction().getGuildId(),
                                (p, s) -> s.ifPresent(p::setGuildId))
                        .withParameterField(
                                "player",
                                SnowflakeParameter.builder().type(SnowflakeParameter.SnowflakeType.USER).build(),
                                Parameters::setPlayerToAdd
                        )
                        .withParameterField(
                                "identity",
                                StringParameter.REQUIRED,
                                Parameters::setPlayerIdentity
                        )
                        .withParameterField(
                                "quiet",
                                BooleanParameter.DEFAULT_FALSE,
                                Parameters::setQuiet
                        )
        );
    }

    @Override
    protected CommandResponse doProtectedPartyAction(Party party, AddPlayerCommand.Parameters params) {
        if (party.getPlayerIdentities().containsKey(params.playerToAdd)) {
            var identity = party.getPlayerIdentities().get(params.playerToAdd);
            identity.setName(params.playerIdentity);
        } else {
            party.getPlayerIdentities().put(params.playerToAdd, new PlayerIdentity(params.playerIdentity));
        }

        assignRole(party.getName(), params.guildId, params.playerToAdd, party.getPing());

        return localizationFactory.createResponse(
                "command.add-player.response",
                FormatUtils.pingUser(params.playerToAdd) + "(" + params.playerIdentity + ")",
                params.getPartyName()
        ).ephemeral(params.quiet);
    }

    private void assignRole(String partyName, Snowflake guildId, Snowflake userId, Snowflake roleId) {
        if (gdc != null && roleId != null && guildId != null) {
            gdc.getMemberById(guildId, userId)
                    .flatMap(m -> m.addRole(roleId, "Joined " + partyName))
                    .subscribe();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyIdentityParameters {
        private Snowflake guildId;
        private Snowflake playerToAdd;
        private String playerIdentity;
        private boolean quiet;
    }
}
