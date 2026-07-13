package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component("delay")
public class DelayCommand extends AbstractSessionAdminCommand<SessionIdentityParameters.One<Duration>> {
    public DelayCommand() {
        super("delay");

        setParameterParser(SessionIdentityParameters.One.parser(
                "duration",
                StringParameter.REQUIRED
                        .map(DateUtil::parseCasualDuration)
        ));
    }

    @Override
    protected CommandResponse doProtectedAction(SessionIdentityParameters.One<Duration> params, NextSession session) {
        var duration = params.getItem();
        if (session.getStartTime() != null) {
            session.setStartTime(session.getStartTime().plus(duration));
            return localizationFactory.createResponse("command.delay.response.start",
                    FormatUtils.pingRole(session.getPing()),
                    FormatUtils.formatShortDateTime(session.getStartTime()));
        } else {
            var alreadyResponded = session.getHydratedPlayerResponses()
                    .filter(pr -> pr.getState() != PlayerResponse.State.NO_RESPONSE)
                    .map(PlayerResponse::getPlayer)
                    .map(FormatUtils::pingUser)
                    .toList();
            session.setProposedStartTime(session.getProposedStartTime().plus(duration));
            if (alreadyResponded.isEmpty()) {
                return localizationFactory.createResponse("command.delay.response.proposed-start.nobody",
                        FormatUtils.pingRole(session.getPing()),
                        FormatUtils.formatShortDateTime(session.getProposedStartTime())
                );
            } else {
                return localizationFactory.createResponse("command.delay.response.proposed-start.somebody",
                        FormatUtils.pingRole(session.getPing()),
                        FormatUtils.formatShortDateTime(session.getProposedStartTime()),
                        String.join(", ", alreadyResponded)
                );
            }
        }
    }
}
