package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import discord4j.common.util.Snowflake;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

@Component("set-start")
public class FormalizeStartSessionCommand extends CommandSpec<FormalizeStartSessionCommand.Parameters> {
    private final NextSessionManager manager;

    public FormalizeStartSessionCommand(NextSessionManager manager) {
        super("set-start", "Formalize the official start time of session");
        this.manager = manager;
        setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withParameterField(PojoParameterParser.channelId(), Parameters::setChannelId)
                .withParameterField(
                        "start-time",
                        "The formal start time for session.",
                        StringParameter.DEFAULT_EMPTY_STRING,
                        Parameters::setEstimatedStart
                )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(FormalizeStartSessionCommand.Parameters params) {
        Function<NextSession, ZonedDateTime> estimatedStart = params.estimatedStart.isEmpty() ?
                this::findBestStartTime :
                (ns) -> DateUtil.parseCasualDateTime(params.estimatedStart);
        var worked = manager.setSessionDate(params.channelId, estimatedStart);
        if (worked) {
            return CommandResponse.reply("Neat! It's done. Trust me.", true);
        } else {
            return CommandResponse.reply("No session???", true);
        }
    }

    private ZonedDateTime findBestStartTime(NextSession nextSession) {
        if (nextSession.allPlayersRespondedYes()) {
            return nextSession.getPlayerResponses()
                    .values()
                    .stream()
                    .map(PlayerResponse::getAfterTime)
                    .max(Comparator.naturalOrder())
                    .orElse(nextSession.getProposedStartTime());
        }
        return null;
    }

    @Data
    public static class Parameters {
        private Snowflake channelId;
        private String estimatedStart;
    }
}
