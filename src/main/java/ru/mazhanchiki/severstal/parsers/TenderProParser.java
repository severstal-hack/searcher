package ru.mazhanchiki.severstal.parsers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.exception.OutOfProxyException;
import ru.mazhanchiki.severstal.exception.TendersNotFoundException;
import ru.mazhanchiki.severstal.exception.TimedOutException;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j(topic = "Tender Pro Worker")
class TenderProWorker {
    private final String URL =  "https://www.tender.pro/api/landings/etp";

    public List<Tender> parse(int page, String query) throws OutOfProxyException, TimedOutException {
        List<Tender> tenders = new ArrayList<>();
        var url = String.format("%s?page=%d&%s", URL, page, query);
//        var proxy = ProxyManager.INSTANCE.getNext();

        int retries = 5;
        Document doc = null;
        log.info("Connecting to page#{}: {}", page, url);
        while(doc == null && retries > 0) {
            try {
                doc = Jsoup.connect(url)
//                        .proxy(proxy)
                        .timeout(30 * 1000)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                        .get();


            } catch (IOException e) {
                log.warn("Retry to parse page#{} due to {} (retries left: {})", page, e.getMessage(), retries);
                retries--;
//                proxy = ProxyManager.INSTANCE.getNext();
            }
        }

        if (doc == null) {
            throw new TimedOutException("Timed out at " + url);
        }

        log.info("Parsing page#{}: {}", page, url);

        var tenderListBlock = doc.select(".tender-list-block ").getFirst();

        var tenderListItems = tenderListBlock.select(".tender-list__item");

        if (tenderListItems.isEmpty()) {
            log.info("tender list is empty");
            return tenders;
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

    public TenderProParser() {
        this.URL =  "https://www.tender.pro/api/landings/etp";
    }

    private int getPagesCount(String query) throws TendersNotFoundException, OutOfProxyException {
        String url = String.format("%s?%s", URL, query);
//        var proxy = ProxyManager.INSTANCE.getNext();
        Document doc = null;
        log.info("Connecting to {}", url);
        while(doc == null) {
            try {
                doc = Jsoup.connect(url)
//                        .proxy(proxy)
                        .timeout(60 * 1000)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                        .get();

            } catch (IOException e) {
                log.warn("Retry to get page count due to {}", e.getMessage());
//                proxy = ProxyManager.INSTANCE.getNext();
            }
        }

        log.info("Getting pages count");

        var paginationLink = doc.select(".pagination__link_last");

       if (paginationLink.isEmpty()) {
           throw new TendersNotFoundException("Not found pagination", url);
       }

       var href = paginationLink.attr("href");
       var splitPage = href.split("page=");
       if (splitPage.length != 2) {
           return 1;
       }

       var count = Integer.parseInt(splitPage[1]);
       return count;
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
    public List<Tender> parse(Filter filter) {
        super.parse(filter);

        var query = this.parseQuery(filter);
        var count = 0;
        try {
            count = this.getPagesCount(query);
        } catch (TendersNotFoundException |  OutOfProxyException ex) {
            throw new RuntimeException(ex);
        }

        log.info("Parse session started with {} pages", count);

        int workersCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(workersCount);
        List<Future<List<Tender>>> futures = new ArrayList<>();
        for (int i = 0; i < count; i += workersCount) {
            for (int j = 0; j < Math.min(workersCount, count - i); j++) {
                var worker = new TenderProWorker();
                int page = i + j;
                futures.add(executor.submit(() -> {
                    try {
                        return worker.parse(page, query);
                    } catch (TimedOutException | OutOfProxyException ex) {
                        log.warn(ex.getMessage());
                    }
                    return null;
                }));
            }

            for (Future<List<Tender>> future : futures) {
                try {
                    var result = future.get();
                    if (result!= null) {
                        this.tenders.addAll(result);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        executor.shutdown();
        log.info("Parse session ended with {} tenders", this.tenders.size());
        return this.tenders;
    }
}
