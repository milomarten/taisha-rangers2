package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("create-party")
public class CreatePartyCommand extends CommandSpec<CreatePartyCommand.Parameters> {
    private final PartyManager partyManager;

    public CreatePartyCommand(PartyManager partyManager) {
        super("create-party", "Create a party");
        this.partyManager = partyManager;
        setParameterParser(PartyAdminParameters.parser(Parameters::new)
                .withParameterField(
                        "ping",
                        "A role that encompasses all the players in this party",
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
            return CommandResponse.reply(
                    String.format("Created party `%s`!", params.getPartyName()),
                    false
            );
        } else {
            return CommandResponse.reply(
                    String.format("Could not create party `%s`, one already exists with that name.", params.getPartyName()),
                    true
            );
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyAdminParameters {
        private Snowflake ping;
    }
}
