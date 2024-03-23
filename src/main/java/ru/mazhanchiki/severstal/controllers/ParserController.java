package ru.mazhanchiki.severstal.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.services.ParserService;

import java.util.List;


@RestController
public class ParserController {

    ParserService service = new ParserService();

    @GetMapping("/parse")
    public ResponseEntity<List<Tender>>  parse(){
        var parsed = service.parse();
        return ResponseEntity.ok(parsed);
    }


}
