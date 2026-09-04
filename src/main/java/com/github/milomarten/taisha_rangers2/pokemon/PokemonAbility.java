package com.github.milomarten.taisha_rangers2.pokemon;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.Data;

@Data
public class PokemonAbility {
    @CsvBindByName(column = "pokemon_id")
    private int pokemonId;
    @CsvBindByName(column = "ability_id")
    private int abilityId;
    @CsvCustomBindByName(column = "is_hidden", converter = NumberBoolean.class)
    private boolean hidden;
    @CsvBindByName
    private int slot;
}
