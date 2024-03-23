package ru.mazhanchiki.severstal.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ParserController {


    @GetMapping("/parse")
    public String parse(){



        return "Hello";
    }


}
