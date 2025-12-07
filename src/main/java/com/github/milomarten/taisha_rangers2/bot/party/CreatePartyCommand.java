package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("create-party")
public class CreatePartyCommand extends LocalizedCommandSpec<CreatePartyCommand.Parameters> {
    private final PartyManager partyManager;

    public CreatePartyCommand(PartyManager partyManager) {
        super("create-party");
        this.partyManager = partyManager;
        setParameterParser(PartyIdentityParameters.parser(Parameters::new)
                .withParameterField(
                        "ping",
                        SnowflakeParameter.builder()
                                .type(SnowflakeParameter.SnowflakeType.ROLE)
                                .defaultValue(null)
                                .build()
                        ,
                        Parameters::setPing
                )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(CreatePartyCommand.Parameters params) {
        var worked = partyManager.createParty(
                params.getPartyName(),
                params.getUserId(),
                params.ping
        );

        if (worked) {
            return localizationFactory.createResponse(
                    "command.create-party.response", params.getPartyName()
            ).ephemeral(false);
        } else {
            return localizationFactory.createResponse(
                    "command.create-party.error.party-exists", params.getPartyName()
            ).ephemeral(true);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyIdentityParameters {
        private Snowflake ping;
    }
}
