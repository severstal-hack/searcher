package ru.mazhanchiki.severstal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mazhanchiki.severstal.dtos.ErrorDto;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.services.ParserService;

import java.util.List;
import java.util.Objects;


@RestController
@Slf4j
public class ParserController {

    ParserService service = new ParserService();

    @GetMapping("/parse")
    public ResponseEntity<Object> parse(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "start_date", required = false) String startDate,
            @RequestParam(name = "end_date", required = false) String endDate,
            @RequestParam(name = "exclude", required = false) String[] exclude
    ){
        log.info("/parse with query: query={}, start_date={}, end_date={}, exclude={}", query, startDate, endDate, exclude);

        Filter filter = new Filter();

        if(query!= null){
            filter.setQuery(query);
        }

        if (startDate!= null && endDate!= null) {
            filter.setStartDate(startDate);
            filter.setEndDate(endDate);
        } else if (startDate!= null) {
            filter.setStartDate(startDate);
        } else if (endDate!= null) {
            filter.setEndDate(endDate);
        }

        if (exclude != null) {
            filter.setExcludes(exclude);
        }

        var parsed = service.parse(filter);

        if (parsed == null) {
            log.warn("tenders is null");
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(parsed);
    }
}
