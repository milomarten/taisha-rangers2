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

    @Value("${reminder.not-all-players-present}")
    private Duration notAllPlayersPresentOffset;

    @Value("${reminder.time-for-session}")
    private Duration timeForSessionOffset;

    @Value("${reminder.done-reminder}")
    private Duration doneReminderOffset;

    public ZonedDateTime getAnnouncementTime(NextSession session) {
        return session.getProposedStartTime().plus(reminderFarOffSessionOffset);
    }

    public boolean isFarOffSession(NextSession session) {
        var now = ZonedDateTime.now();
        var announcementTime = getAnnouncementTime(session);

        return now.isBefore(announcementTime);
    }

    public ZonedDateTime getGentleReminderTime(NextSession session) {
        return session.getProposedStartTime()
                .plus(notAllPlayersPresentOffset);
    }

    public ZonedDateTime getSessionStartReminderTime(NextSession session) {
        return session.getStartTime().plus(timeForSessionOffset);
    }

    public ZonedDateTime getDoneReminderTime(NextSession session) {
        return session.getStartTime().plus(doneReminderOffset);
    }
}
