package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component("add-relevant-channel")
public class AddRelevantChannelCommand extends AbstractPartyAdminCommand<AddRelevantChannelCommand.Params> {
    public AddRelevantChannelCommand() {
        super("add-relevant-channel");

        setParameterParser(
                PartyIdentityParameters.parser(Params::new)
                        .withParameterField(
                                "channel",
                                SnowflakeParameter.builder().type(SnowflakeParameter.SnowflakeType.CHANNEL).build(),
                                Params::setChannel
                        )
        );
    }

    @Override
    protected CommandResponse doProtectedPartyAction(Party party, Params params) {
        if (party.getRelevantChannels() == null) {
            party.setRelevantChannels(new HashSet<>());
        }
        var worked = party.getRelevantChannels().add(params.channel);
        var ping = FormatUtils.mentionChannel(params.channel);
        if (worked) {
            return localizationFactory.createResponse("command.add-relevant-channel.response.worked",
                    party.getName(), ping);
        } else {
            return localizationFactory.createResponse("command.add-relevant-channel.response.already-exists",
                    party.getName(), ping)
                    .ephemeral(true);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Params extends PartyIdentityParameters {
        Snowflake channel;
    }
}
