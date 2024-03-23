package ru.mazhanchiki.severstal.services;

import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.parsers.TenderProParser;

import java.util.List;

public class ParserService {
    public List<Tender> parse() {
        TenderProParser parser = new TenderProParser();

        return parser.parse();
    }
}
