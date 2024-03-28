package ru.mazhanchiki.severstal.parsers;

import ru.mazhanchiki.severstal.entities.Filter;

public abstract class QueryParser {

    protected Filter filter;

    public QueryParser(Filter filter){
        this.filter = filter;
    }

    public abstract StringBuilder parse();

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
