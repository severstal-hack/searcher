package ru.mazhanchiki.severstal.parsers.fabrikant.workers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import ru.mazhanchiki.severstal.entities.Price;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.parsers.fabrikant.filter.FabrikantQueryParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "Fabrikant Parser Worker")
public abstract class FabrikantParserWorker {
    protected String url;
    protected FabrikantQueryParser queryParser;

    FabrikantParserWorker() {
        this.url = "https://etp-ets.ru";
    }

    abstract Tender parseRow(Element row);

    public List<Tender> start() {
        var pageCount = getPagesCount();
        pageCount = Math.min(10, pageCount);
        log.info(String.format("Available pages: %d", pageCount));

        var tenders = new ArrayList<Tender>();
        for (int i = 1; i <= pageCount; i++) {
            tenders.addAll(this.parse(i));
        }
        return tenders;
    }


    private Document getPage() {
        return this.getPage(1);
    }
    private Document getPage(int pageNumber) {
        var query = queryParser.parse().toString();
        var endUrl = String.format("%s%s&page=%d", url, query, pageNumber);

        log.info(String.format("Parsing page#%d:%s", pageNumber, endUrl));
        Document doc = null;
        try {
            doc = Jsoup.connect(endUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0")
                    .get();

            return doc;
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    private List<Tender> parse(int pageNumber) {
        var tenders = new ArrayList<Tender>();

        var doc = this.getPage(pageNumber);
        if (doc == null) {
            return tenders;
        }

        var table = doc.select("table.table").first();
        if (table == null) {
            return tenders;
        }

        var tbody = table.select("tbody").first();
        if (tbody == null) {
            return tenders;
        }

        var rows = tbody.select("tr");
        for (var row : rows) {
            var tender = this.parseRow(row);
            tender.setDomain("etp-ets.ru");
            tenders.add(tender);
        }

        return tenders;
    }
    private int getPagesCount() {

        Document doc = this.getPage();
        if (doc == null) {
            return 0;
        }

        var pagesContainer = doc.select(".pageLimiter-flex > .input-group-addon").first();

        if (pagesContainer == null) {
            log.error(String.format("Can't get pages count for %s", url));
            return 1;
        }

        var pages = pagesContainer.text().split(": ")[1];
        var value = Double.parseDouble(pages);
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(value / 100);
    }

    protected Price parsePrice(String priceText) {
        var priceParts = priceText.split("\\.");
        var priceToConvert = priceParts[0].replace(" ", "") + "." + priceParts[1].substring(0, 2);
        var priceValue = Double.parseDouble(priceToConvert);
        var currency = priceParts[1].split(" ")[1];
        return new Price(priceValue, currency);
    }

}
