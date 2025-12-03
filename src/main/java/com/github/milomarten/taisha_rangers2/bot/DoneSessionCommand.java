package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.StringJoiner;

@Component("done")
public class DoneSessionCommand extends CommandSpec<DoneSessionCommand.Parameters> {
    private final NextSessionManager manager;

    public DoneSessionCommand(NextSessionManager manager) {
        super("done", "Mark the session as done");
        this.manager = manager;

        setParameterParser(SessionIdentityParameters.parser(Parameters::new)
                .withParameterField("exp", "The amount of exp gained", IntParameter.REQUIRED, Parameters::setExp)
                .withParameterField("other", "Anything else gained", StringParameter.DEFAULT_EMPTY_STRING, Parameters::setOtherStuff)
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(DoneSessionCommand.Parameters params) {
        var worked = manager.cancelSession(params);
        if (worked) {
            var sb = new StringJoiner("\n");
            sb.add("Session is complete. Hope it was fun!");
            sb.add("Players gain: " + params.exp + " EXP!");
            if (!params.getOtherStuff().isEmpty()) {
                sb.add("Players also gain: " + params.otherStuff);
            }
            return CommandResponse.reply(sb.toString(), false);
        } else {
            return CommandResponse.reply("No session???", true);
        }
    }

    @Getter
    @Setter
    public static class Parameters extends SessionIdentityParameters {
        private int exp;
        private String otherStuff;
    }
}
