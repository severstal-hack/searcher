package ru.mazhanchiki.severstal.parsers.fabrikant.filter;

import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.parsers.QueryParser;

public abstract class FabrikantQueryParser extends QueryParser {

    public FabrikantQueryParser(Filter filter) {
        super(filter);
    }

    protected abstract String getQuery();

    @Override
    public StringBuilder parse() {
        var sb = new StringBuilder();

        if (!filter.isIncludeArchive()) {
            sb.append("/published?");
        } else {
            sb.append("?");
        }

        sb.append("limit=100").append("&");

        if (filter.getStartDate() != null) {
            sb.append("requestEndGiveDateTime-from=").append(filter.getStartDate()).append("&");
        }

        if (filter.getEndDate() != null) {
            sb.append("requestEndGiveDateTime-to=").append(filter.getEndDate()).append("&");
        }


        if (filter.getQuery()!= null) {
            sb.append(this.getQuery());
        }

        return sb;
    }
}
