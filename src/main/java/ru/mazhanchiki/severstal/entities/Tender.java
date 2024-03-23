package ru.mazhanchiki.severstal.entities;

import ru.mazhanchiki.severstal.enums.TenderStatus;

public class Tender{
    private String id;
    private String name;
    private TenderStatus status;
    private String company;
    private String Link;
    private Long dueDate;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TenderStatus getStatus() {
        return status;
    }

    public void setStatus(TenderStatus status) {
        this.status = status;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }
}
