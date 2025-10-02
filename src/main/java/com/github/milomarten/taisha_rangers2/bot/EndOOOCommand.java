package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.OutOfOfficeManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import discord4j.common.util.Snowflake;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component("end-ooo")
public class EndOOOCommand extends CommandSpec<EndOOOCommand.Parameters> {
    private final OutOfOfficeManager manager;

    public EndOOOCommand(OutOfOfficeManager manager) {
        super("end-ooo", "Cancel your previously-entered Out of Office date");
        this.manager = manager;
        setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withParameterField(PojoParameterParser.userId(), Parameters::setId)
                .withParameterField(
                        "date",
                        "The start date of the vacation to cancel",
                        StringParameter.REQUIRED
                                .map(DateUtil::parseCasualDate),
                        Parameters::setDate
                )
        );
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        var worked = this.manager.removeOutDate(
                params.id,
                params.date
        );

        if (worked) {
            return CommandResponse.reply("Cancelled your OOO.", false);
        } else {
            return CommandResponse.reply("You did not have OOO time on that day", true);
        }
    }

    @Data
    public static class Parameters {
        private Snowflake id;
        private LocalDate date;
    }
}
