package ru.mazhanchiki.severstal.parsers.fabrikant.filter;

import ru.mazhanchiki.severstal.entities.Filter;

public class FabrikantQuery223Parser extends FabrikantQueryParser {

    public FabrikantQuery223Parser(Filter filter) {
        super(filter);
    }

    @Override
    protected String getQuery() {
        return String.format("&keywords=%s", filter.getQuery());
    }
}
