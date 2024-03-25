package ru.mazhanchiki.severstal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mazhanchiki.severstal.dtos.MessageDto;
import ru.mazhanchiki.severstal.dtos.TenderListDto;
import ru.mazhanchiki.severstal.dtos.grpc.TenderDto;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.exception.ServiceUnavailableException;
import ru.mazhanchiki.severstal.services.DataService;
import ru.mazhanchiki.severstal.services.ParserService;

import java.util.List;


@RestController
@Slf4j
public class ParserController {

    private final ParserService service;
    public final DataService dataService;

    @Autowired
    public ParserController(ParserService service, DataService dataService) {
        this.service = service;
        this.dataService = dataService;
    }

    @GetMapping("/parse")
    public ResponseEntity<Object> parse(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "start_date", required = false) String startDate,
            @RequestParam(name = "end_date", required = false) String endDate,
            @RequestParam(name = "exclude", required = false) String[] exclude,
            @RequestParam(name = "include_archive", required = false) boolean includeArchive
    ){
        log.info("/parse with query: query={}, start_date={}, end_date={}, exclude={}, include_archive={}", query, startDate, endDate, exclude, includeArchive);

//        if (!dataService.healthCheck()) {
//            log.warn("/parse 503 Service Unavailable");
//            return ResponseEntity.status(503).build();
//        }

        Filter filter = new Filter();

        if(query != null){
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

        Thread parseThread = new Thread(() -> {
            List<Tender> parsed = service.parse(filter);
            try {
                dataService.AddLinks(parsed);
            } catch (Exception e) {
                log.error("cannot add links - {}", e.getMessage());
            }
        });
        parseThread.start();

        log.info("/parse 200 OK");
        return ResponseEntity.ok().body(new MessageDto("process started"));
    }
}
