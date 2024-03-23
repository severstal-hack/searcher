package ru.mazhanchiki.severstal.parsers;

import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.util.List;

public class TenderProParser extends Parser{

    public TenderProParser() {
        this.URL =  "https://www.tender.pro/api/landings/etp";
    }

    private void parsePage(int page) {
        Document doc = this.parseDocument(30);
        System.out.println(doc.body());



    }

    @Override
    public List<Tender> parse() {

        parsePage(1);

        return this.tenders;
    }
}
