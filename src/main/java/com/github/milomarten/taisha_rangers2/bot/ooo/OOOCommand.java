package com.github.milomarten.taisha_rangers2.bot.ooo;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.OutOfOfficeManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import discord4j.common.util.Snowflake;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Stream;

@Component("ooo")
public class OOOCommand extends LocalizedCommandSpec<OOOCommand.Parameters> {
    private final OutOfOfficeManager manager;

    public OOOCommand(OutOfOfficeManager manager) {
        super("ooo");
        this.manager = manager;
        setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withParameterField(PojoParameterParser.userId(), Parameters::setId)
                .withParameterField(
                        "start-date",
                        StringParameter.REQUIRED
                                .map(DateUtil::parseCasualDate),
                        Parameters::setStart
                )
                .withParameterField(
                        "end-date",
                        StringParameter.DEFAULT_EMPTY_STRING
                                .map(DateUtil::parseCasualDate),
                        Parameters::setEnd
                )
        );
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        LocalDate start;
        LocalDate end;

        if (params.end == null) {
            start = end = params.start;
        } else {
            var ordered = Stream.of(params.start, params.end).sorted().toList();
            start = ordered.getFirst();
            end = ordered.getLast();
        }

        if (end.isBefore(LocalDate.now())) {
            return localizationFactory.createResponse(
                    "command.ooo.error.vacation-in-past"
            ).ephemeral(true);
        }

        this.manager.addOutDate(
                params.id,
                start,
                end
        );

        return localizationFactory.createResponse((ms, locale) -> {
           if (start.equals(end)) {
                return ms.getMessage("command.ooo.response.one-day",
                        new Object[]{DateUtil.getPrettyDate(start, locale)}, locale);
           } else {
               return ms.getMessage("command.ooo.response.many-days",
                       new Object[]{DateUtil.getPrettyDate(start, locale), DateUtil.getPrettyDate(end, locale)},
                       locale);
           }
        });
    }

    @Data
    public static class Parameters {
        private Snowflake id;
        private LocalDate start;
        private LocalDate end;
    }
}
