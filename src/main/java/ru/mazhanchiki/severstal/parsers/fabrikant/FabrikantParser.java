package ru.mazhanchiki.severstal.parsers.fabrikant;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Price;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.parsers.Parser;
import ru.mazhanchiki.severstal.parsers.fabrikant.filter.FabrikantQuery223Parser;
import ru.mazhanchiki.severstal.parsers.fabrikant.filter.FabrikantQuery44Parser;
import ru.mazhanchiki.severstal.parsers.fabrikant.filter.FabrikantQueryParser;
import ru.mazhanchiki.severstal.parsers.fabrikant.workers.FabrikantParser223Worker;
import ru.mazhanchiki.severstal.parsers.fabrikant.workers.FabrikantParser44Worker;
import ru.mazhanchiki.severstal.parsers.fabrikant.workers.FabrikantParserWorker;
import ru.mazhanchiki.severstal.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "Fabrikant Parser")
public class FabrikantParser extends Parser {

    public FabrikantParser() {
        super();
        log.info("start fabrikant parser");
        this.URL = "https://etp-ets.ru";
    }

    @Override
    public List<Tender> parse(Filter filter) {
        super.parse(filter);

        var tenders = new ArrayList<Tender>();

        var worker44 = new FabrikantParser44Worker(filter);
        tenders.addAll(worker44.start());

        var worker223 = new FabrikantParser223Worker(filter);
        tenders.addAll(worker223.start());

        return tenders;
    }

}



