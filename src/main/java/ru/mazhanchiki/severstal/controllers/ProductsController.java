package ru.mazhanchiki.severstal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mazhanchiki.severstal.services.DataService;

@Slf4j
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductsController {

    private final DataService dataService;

    @Autowired
    public ProductsController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/products")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Object> products() {
        return ResponseEntity.ok(dataService.getProducts());
    }


    @GetMapping("/match")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Object> products(
            @RequestParam(value = "q", required = true) String q
    ) {
        log.info(q);
        return ResponseEntity.ok(dataService.match(q));
    }



}
