package ru.mazhanchiki.severstal.parsers;

import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "Fabrikant Parser")
public class FabrikantParser extends Parser {

    private final Playwright playwright;

    public FabrikantParser() {
        super();
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
        page44.navigate(String.format("%s/44/catalog/procedure%s", URL, query));

        var page223 = browser.newPage();
        page223.navigate(String.format("%s/223/catalog/procedure%s", URL, query));

        log.info(String.format("%s/44/catalog/procedure%s", URL, query));
        log.info(String.format("%s/223/catalog/procedure%s", URL, query));

        browser.close();

        return tenders;
    }

    private String parseQuery(Filter filter) {
        var sb = new StringBuilder();

        if (filter.isIncludeArchive()) {
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
}

final class FabrikantParserWorker {
    public List<Tender> parse(String url) throws IOException {
        var tenders = new ArrayList<Tender>();

        var doc = Jsoup.connect(url)
//                        .proxy(proxy)
                .timeout(30 * 1000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                .get();

        return tenders;
    }
}
