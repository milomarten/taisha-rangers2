package com.github.milomarten.taisha_rangers2.bot.ooo;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.OneParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.OutOfOfficeManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component("check-ooo")
public class CheckOOOCommand extends LocalizedCommandSpec<LocalDate> {
    private final OutOfOfficeManager manager;

    public CheckOOOCommand(OutOfOfficeManager manager) {
        super("check-ooo");
        this.manager = manager;
        setParameterParser(new OneParameterParser<>(
                        "date",
                        "date",
                        StringParameter.REQUIRED
                                .map(DateUtil::parseCasualDate)
                )
        );
    }

    @Override
    public CommandResponse doAction(LocalDate params) {
        var who = this.manager.whoIsOutOn(params);
        var ats = who.stream()
        .map(FormatUtils::pingUser)
        .collect(Collectors.joining(" "));

        return localizationFactory.createResponse("command.check-ooo.response", who.size(), ats)
                .ephemeral(true);
    }
}
