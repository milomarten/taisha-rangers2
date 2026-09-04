package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.Data;

@Data
public class PokemonSpecies {
    @CsvBindByName
    private int id;
    @CsvBindByName
    private String identifier;
    @CsvBindByName(column = "generation_id")
    private int generationId;
    @CsvBindByName(column = "evolves_from_species_id")
    private Integer evolvesFromSpeciesId;
    @CsvCustomBindByName(column = "is_baby", converter = NumberBoolean.class)
    private boolean baby;
    @CsvCustomBindByName(column = "is_legendary", converter = NumberBoolean.class)
    private boolean legendary;
    @CsvCustomBindByName(column = "is_mythical", converter = NumberBoolean.class)
    private boolean mythical;
    @CsvBindByName
    private int order;
}
