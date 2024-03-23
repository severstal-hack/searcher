package ru.mazhanchiki.severstal.parsers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.exception.TendersNotFoundException;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class TenderProParser extends Parser {

    public TenderProParser(Filter filter) {
        super(filter);
        this.URL =  "https://www.tender.pro/api/landings/etp";
    }

    private int getPagesCount() throws TendersNotFoundException {
       Document doc = this.parseDocument(1, null);
       var paginationLink = doc.select(".pagination__link_last");

       if (paginationLink.isEmpty()) {
           throw new TendersNotFoundException("Not found pagination", null);
       }

       var href = paginationLink.attr("href");

       var page = href.split("page=")[1];

       log.info("page count: {}", page);

       return Integer.parseInt(page);
    }

    private void parsePage(int page) {
        var query = parseQuery(filter);
        Document doc = this.parseDocument(page, query);

        var tenderListBlock = doc.select(".tender-list-block ").getFirst();

        var tenderListItems = tenderListBlock.select(".tender-list__item");

        if (tenderListItems.isEmpty()) {
            log.info("tender list is empty");
            return;
        }

        for (var tenderListItem : tenderListItems) {
            Tender tender = new Tender();

            var tenderId = tenderListItem.select(".tender-id").getFirst().text();
            var companyName = tenderListItem.select(".company-name").getFirst().text();
            var tenderNameElement = tenderListItem.select(".tender-name").getFirst();

            var status = tenderListItem.select(".t-status").getFirst().text();
            var tenderName = tenderNameElement.text();
            var tenderUrl = String.format("%s%s", this.URL, tenderNameElement.attr("href"));



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
        log.info("parsed page={}", page);
    }

    private String parseQuery(Filter filter) {
        StringBuilder builder = new StringBuilder();
        if (filter.getQuery() != null) {
            builder.append("tender_name=").append(filter.getQuery()).append("&");
        }
        if (filter.getStartDate() != null) {
            builder.append("dateb2=").append(filter.getStartDate()).append("&");
        }
        if (filter.getEndDate() != null) {
            builder.append("datee2=").append(filter.getEndDate()).append("&");
        }
        if (!filter.isIncludeArchive()) {
            builder.append("tender_state=1&");
        }

        return builder.toString();
    }

    @Override
    public List<Tender> parse() {

        try {
            pageCount = getPagesCount();
        } catch (TendersNotFoundException e) {
            log.warn("Nothing found");
            return null;
        }
        for (int i = 1; i < 3; i++) {
            parsePage(i);
        }

        return this.tenders;
    }
}
