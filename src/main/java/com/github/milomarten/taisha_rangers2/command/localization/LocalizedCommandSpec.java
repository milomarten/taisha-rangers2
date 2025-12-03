package com.github.milomarten.taisha_rangers2.command.localization;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class LocalizedCommandSpec<PARAM> extends CommandSpec<PARAM> {
    protected LocalizationFactory localizationFactory;

    public LocalizedCommandSpec(String name, String description) {
        super(name, description);
    }

    @Autowired
    public void setLocalizationFactory(LocalizationFactory localizationFactory) {
        this.localizationFactory = localizationFactory;
        this.setLocalizer(localizationFactory);
    }
}
