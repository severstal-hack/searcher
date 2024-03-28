package ru.mazhanchiki.severstal.parsers.fabrikant.workers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.parsers.fabrikant.filter.FabrikantQuery223Parser;
import ru.mazhanchiki.severstal.utils.Utils;

@Slf4j(topic = "Fabrikant Parser 223 Worker")
public class FabrikantParser223Worker extends FabrikantParserWorker {

    public FabrikantParser223Worker(Filter filter) {
        this.url += "/223/catalog/procedure";
        this.queryParser = new FabrikantQuery223Parser(filter);
    }

    @Override
    Tender parseRow(Element row) {
        var cells = row.select("td");

        var tender = new Tender();

        var tradeNameCell = cells.get(1).text();
        var tradeName = tradeNameCell.split("\\(")[0];
        var tradeId = tradeNameCell.split("\\(")[1].replace(")", "");
        var link = cells.get(1).select("a").first().attr("href");

        var priceText = cells.get(3).text();
        var price = this.parsePrice(priceText);

        var buyer = cells.get(5).text();

        var pubDateText = cells.get(6).text();
        var pubDate = Utils.getTimestamp(pubDateText, "dd.MM.yyyy HH:mm:ss");

        var dueDateText = cells.get(7).text();
        var dueDate = Utils.getTimestamp(dueDateText, "dd.MM.yyyy HH:mm:ss");

        var status = cells.get(10).text();

        switch (status) {
            case "Прием заявок":
                tender.setStatus(TenderStatus.OPEN);
                break;
            case "Ожидание торгов":
                tender.setStatus(TenderStatus.AGREEMENT);
                break;
            case "Завершенные":
                tender.setStatus(TenderStatus.CLOSED);
                break;
        }

        tender.setId(tradeId);
        tender.setName(tradeName);
        tender.setLink(link);
        tender.setPrice(price);
        tender.setCompany(buyer);
        tender.setPublishDate(pubDate);
        tender.setDueDate(dueDate);

        tender.setType("223-ФЗ");

        return tender;
    }
}
