package ru.mazhanchiki.severstal.entities.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tender {
    private String name;
    private String link;

    public Tender(String name, String link) {
        this.name = name;
        this.link = link;
    }
}
