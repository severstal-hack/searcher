package ru.mazhanchiki.severstal.parsers.fabrikant;

import lombok.extern.slf4j.Slf4j;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.parsers.Parser;
import ru.mazhanchiki.severstal.parsers.fabrikant.workers.FabrikantParser223Worker;
import ru.mazhanchiki.severstal.parsers.fabrikant.workers.FabrikantParser44Worker;

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
    public List<Tender> parse() {
        var tenders = new ArrayList<Tender>();

        var worker44 = new FabrikantParser44Worker(filter);
        tenders.addAll(worker44.start());

        var worker223 = new FabrikantParser223Worker(filter);
        tenders.addAll(worker223.start());

        return tenders;
    }

}



