package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByNames;
import lombok.Data;

@Data
public class Name {
    @CsvBindByNames({
            @CsvBindByName(column = "ability_id", profiles = "ability"),
            @CsvBindByName(column = "type_id", profiles = "type"),
            @CsvBindByName(column = "pokemon_species_id", profiles = "pokemon_species")
    })
    private int id;
    @CsvBindByName(column = "local_language_id")
    private int languageId;
    @CsvBindByName
    private String name;
}
