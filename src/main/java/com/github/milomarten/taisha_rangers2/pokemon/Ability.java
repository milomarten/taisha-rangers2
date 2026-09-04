package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.Data;

@Data
public class Ability {
    @CsvBindByName
    private int id;
    @CsvBindByName
    private String identifier;
    @CsvBindByName(column = "generation_id")
    private int generationId;
    @CsvCustomBindByName(column = "is_main_series", converter = NumberBoolean.class)
    private boolean isMainSeries;
}
