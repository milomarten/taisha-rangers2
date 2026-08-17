package com.github.milomarten.taisha_rangers2.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DateFormat {
    MDY {
        @Override public int getYearIndex() { return 2; }
        @Override public int getMonthIndex(boolean noYear) { return 0; }
        @Override public int getDayIndex(boolean noYear) { return 1; }
    },
    DMY {
        @Override public int getYearIndex() { return 2; }
        @Override public int getMonthIndex(boolean noYear) { return 1; }
        @Override public int getDayIndex(boolean noYear) { return 0; }
    },
    YMD {
        @Override public int getYearIndex() { return 0; }
        @Override public int getMonthIndex(boolean noYear) { return noYear ? 0 : 1; }
        @Override public int getDayIndex(boolean noYear) { return noYear ? 1 : 2; }
    }
    ;

    public abstract int getYearIndex();
    public abstract int getMonthIndex(boolean noYear);
    public abstract int getDayIndex(boolean noYear);
}
