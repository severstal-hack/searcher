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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class TatneftParser extends Parser {

    Playwright playwright;


    public TatneftParser(Filter filter) {
        super(filter);
        this.URL = "https://etp.tatneft.ru/pls/tzp/f?p=220:562:2679716050322::::P562_OPEN_MODE,GLB_NAV_ROOT_ID,GLB_NAV_ID:,12920020,12920020";
        this.playwright = Playwright.create();
    }

    private Page goToNextPage(Page page) {
        var l = page.locator(".a-IRR-button--pagination");
        if(!l.isVisible()) {
            return null;
        }
        l.click();
        while(page.locator(".u-Processing").isVisible()) {
            log.info("Загрузка");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Загружено");
        return page;
    }

    @Override
    public List<Tender> parse() {
        log.info("launching playwright browser");
        try(Browser browser = playwright.chromium().launch()){
            Page page = browser.newPage();
            log.info("starts loading");
            page.navigate(this.URL);
            page.waitForLoadState();
            log.info("loaded");
            log.info(page.title());

//            do {

                var document = Jsoup.parse(page.content());
                var tableContainer = document.select(".a-IRR-tableContainer").getFirst();

                var tableRows = tableContainer.select("tr");
                for(var row : tableRows) {
                    var tableCells = row.select("td");
                    var tender = new Tender();

                    if (tableCells.isEmpty()) {
                        continue;
                    }

                    tender.setId(tableCells.get(1).text());
                    var title = tableCells.get(2);

                    if(!title.select("a").isEmpty()) {
                        tender.setLink(
                                String.format("%s/%s",
                                        this.URL,
                                        title.select("a").getFirst().attr("href")
                                )
                        );
                    }

                    tender.setName(title.text());
                    var status = tableCells.get(3).text();
                    switch (status) {
                        case "Опубликован":
                            tender.setStatus(TenderStatus.OPEN);
                            break;
                    }
                    tender.setCompany(tableCells.get(4).text());

                    var price = tableCells.get(5).text().replace(",", ".").replace(" ", "");
                    if (!price.isEmpty()) {
                        tender.setPrice(new Price(
                                Double.parseDouble(price),
                                tableCells.get(6).text()
                        ));
                    }

                    var pubDate = tableCells.get(7).text();
                    tender.setPublishDate(Utils.getTimestamp(pubDate, "dd.MM.yyyy HH:mm"));

                    var startDate = tableCells.get(8).text();
                    tender.setStartDate(Utils.getTimestamp(startDate, "dd.MM.yyyy HH:mm"));

                    var endDate = tableCells.get(9).text();
                    tender.setDueDate(Utils.getTimestamp(endDate, "dd.MM.yyyy HH:mm"));

                    this.tenders.add(tender);
                }
//            } while(null != (page = goToNextPage(page)));
        };

        return this.tenders;
    }
}
