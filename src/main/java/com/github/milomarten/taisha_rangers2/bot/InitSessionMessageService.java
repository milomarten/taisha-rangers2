package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.response.ReplyResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.rest.util.AllowedMentions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InitSessionMessageService {
    private final LocalizationFactory localizationFactory;

    public LocalizationFactory.LocalizedDynamicReplyResponse createInitResponse(NextSession session, ZonedDateTime proposedStart) {
        var pingText = session.getPing() == null ? "everyone" : FormatUtils.pingRole(session.getPing());
        return localizationFactory.createComplexResponse((ms, locale) -> {
            var successMsg = ms.getMessage("command.init.response.success",
                    new Object[]{pingText, FormatUtils.formatShortDateTime(proposedStart)}, locale);
            return new ReplyResponse(successMsg)
                    .ephemeral(false)
                    .allowedMentions(AllowedMentions.builder().allowRole(session.getPing()).build())
                    .component(createYesNoButtons(ms, locale));
        });
    }

    private ActionRow createYesNoButtons(MessageSource ms, Locale locale) {
        return ActionRow.of(
                Button.success("yes", ms.getMessage("command.init.buttons.yes", null, locale)),
                Button.danger("no", ms.getMessage("command.init.buttons.no", null, locale)),
                Button.secondary("maybe-PT24H", ms.getMessage("command.init.buttons.maybeTomorrow", null, locale))
        );
    }
}
