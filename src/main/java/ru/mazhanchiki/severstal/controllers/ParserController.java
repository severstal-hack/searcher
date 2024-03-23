package ru.mazhanchiki.severstal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mazhanchiki.severstal.dtos.TenderListDto;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.services.ParserService;


@RestController
@Slf4j
public class ParserController {

    ParserService service = new ParserService();

    @GetMapping("/parse")
    public ResponseEntity<Object> parse(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "start_date", required = false) String startDate,
            @RequestParam(name = "end_date", required = false) String endDate,
            @RequestParam(name = "exclude", required = false) String[] exclude,
            @RequestParam(name = "include_archive", required = false) boolean includeArchive
    ){
        log.info("/parse with query: query={}, start_date={}, end_date={}, exclude={}, include_archive={}", query, startDate, endDate, exclude, includeArchive);

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

        if (includeArchive) {
            filter.setIncludeArchive(true);
        }

        System.out.println(filter);

        var parsed = service.parse(filter);

        if (parsed == null) {
            log.warn("tenders is null");
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new TenderListDto(parsed));
    }
}
