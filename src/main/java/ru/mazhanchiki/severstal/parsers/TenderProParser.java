package ru.mazhanchiki.severstal.parsers;

import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

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
            var tenderNameElement = tenderListItem.select(".tender-name").getFirst();

            var tenderName = tenderNameElement.text();
            var tenderUrl = String.format("%s%s", this.URL, tenderNameElement.attr("href"));



            Tender tender = new Tender();
            switch (status) {
                case "Открыт": tender.setStatus(TenderStatus.OPEN); break;
                case "Согласование": tender.setStatus(TenderStatus.AGREEMENT); break;
                case "Закрыт": tender.setStatus(TenderStatus.CLOSED); break;
            }

            if (tender.getStatus() == TenderStatus.OPEN) {
                var tenderDueDateElement = tenderListItem.select(".t-time").getFirst();
                var tenderDueDate = tenderDueDateElement.select("span").text();

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy в hh:mm");
                Date parsedDate = null;
                try {
                    parsedDate = dateFormat.parse(tenderDueDate);
                    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                    tender.setDueDate(timestamp.getTime());
                } catch (ParseException e) {
                    System.out.printf("Ошибка при обработке времение завершения тендера %s\n", tenderDueDate);
                }
            }

            tender.setId(tenderId);
            tender.setName(tenderName);
            tender.setCompany(companyName);
            tender.setLink(tenderUrl);

            this.tenders.add(tender);
        }

        System.out.printf("Страница %d обработана\n", page);

    }

    @Override
    public List<Tender> parse() {

        pageCount = getPagesCount();
        System.out.printf("Всего cтраниц %s\n", pageCount);
        for (int i = 1; i < 3; i++) {
            parsePage(i);
        }

        return this.tenders;
    }
}
