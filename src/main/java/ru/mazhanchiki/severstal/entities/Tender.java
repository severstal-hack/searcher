package ru.mazhanchiki.severstal.entities;

import lombok.Getter;
import lombok.Setter;
import ru.mazhanchiki.severstal.enums.TenderStatus;

@Getter
@Setter
public class Tender {
    private String id;
    private String name;
    private TenderStatus status;
    private String company;
    private String Link;
    private Price price;
    private Long publishDate;
    private Long startDate;
    private Long dueDate;
}