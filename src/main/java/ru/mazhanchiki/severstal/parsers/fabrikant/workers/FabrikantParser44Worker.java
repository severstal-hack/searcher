package ru.mazhanchiki.severstal.parsers.fabrikant.workers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.enums.TenderStatus;
import ru.mazhanchiki.severstal.parsers.fabrikant.filter.FabrikantQuery44Parser;
import ru.mazhanchiki.severstal.utils.Utils;

@Slf4j(topic = "Fabrikant Parser Worker")
public final class FabrikantParser44Worker extends FabrikantParserWorker {

    public FabrikantParser44Worker(Filter filter) {
        super();
        this.url += "/44/catalog/procedure";
        this.queryParser = new FabrikantQuery44Parser(filter);
    }

    @Override
    public Tender parseRow(Element row) {
        var tender = new Tender();

        var cells = row.select("td");

        var id = cells.get(1).text();
        var titleCell = cells.get(2).select("a");
        var title = titleCell.text();
        var link = titleCell.attr("href");

        var priceText = cells.get(3).text();
        var price = this.parsePrice(priceText);

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

        tender.setType("44-ФЗ");

        return tender;
    }
}
