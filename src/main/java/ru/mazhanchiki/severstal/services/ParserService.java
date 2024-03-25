package ru.mazhanchiki.severstal.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.parsers.Parser;
import ru.mazhanchiki.severstal.parsers.TatneftParser;
import ru.mazhanchiki.severstal.parsers.TenderProParser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service("parserService")
public class ParserService {

    public List<Tender> parse(Filter filter) {
        List<Parser> parsers = new ArrayList<>();
        parsers.add(new TatneftParser());
        parsers.add(new TenderProParser());

        ExecutorService executorService = Executors.newFixedThreadPool(parsers.size());
        List<Future<List<Tender>>> futures = new ArrayList<>();

        for (Parser parser : parsers) {
            futures.add(executorService.submit(() -> {
                try {
                    return parser.parse(filter);
                } catch (Exception e) {
                    log.error("[{}] Failed to parse () - {}", e.getClass().getName(), e.getStackTrace());
                    return null;
                }
            }));
        }

        List<Tender> tenders = new ArrayList<>();
        for (Future<List<Tender>> future : futures) {
            try {
                var result = future.get();
                if (result!= null) {
                    tenders.addAll(result);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(Arrays.toString(e.getStackTrace()));
            }
        }

        executorService.shutdown();

//        List<Tender> tenders = new ArrayList<>();
//        for (Parser parser : parsers) {
//            var result = parser.parse(filter);
//            if (result != null) {
//                tenders.addAll(result);
//            }
//        }

        log.info("Parsed {} tenders", tenders.size());

        return tenders;
    }
}
