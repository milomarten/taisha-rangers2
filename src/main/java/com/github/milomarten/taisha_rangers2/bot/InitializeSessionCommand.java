package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.rest.util.AllowedMentions;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Set;

@Component("init")
public class InitializeSessionCommand extends CommandSpec<InitializeSessionCommand.Parameters> {
    private final NextSessionManager manager;

    public InitializeSessionCommand(NextSessionManager manager) {
        super("init", "Create an upcoming session");
        this.manager = manager;
        setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withInteractionField(PojoParameterParser.channelId(Parameters::setChannelId))
                .withInteractionField(PojoParameterParser.userId(Parameters::setGm))
                .withParameterField(
                        "number-of-players",
                        "The number of players in the session. Default: 4",
                        IntParameter.builder().defaultValue(4).minValue(1).build(),
                        Parameters::setNumPlayers
                )
                .withParameterField(
                        "proposed-start-time",
                        "The proposed start time for session.",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualDateTime),
                        Parameters::setProposedStart
                )
                .withParameterField(
                        "ping",
                        "The role to ping for announcements. Default: @Taisha Rangers",
                        SnowflakeParameter.builder()
                                .type(SnowflakeParameter.SnowflakeType.ROLE)
                                .defaultValue(Snowflake.of(1169680518531518544L))
                                .build(),
                        Parameters::setPing
                )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(InitializeSessionCommand.Parameters params) {
        manager.createSession(
                params.channelId,
                params.ping,
                params.gm,
                params.numPlayers,
                params.proposedStart
        );

        var pingText = params.ping == null ? "everyone" : FormatUtils.pingRole(params.ping);
        var text = String.format("Hey %s! A session has been scheduled for %s. Let me know if you can join, by typing `/yes` or `/no`!",
                pingText, FormatUtils.formatShortDateTime(params.proposedStart));
        return CommandResponse.reply(text, false)
                .allowedMentions(AllowedMentions.builder().allowRole(params.ping).build());
    }

    @Data
    public static class Parameters {
        private Snowflake channelId;
        private Snowflake ping;
        private Snowflake gm;
        private int numPlayers;
        private ZonedDateTime proposedStart;
    }
}
