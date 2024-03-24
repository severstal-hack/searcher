package ru.mazhanchiki.severstal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.parsers.Parser;
import ru.mazhanchiki.severstal.parsers.TatneftParser;
import ru.mazhanchiki.severstal.parsers.TenderProParser;

import java.util.List;

@Slf4j
@Service("parserService")
public class ParserService {
    public List<Tender> parse(Filter filter) {
        Parser parser = new TatneftParser(filter);
        return parser.parse();
    }
}
