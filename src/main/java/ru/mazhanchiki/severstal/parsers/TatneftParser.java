package ru.mazhanchiki.severstal.parsers;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ViewportSize;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Price;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.utils.Utils;

import java.nio.file.Paths;
import java.util.List;

@Slf4j(topic = "TatneftParser")
public class TatneftParser extends Parser {

    private final Playwright playwright;
    private Page page;

    public TatneftParser() {
        super();
        this.URL = "https://etp.tatneft.ru/pls/tzp";
        log.info("Creating playwright instance");
        this.playwright = Playwright.create();
    }

    private void goToNextPage() {
        this.pageNumber++;
        var l = page.getByLabel(">");
        if(!l.isVisible()) {
            log.info("No more pages");
            page = null;
            return;
        }

        l.click();
        log.info("Loading #{} page", this.pageNumber);

        page.waitForResponse("https://etp.tatneft.ru/pls/tzp/wwv_flow.show", () -> {
            log.info("Page#{} loaded", this.pageNumber);
        });

    }

    private void parsePage() {
        log.info("Parsing page #{}", this.pageNumber);
        var tableContainer = page.locator(".a-IRR-tableContainer").first();


        var tableRows = tableContainer.locator("tr").all();
        for (var row : tableRows) {

            var cells = row.locator("td").all();

            var tender = new Tender();

            if (cells.isEmpty()) {
                continue;
            }

            tender.setId(cells.get(1).innerText());

            var title = cells.get(2);
            if (title.locator("a").isVisible()) {
                tender.setLink(
                        String.format("%s/%s",
                                this.URL,
                                title.locator("a").first().getAttribute("href")
                        )
                );
            }
            tender.setName(title.innerText());

            var pubDate = cells.get(7).innerText();
            tender.setPublishDate(Utils.getTimestamp(pubDate, "dd.MM.yyyy HH:mm"));

            var startDate = cells.get(8).innerText();
            if (filter.getStartDate() != null) {
                var filterStartTimestamp = Utils.getTimestamp(filter.getStartDate(), "dd.MM.yyyy");
                var timestamp = Utils.getTimestamp(startDate, "dd.MM.yyyy HH:mm");

                if (timestamp == null) {
                    log.info("Tender with id={} skipped (missing start date)", tender.getId());
                    continue;
                }

                if (filterStartTimestamp > timestamp) {
                    log.info("Tender with id={} skipped (start date)", tender.getId());
                    continue;
                }

                tender.setStartDate(timestamp);
            }

            var endDate = cells.get(9).innerText();
            var timestamp = Utils.getTimestamp(endDate, "dd.MM.yyyy HH:mm");
            tender.setDueDate(timestamp);

            var status = cells.get(3).innerText();
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
            tender.setCompany(cells.get(4).innerText());

            var price = cells.get(5).innerText()
                    .replace(",", ".")
                    .replace(" ", "");

            if (!price.isEmpty()) {
                tender.setPrice(new Price(
                        Double.parseDouble(price),
                        cells.get(6).innerText()
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
            page = browser.newPage();
            log.info("starts loading");
            try {
                page.navigate(String.format("%s/f?p=220:562:2679716050322::::P562_OPEN_MODE,GLB_NAV_ROOT_ID,GLB_NAV_ID:,12920020,12920020", this.URL));
                page.waitForLoadState();
//                page.waitForResponse("https://etp.tatneft.ru/pls/tzp/wwv_flow.show", () -> {
//                    log.info("Page#{} loaded", this.page);
//                });
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("tatneft-initial.png")));
            } catch (Exception e) {
                log.error("Error loading page", e);
                return null;
            }

            log.info("loaded");

            applyFilter(page);

            do {
                page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(String.format("tatneft-%s.png", this.pageNumber))));
                parsePage();
                goToNextPage();
            } while(null != page);
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
