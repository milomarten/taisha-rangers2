package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.Data;

@Data
public class PokemonType {
    @CsvBindByName(column = "pokemon_id")
    private int pokemonId;
    @CsvBindByName(column = "type_id")
    private int abilityId;
    @CsvBindByName
    private int slot;
}
