package ru.mazhanchiki.severstal.parsers.fabrikant.filter;

import ru.mazhanchiki.severstal.entities.Filter;

public class FabrikantQuery44Parser extends FabrikantQueryParser {

    public FabrikantQuery44Parser(Filter filter) {
        super(filter);
    }

    @Override
    protected String getQuery() {
        return String.format("&q=%s", filter.getQuery());
    }
}
