package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.dice.DiceExpressionParser;
import com.github.milomarten.dice.DiceResultFormatter;
import com.github.milomarten.dice.term.DiceMathTerm;
import com.github.milomarten.evaluator.EvaluatorOptions;
import com.github.milomarten.evaluator.ExpressionSyntaxError;
import com.github.milomarten.formatting.ExpressionFormatter;
import com.github.milomarten.formatting.LineByLineFormatter;
import com.github.milomarten.parsing.StringExpressionEvaluator;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.EnumParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.dice.DiceService;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.util.FindPlayerService;
import com.github.milomarten.taisha_rangers2.util.ResponseMode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("roll")
public class RollCommand extends LocalizedCommandSpec<RollCommand.Parameters> {
    private final FindPlayerService findPlayerService;
    private final DiceService diceService;

    private final StringExpressionEvaluator<DiceMathTerm> EVALUATOR =
            new StringExpressionEvaluator<>(new DiceExpressionParser());
    private final ExpressionFormatter<DiceMathTerm> FORMATTER =
            new DiceResultFormatter();

    public RollCommand(FindPlayerService findPlayerService, DiceService diceService) {
        super("roll");
        this.findPlayerService = findPlayerService;
        this.diceService = diceService;

        setParameterParser(Parameters.parser(Parameters::new)
                .withParameterField("expression", StringParameter.REQUIRED, Parameters::setExpression)
                .withParameterField("comment", StringParameter.DEFAULT_EMPTY_STRING, Parameters::setComment)
                .withParameterField("mode", new EnumParameter<>(ResponseMode.class, ResponseMode.PUBLIC), Parameters::setMode)
        );
    }


    @Override
    protected CommandResponse doAction(Parameters params) {
        var dice = diceService.getDice(params.getUserId().asString());
        var roller = findPlayerService.findPlayerCharacterName(params.getUserId(), params.getChannelId());

        if (roller.isEmpty() && params.mode == ResponseMode.EPHEMERAL_AND_STORYTELLER) {
            return localizationFactory.createResponse("errors.party.no-storyteller")
                    .ephemeral(true);
        }

        var rollerName = roller
                .map(p -> p.identity().getName())
                .orElseGet(params::getUsername);

        var ctx = EvaluatorOptions.builder()
                .randomSource(dice.getUrp())
                .build();

        try {
            var roll = EVALUATOR.evaluate(params.expression, ctx);
            var formatted = LineByLineFormatter.format(roll, FORMATTER);

            return localizationFactory.createComplexResponse((ms, locale) -> {
                var firstLine = ms.getMessage("command.roll.response", new Object[]{rollerName}, locale);
                if (!params.comment.isEmpty()) {
                    firstLine += " [" + params.comment + "]";
                }

                String work;
                if (roll.value().isNumber()) {
                    var finalResult = Integer.toString(roll.value().asInteger(ctx));
                    if (Objects.equals(finalResult, formatted.getLast())) {
                        formatted.removeLast();
                    }
                    work = String.join("\n⤷ ", formatted) + "\n **= " + finalResult + "**";
                } else {
                    work = String.join("\n⤷ ", formatted);
                }

                return params.mode
                        .respond(firstLine + "\n" + work, roller.orElse(null));
            });

        } catch (ExpressionSyntaxError ex) {
            return CommandResponse.reply(ex.getMessage(), true);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionIdentityParameters {
        private String expression;
        private String comment;
        private ResponseMode mode;
    }
}
