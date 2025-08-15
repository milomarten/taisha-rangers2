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
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Set;

@Component("set-start")
public class FormalizeStartSessionCommand extends CommandSpec<FormalizeStartSessionCommand.Parameters> {
    private final NextSessionManager manager;

    public FormalizeStartSessionCommand(NextSessionManager manager) {
        super("set-start", "Formalize the official start time of session");
        this.manager = manager;
        setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withInteractionField(PojoParameterParser.channelId(Parameters::setChannelId))
                .withParameterField(
                        "start-time",
                        "The formal start time for session.",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualDate),
                        Parameters::setEstimatedStart
                )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(FormalizeStartSessionCommand.Parameters params) {
        var worked = manager.setSessionDate(params.channelId, params.estimatedStart);
        if (worked) {
            return CommandResponse.reply("Alright! Session will officially start at "
                    + FormatUtils.formatShortDateTime(params.estimatedStart) + "!", false);
        } else {
            return CommandResponse.reply("No session???", true);
        }
    }

    @Data
    public static class Parameters {
        private Snowflake channelId;
        private ZonedDateTime estimatedStart;
    }
}
