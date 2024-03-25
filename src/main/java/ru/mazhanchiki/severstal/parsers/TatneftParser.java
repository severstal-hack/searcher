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

import java.util.List;

class CallBack implements Runnable {
    @Override
    public void run() {}
}

@Slf4j(topic = "TatneftParser")
public class TatneftParser extends Parser {

    Playwright playwright;


    public TatneftParser() {
        this.URL = "https://etp.tatneft.ru/pls/tzp";
        log.info("Creating playwright instance");
        this.playwright = Playwright.create();
    }

    private Page goToNextPage(Page page) {
        this.page++;
        var l = page.getByLabel(">");
        if(!l.isVisible()) {
            log.info("No more pages");
            return null;
        }
        l.click();
        log.info("Loading #{} page", this.page);

        page.waitForResponse("https://etp.tatneft.ru/pls/tzp/wwv_flow.show", new CallBack());

        return page;
    }

    private void parsePage(String html) {
        log.info("Parsing page #{}", this.page);
        var document = Jsoup.parse(html);
        var tableContainer = document.select(".a-IRR-tableContainer").getFirst();


        var filterStartTimestamp = Utils.getTimestamp(filter.getStartDate(), "dd.MM.yyyy");
        var filterEndTimestamp = Utils.getTimestamp(filter.getEndDate(), "dd.MM.yyyy");
        var tableRows = tableContainer.select("tr");
        for (var row : tableRows) {
            var tableCells = row.select("td");
            var tender = new Tender();

            if (tableCells.isEmpty()) {
                continue;
            }

            tender.setId(tableCells.get(1).text());

            var pubDate = tableCells.get(7).text();
            if (pubDate != null) {
                tender.setPublishDate(Utils.getTimestamp(pubDate, "dd.MM.yyyy HH:mm"));
            }

            var startDate = tableCells.get(8).text();
            if (filterStartTimestamp != null) {
                var timestamp = Utils.getTimestamp(startDate, "dd.MM.yyyy HH:mm");
                if (timestamp == null || filterStartTimestamp > timestamp) {
                    log.info("Tender with id={} skipped (start date)", tender.getId());
                    continue;
                }

                tender.setStartDate(timestamp);
            }


            var endDate = tableCells.get(9).text();
            if (filterEndTimestamp != null) {
                var timestamp = Utils.getTimestamp(endDate, "dd.MM.yyyy HH:mm");
                if (timestamp == null || filterEndTimestamp < timestamp) {
                    log.info("Tender with id={} skipped (end date)", tender.getId());
                    continue;
                }

                tender.setDueDate(timestamp);
            }


            tender.setId(tableCells.get(1).text());
            var title = tableCells.get(2);
            if (!title.select("a").isEmpty()) {
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
                case "Торги не состоялись":
                    tender.setStatus(TenderStatus.CANCELLED);
                    break;
                case "Раунд завершен":
                    tender.setStatus(TenderStatus.CLOSED);
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


            tender.setDomain("etp.tatneft.ru");

            this.tenders.add(tender);
        }
    }

    @Override
    public List<Tender> parse(Filter filter) {
        super.parse(filter);
        log.info("launching playwright browser");
        try(Browser browser = playwright.chromium().launch()){
            Page page = browser.newPage();
            log.info("starts loading");
            page.navigate(String.format("%s/f?p=220:562:2679716050322::::P562_OPEN_MODE,GLB_NAV_ROOT_ID,GLB_NAV_ID:,12920020,12920020", this.URL));
            page.waitForLoadState();
            log.info("loaded");

            applyFilter(page);

//            do {
            for (int i = 0; i < 3; i++) {
                parsePage(page.content());
                page = goToNextPage(page);
                if(page == null) {
                    break;
                }
            }
//            } while(null != (page = goToNextPage(page)));
        };

        log.info("Parse session ended with {} tenders", this.tenders.size());
        return this.tenders;
    }

    private void applyFilter(Page page) {
        if (this.filter.isIncludeArchive()) {
            page.waitForSelector("#P562_STATE");
            page.selectOption("#P562_STATE", "ALL");
        }

        if (this.filter.getQuery() != null && !this.filter.getQuery().isEmpty()) {
            page.waitForSelector("#P562_SEARCH_FIELD");
            page.fill("#P562_SEARCH_FIELD", this.filter.getQuery());
            page.press("#P562_SEARCH_FIELD", "Enter");
            page.waitForLoadState();
        }
    }
}
