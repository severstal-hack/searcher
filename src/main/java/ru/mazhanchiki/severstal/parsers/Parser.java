package ru.mazhanchiki.severstal.parsers;

import lombok.extern.slf4j.Slf4j;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Parser {
    protected String URL;
    protected List<Tender> tenders;
    protected int pageNumber;
    protected Filter filter;

    public Parser() {
        this.tenders = new ArrayList<>();
        this.pageNumber = 0;
    }

    public List<Tender> start(Filter filter) {
        this.filter = filter;
        this.pageNumber = 0;

        var tenders = this.parse();

        // капец
        if (filter.getExcludes() != null) {
            return tenders.stream().filter(tender -> {
                for(String exclude : filter.getExcludes()) {
                    if (tender.getName().toLowerCase().contains(exclude.toLowerCase())) {
                        return false;
                    }
                }
                return true;
            }).toList();
        }

        return tenders;
    }

    public abstract List<Tender> parse();
}