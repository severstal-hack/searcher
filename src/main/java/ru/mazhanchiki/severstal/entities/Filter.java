package ru.mazhanchiki.severstal.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;

@Setter
@Getter
public class Filter {

    private String query;
    private String startDate;
    private String endDate;
    private String[] excludes;
    private boolean includeArchive;

    @Override
    public String toString() {
        return "Filter{" +
                "query='" + query + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", excludes=" + Arrays.toString(excludes) +
                ", includeArchive=" + includeArchive +
                '}';
    }
}
