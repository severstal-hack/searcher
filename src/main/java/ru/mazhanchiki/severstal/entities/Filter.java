package ru.mazhanchiki.severstal.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Setter
@Getter
public class Filter {

    private String query;
    private String startDate;
    private String endDate;
    private String[] excludes;

}
