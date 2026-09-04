package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.customconverter.ConverterLanguageToBoolean;

public class NumberBoolean<T, I> extends ConverterLanguageToBoolean<T, I> {

    public static final String TRUE = "1";
    public static final String FALSE = "0";
    public static final String[] TRUES = {TRUE};
    public static final String[] FALSES = {FALSE};

    @Override
    protected String getLocalizedTrue() {
        return TRUE;
    }

    @Override
    protected String getLocalizedFalse() {
        return FALSE;
    }

    @Override
    protected String[] getAllLocalizedTrueValues() {
        return TRUES;
    }

    @Override
    protected String[] getAllLocalizedFalseValues() {
        return FALSES;
    }
}
