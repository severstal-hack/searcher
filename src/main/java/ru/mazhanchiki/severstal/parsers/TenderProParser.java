package ru.mazhanchiki.severstal.parsers;

import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.util.List;

public class TenderProParser extends Parser{

    public TenderProParser() {
        this.URL =  "https://www.tender.pro/api/landings/etp";
    }

    private int getPagesCount() {
       Document doc = this.parseDocument(1, 30);
       var paginationLink = doc.select(".pagination__link_last");
       var href = paginationLink.attr("href");

       var page = href.split("page=")[1];

       return Integer.parseInt(page);
    }

    private void parsePage(int page) {
        Document doc = this.parseDocument(page, 30);

        var tenderListBlock = doc.select(".tender-list-block ").getFirst();

        var tenderListItems = tenderListBlock.select(".tender-list__item");

        for (var tenderListItem : tenderListItems) {
            var status = tenderListItem.select(".t-status").getFirst().text();
            var companyName = tenderListItem.select(".company-name").getFirst().text();
            var tenderId = tenderListItem.select(".tender-id").getFirst().text();
            var tenderName = tenderListItem.select(".tender-name").getFirst().text();

            System.out.println(status + " " + companyName + " " + tenderId + " " + tenderName);
        }

    }

    @Override
    public List<Tender> parse() {

        pageCount = getPagesCount();
        System.out.printf("Всего cтраниц %s\n", pageCount);
        for (int i = 1; i <= 10; i++) {
            parsePage(i);
        }

        parsePage(1);

        return this.tenders;
    }
}
