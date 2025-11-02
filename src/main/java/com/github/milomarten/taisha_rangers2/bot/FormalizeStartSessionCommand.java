package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Component("set-start")
public class FormalizeStartSessionCommand extends CommandSpec<FormalizeStartSessionCommand.Parameters> {
    private final NextSessionManager manager;

    public FormalizeStartSessionCommand(NextSessionManager manager) {
        super("set-start", "Formalize the official start time of session");
        this.manager = manager;
        setParameterParser(SessionAdminParams.parser(Parameters::new)
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
        return manager.updateAndReturn(params.getChannelId(), ns -> {
            if (Objects.equals(ns.getGm(), params.getUserId())) {
                ns.setStartTime(estimatedStart.apply(ns));
                return CommandResponse.reply(String.
                        format("Alright, session will formally start at %s!", FormatUtils.formatShortDateTime(ns.getStartTime())),
                            false);
            } else {
                return CommandResponse.reply(
                        "You're not the DM!", true
                );
            }
        }).orElseGet(() -> {
            return CommandResponse.reply("No session???", true);
        });
    }

    private ZonedDateTime findBestStartTime(NextSession nextSession) {
        if (nextSession.allPlayersRespondedYes()) {
            return nextSession.getPlayerResponses()
                    .values()
                    .stream()
                    .map(PlayerResponse::getAfterTime)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(nextSession.getProposedStartTime());
        }
        return null;
    }

    @Getter @Setter
    public static class Parameters extends SessionAdminParams {
        private String estimatedStart;
    }
}
