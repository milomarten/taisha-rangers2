package com.github.milomarten.taisha_rangers2.command.localization;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public interface Localizer {
    Localizer IDENTITY = new IdentityLocalizer();

    LocalizedStrings localize(String key);
    LocalizedStrings localize(String key, String suffix);

    default Localizer withPrefix(String prefix) {
        return new BreadcrumbLocalizer(this, prefix);
    }

    class IdentityLocalizer implements Localizer {
        @Override
        public LocalizedStrings localize(String key) {
            return LocalizedStrings.of(key);
        }

        @Override
        public LocalizedStrings localize(String key, String suffix) {
            return localize(key);
        }

        @Override
        public Localizer withPrefix(String prefix) {
            return this;
        }
    }

    @RequiredArgsConstructor
    class BreadcrumbLocalizer implements Localizer {
        private final List<String> crumbs;
        private final Localizer nested;

        public BreadcrumbLocalizer(Localizer nested, String nextCrumb) {
            if (nested instanceof  BreadcrumbLocalizer bl) {
                this.crumbs = new ArrayList<>(bl.crumbs);
                this.crumbs.add(nextCrumb);
                this.nested = bl.nested;
            } else {
                this.crumbs = new ArrayList<>();
                this.crumbs.add(nextCrumb);
                this.nested = nested;
            }
        }

        @Override
        public LocalizedStrings localize(String key) {
            return nested.localize(String.join(".", crumbs) + "." + key);
        }

        @Override
        public LocalizedStrings localize(String key, String suffix) {
            return nested.localize(String.join(".", crumbs) + "." + key, suffix);
        }
    }
}
