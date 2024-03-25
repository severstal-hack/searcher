package ru.mazhanchiki.severstal.parsers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.exception.TendersNotFoundException;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j(topic = "Tender Pro Worker")
class TenderProWorker {
    private final String URL =  "https://www.tender.pro/api/landings/etp";

    public List<Tender> parse(int page, String query) {
        List<Tender> tenders = new ArrayList<>();

        Document doc = null;
        try {
            var url = String.format("%s?page=%d&%s", URL, page, query);
            log.info("Parsing page#{}: {}", page, url);

            doc = Jsoup.connect(url)
//                        .proxy(proxy)
                    .timeout(30 * 1000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                    .get();

        } catch (ConnectException e) {
            System.out.println("Ошибка соединения с сайтом " + e);
            return tenders;
        } catch (IOException e) {
            System.out.println("Ошибка парсинга " + e);
            return tenders;
        }

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

            tender.setDomain("tender.pro");

            tenders.add(tender);
        }

        return tenders;
    }
}

@Slf4j(topic = "TenderProParser")
public class TenderProParser extends Parser {
    private String query;
    private Document doc;

    public TenderProParser() {
        this.URL =  "https://www.tender.pro/api/landings/etp";
    }

    private void getPagesCount() throws TendersNotFoundException {
       var paginationLink = doc.select(".pagination__link_last");

       if (paginationLink.isEmpty()) {
           throw new TendersNotFoundException("Not found pagination", null);
       }

       var href = paginationLink.attr("href");
       var splitPage = href.split("page=");
       if (splitPage.length != 2) {
           pageCount = 1;
           return;
       }

       this.pageCount = Integer.parseInt(splitPage[1]);
       log.info("page count: {}", pageCount);
    }

    private void parsePage() {
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

            tender.setDomain("tender.pro");

            this.tenders.add(tender);
        }
    }

    private void parseQuery(Filter filter) {
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

        query = builder.toString();
    }

    protected void parseDocument() throws RuntimeException {
        try {
            var url = String.format("%s?page=%d&%s", URL, pageNumber, query);
            log.info("Parsing page#{}: {}", pageNumber++, url);

            doc = Jsoup.connect(url)
//                        .proxy(proxy)
                    .timeout(30 * 1000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                    .get();

        } catch (ConnectException e) {
            System.out.println("Ошибка соединения с сайтом " + e);
            proxy = ProxyManager.INSTANCE.getNext();
        } catch (IOException e) {
            System.out.println("Ошибка парсинга " + e);
        }
    }

    @Override
    public List<Tender> parse(Filter filter) {
        super.parse(filter);

        this.parseQuery(filter);
        this.parseDocument();

        try {
            this.getPagesCount();
        } catch (TendersNotFoundException e) {
            log.warn("Nothing found");
            return new ArrayList<>();
        }

        while (this.pageNumber < this.pageCount) {
            parsePage();
            this.parseDocument();
        }

        log.info("Parse session ended with {} tenders", this.tenders.size());
        return this.tenders;
    }
}
