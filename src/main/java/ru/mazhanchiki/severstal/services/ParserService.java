package ru.mazhanchiki.severstal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.parsers.TenderProParser;

import java.util.List;

@Slf4j
@Service("parserService")
public class ParserService {
    public List<Tender> parse(Filter filter) {
        TenderProParser parser = new TenderProParser(filter);
        return parser.parse();
    }
}
