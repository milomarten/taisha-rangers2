package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.OutOfOfficeManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import discord4j.common.util.Snowflake;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;

@Qualifier("ooo")
public class OOOCommand extends CommandSpec<OOOCommand.Parameters> {
    private final OutOfOfficeManager manager;

    public OOOCommand(OutOfOfficeManager manager) {
        super("ooo", "Mark yourself as unable to attend session for a certain timeframe");
        this.manager = manager;
        setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withParameterField(PojoParameterParser.userId(), Parameters::setId)
                .withParameterField(
                        "start-date",
                        "The start date of your vacation",
                        StringParameter.REQUIRED
                                .map(DateUtil::parseCasualDate),
                        Parameters::setStart
                )
                .withParameterField(
                        "end-date",
                        "The end date of your vacation, if different than start",
                        StringParameter.DEFAULT_EMPTY_STRING
                                .map(DateUtil::parseCasualDate),
                        Parameters::setEnd
                )
        );
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        LocalDate start = params.start;
        LocalDate end = params.end;

        if (end == null) {
            end = params.start;
        } else if (end.isBefore(start)) {
            start = params.end;
            end = params.start;
        }

        if (end.isBefore(LocalDate.now())) {
            return CommandResponse.reply("Your vacation time is in the past.", true);
        }

        this.manager.addOutDate(
                params.id,
                start,
                end
        );

        String whenVacationIs;
        if (start.equals(end)) {
            whenVacationIs = DateUtil.getPrettyDate(start);
        } else {
            whenVacationIs = DateUtil.getPrettyDate(start) + " to " + DateUtil.getPrettyDate(end);
        }

        return CommandResponse.reply(
                String.format("Scheduled your vacation for %s! Have fun!", whenVacationIs),
                false);
    }

    @Data
    public static class Parameters {
        private Snowflake id;
        private LocalDate start;
        private LocalDate end;
    }
}
