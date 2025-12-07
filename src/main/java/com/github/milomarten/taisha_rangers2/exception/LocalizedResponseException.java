package com.github.milomarten.taisha_rangers2.exception;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;

public interface LocalizedResponseException {
    CommandResponse getLocalizedMessage(LocalizationFactory factory);
}
