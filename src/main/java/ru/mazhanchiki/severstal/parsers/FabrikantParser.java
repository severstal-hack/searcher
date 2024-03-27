package ru.mazhanchiki.severstal.parsers;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Price;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "Fabrikant Parser")
public class FabrikantParser extends Parser {

    public FabrikantParser() {
        super();
        log.info("start fabrikant parser");
        this.URL = "https://etp-ets.ru";
    }

    @Override
    public List<Tender> parse(Filter filter) {
        super.parse(filter);

        var tenders = new ArrayList<Tender>();


        var query = parseQuery(filter);

        var url44 = String.format("%s/44/catalog/procedure%s", URL, query);
        log.info(url44);

        var url223 = String.format("%s/223/catalog/procedure%s", URL, query);
        log.info(url223);

        var pagesCount44 = Math.min(10, getPagesCount(url44));
        var pagesCount223 = Math.min(10, getPagesCount(url223));

        log.info(String.format("Available pages of 44: %d", pagesCount44));
        log.info(String.format("Available pages of 223: %d", pagesCount223));

        var worker44 = new FabrikantParserWorker(url44);
        for (var i = 1; i <= pagesCount44; i++) {
            log.info(String.format("Parsing page %d/%d of 44-FZ", i, pagesCount44));
            tenders.addAll(worker44.parse(i));
        }

//        var worker223 = new FabrikantParserWorker(browser, url223);
//        for (var i = 0; i < pagesCount223; i++) {
//            worker223.parse(i);
//        }

        return tenders;
    }

    private String parseQuery(Filter filter) {
        var sb = new StringBuilder();

        if (!filter.isIncludeArchive()) {
            sb.append("/published?");
        } else {
            sb.append("?");
        }

        if (filter.getQuery() != null) {
            sb.append("keywords=").append(filter.getQuery()).append("&");
        }

        if (filter.getStartDate() != null) {
            sb.append("requestEndGiveDateTime-from=").append(filter.getStartDate()).append("&");
        }

        if (filter.getEndDate() != null) {
            sb.append("requestEndGiveDateTime-to=").append(filter.getEndDate()).append("&");
        }

        sb.append("limit=100");

        return sb.toString();
    }
    private int getPagesCount(String url) {

        Document doc = null;

        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0").get();
        } catch (IOException e) {
           log.error(e.getMessage());
           return 0;
        }

        var pagesContainer = doc.select(".pageLimiter-flex > .input-group-addon").first();

        if (pagesContainer == null) {
            log.error(String.format("Can't get pages count for %s", url));
            return 1;
        }

        var pages = pagesContainer.text().split(": ")[1];
        return Integer.parseInt(pages) / 100;
    }
}

@Slf4j(topic = "Fabrikant Parser Worker")
final class FabrikantParserWorker {
   private final String url;

    FabrikantParserWorker(String url) {
        this.url = url;
    }

    public List<Tender> parse(int pageNumber) {
        var tenders = new ArrayList<Tender>();

        var endUrl = String.format("%s&page=%d", url, pageNumber);

        Document doc = null;
        try {
            doc = Jsoup.connect(endUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0")
                    .get();
        } catch (IOException e) {
            log.error(e.getMessage());
            return tenders;
        }

        //        page.navigate(String.format("%s&page=%d", url, pageNumber));

        var table = doc.select("table.table").first();

        if (table == null) {
            log.error(String.format("Table not found: %s", endUrl));
            return tenders;
        }

        var tbody = table.select("tbody").first();

        if (tbody == null) {
            log.error(String.format("Tbody not found: %s", endUrl));
            return tenders;
        }

        var rows = tbody.select("tr");
        for (var row : rows) {
            var cells = row.select("td");
            // 13 cells

            var tender = new Tender();

            var id = cells.get(1).text();
            var titleCell = cells.get(2).select("a");
            var title = titleCell.text();
            var link = titleCell.attr("href");

            var priceText = cells.get(3).text();
            var priceParts = priceText.split("\\.");
            var priceToConvert = priceParts[0].replace(" ", "") + "." + priceParts[1].substring(0, 2);
            var priceValue = Double.parseDouble(priceToConvert);
            var currency = priceParts[1].split(" ")[1];
            var price = new Price(priceValue, currency);

            var buyer = cells.get(5).text();

            var pubDateText = cells.get(6).text();
            var pubDate = Utils.getTimestamp(pubDateText, "dd.MM.yyyy HH:mm:ss");

            var dueDateText = cells.get(7).text();
            var dueDate = Utils.getTimestamp(dueDateText, "dd.MM.yyyy HH:mm:ss");

            var status = cells.get(10).text();

            switch (status) {
                case "Контракт заключен":
                    tender.setStatus(TenderStatus.CLOSED);
                    break;
                case "Отменена":
                    tender.setStatus(TenderStatus.CANCELLED);
                    break;
                case "Не состоялась":
                    tender.setStatus(TenderStatus.OPEN);
                    break;
            }

            tender.setId(id);
            tender.setName(title);
            tender.setLink(link);
            tender.setPrice(price);
            tender.setCompany(buyer);
            tender.setPublishDate(pubDate);
            tender.setDueDate(dueDate);

            tender.setDomain("etp-ets.ru");

            tenders.add(tender);
        }

        return tenders;
    }
}

