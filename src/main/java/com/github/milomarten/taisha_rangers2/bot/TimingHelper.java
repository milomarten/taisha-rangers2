package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;

@Component
public class TimingHelper {
    @Value("${reminder.far-off-session}")
    private Duration reminderFarOffSessionOffset;

    @Value("${reminder.latest-status-submit-time}")
    private Duration latestStatusSubmitOffset;

    @Value("${reminder.gentle-reminder}")
    private Duration gentleReminderOffset;

    @Value("${reminder.session-start}")
    private Duration sessionStartReminderOffset;

    @Value("${reminder.done}")
    private Duration doneReminderOffset;

    public ZonedDateTime getAnnouncementTime(NextSession session) {
        return session.getProposedStartTime().plus(reminderFarOffSessionOffset);
    }

    public boolean isFarOffSession(NextSession session) {
        var now = ZonedDateTime.now();
        var announcementTime = getAnnouncementTime(session);

        return now.isBefore(announcementTime);
    }

    public ZonedDateTime getLatestStatusSubmitTime(NextSession session) {
        return session.getProposedStartTime()
                .plus(latestStatusSubmitOffset);
    }

    public ZonedDateTime getGentleReminderTime(NextSession session) {
        return session.getProposedStartTime()
                .plus(gentleReminderOffset);
    }

    public ZonedDateTime getSessionStartReminderTime(NextSession session) {
        return session.getStartTime().plus(sessionStartReminderOffset);
    }

    public ZonedDateTime getDoneReminderTime(NextSession session) {
        return session.getStartTime().plus(doneReminderOffset);
    }
}
