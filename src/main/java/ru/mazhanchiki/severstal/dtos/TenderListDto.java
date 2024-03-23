package ru.mazhanchiki.severstal.dtos;

import ru.mazhanchiki.severstal.entities.Tender;

import java.util.List;

public class TenderListDto {
    private List<Tender> tenders;
    private int count;

    public TenderListDto(List<Tender> tenders) {
        this.tenders = tenders;
        this.count = tenders.size();
    }

    public List<Tender> getTenders() {
        return tenders;
    }

    public void setTenders(List<Tender> tenders) {
        this.tenders = tenders;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
