package ru.mazhanchiki.severstal.parsers;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
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

    private final Playwright playwright;

    public FabrikantParser() {
        super();
        log.info("start fabrikant parser");
        this.URL = "https://etp-ets.ru";
        playwright = Playwright.create();
    }

    @Override
    public List<Tender> parse(Filter filter) {
        super.parse(filter);

        var tenders = new ArrayList<Tender>();

        var browser = playwright.chromium().launch();

        var query = parseQuery(filter);

        var page44 = browser.newPage();
        var url44 = String.format("%s/44/catalog/procedure%s", URL, query);
        page44.navigate(url44);

        var page223 = browser.newPage();
        var url223 = String.format("%s/223/catalog/procedure%s", URL, query);
        page223.navigate(url223);

        log.info(url44);
        log.info(url223);

        var pagesCount44 = Math.min(10, getPagesCount(page44));
        var pagesCount223 = Math.min(10, getPagesCount(page223));

        log.info(String.format("Available pages of 44: %d", pagesCount44));
        log.info(String.format("Available pages of 223: %d", pagesCount223));

        var worker44 = new FabrikantParserWorker(browser, url44);
        for (var i = 1; i <= pagesCount44; i++) {
            log.info(String.format("Parsing page %d/%d of 44-FZ", i, pagesCount44));
            tenders.addAll(worker44.parse(i));
        }

//        var worker223 = new FabrikantParserWorker(browser, url223);
//        for (var i = 0; i < pagesCount223; i++) {
//            worker223.parse(i);
//        }

        browser.close();

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
    private int getPagesCount(Page page) {
        var pagesContainer = page.locator(".pageLimiter-flex > .input-group-addon").first();
        var pages = pagesContainer.innerText().split(": ")[1];
        return Integer.parseInt(pages) / 100;
    }
}

@Slf4j(topic = "Fabrikant Parser Worker")
final class FabrikantParserWorker {

   private final Browser browser;
   private final String url;

    FabrikantParserWorker(Browser browser, String url) {
        this.browser = browser;
        this.url = url;
    }

    public List<Tender> parse(int pageNumber) {
        var tenders = new ArrayList<Tender>();
        var page = browser.newPage();

//        var endUrl = String.format("%s&page=%d", url, pageNumber);
//
//        try {
//            var doc = Jsoup.connect(endUrl)
//                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0")
//                    .get();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            return tenders;
//        }

        page.navigate(String.format("%s&page=%d", url, pageNumber));

        var table = page.locator("table.table").first();
        var tbody = table.locator("tbody").first();
        var rows = tbody.locator("tr").all();

        for (var row : rows) {
            var cells = row.locator("td").all();
            // 13 cells

            var tender = new Tender();

            var id = cells.get(1).innerText();
            var titleCell = cells.get(2).locator("a");
            var title = titleCell.innerText();
            var link = titleCell.getAttribute("href");

            var priceText = cells.get(3).innerText();
            var priceParts = priceText.split("\\.");
            var priceToConvert = priceParts[0].replace(" ", "") + "." + priceParts[1].substring(0, 2);
            var priceValue = Double.parseDouble(priceToConvert);
            var currency = priceParts[1].split(" ")[1];
            var price = new Price(priceValue, currency);

            var company = cells.get(4).innerText();
            var buyer = cells.get(5).innerText();

            var pubDateText = cells.get(6).innerText();
            var pubDate = Utils.getTimestamp(pubDateText, "dd.MM.yyyy HH:mm:ss");

            var dueDateText = cells.get(7).innerText();
            var dueDate = Utils.getTimestamp(dueDateText, "dd.MM.yyyy HH:mm:ss");

            var status = cells.get(10).innerText();

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


        page.close();
        return tenders;
    }
}

