package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component("set-start")
public class FormalizeStartSessionCommand extends AbstractSessionAdminCommand<FormalizeStartSessionCommand.Parameters> {
    public FormalizeStartSessionCommand() {
        super("set-start");
        setParameterParser(SessionIdentityParameters.parser(Parameters::new)
                .withParameterField(
                        "start-time",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualDateTime),
                        Parameters::setEstimatedStart
                )
        );
    }

    @Override
    protected CommandResponse doProtectedAction(Parameters params, NextSession session) {
        session.setStartTime(params.estimatedStart);
        return localizationFactory.createResponse("command.set-start.response", FormatUtils.formatShortDateTime(session.getStartTime()));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionIdentityParameters {
        private ZonedDateTime estimatedStart;
    }
}
