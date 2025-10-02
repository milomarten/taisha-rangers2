package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.OneParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.OutOfOfficeManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
@Qualifier("check-ooo")
public class CheckOOOCommand extends CommandSpec<LocalDate> {
    private final OutOfOfficeManager manager;

    public CheckOOOCommand(OutOfOfficeManager manager) {
        super("check-ooo", "Check who is out of office one day");
        this.manager = manager;
        setParameterParser(new OneParameterParser<>(
                        "date",
                        "The date to check",
                        StringParameter.REQUIRED
                                .map(DateUtil::parseCasualDate)
                )
        );
    }

    @Override
    public CommandResponse doAction(LocalDate params) {
        var who = this.manager.whoIsOutOn(params);

        if (who.isEmpty()) {
            return CommandResponse.reply("Nobody is out that day!", true);
        } else {
            var ats = who.stream()
                    .map(FormatUtils::pingUser)
                    .collect(Collectors.joining(", "));
            return CommandResponse.reply(
                    String.format("%s are out that day", ats),
                    true
            );
        }
    }
}
