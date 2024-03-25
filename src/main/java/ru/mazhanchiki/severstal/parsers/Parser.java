package ru.mazhanchiki.severstal.parsers;

import lombok.extern.slf4j.Slf4j;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Parser {
    protected String URL;
    protected List<Tender> tenders;
    protected int pageCount;
    protected Proxy proxy;
    protected int pageNumber;
    protected Filter filter;


    public Parser() {
        this.tenders = new ArrayList<>();
        this.pageNumber = 0;
    }


    public List<Tender> parse(Filter filter) {
        this.filter = filter;
        this.pageNumber = 0;
        this.tenders = new ArrayList<>();
        return null;
    }
}
