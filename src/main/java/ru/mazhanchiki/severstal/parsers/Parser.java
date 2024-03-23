package ru.mazhanchiki.severstal.parsers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Filter;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Parser {
    protected String URL;
    protected List<Tender> tenders;
    protected int pageCount;
    protected Proxy proxy;
    protected String query;

    public Parser() {
        this.tenders = new ArrayList<>();
//        this.proxy = ProxyManager.INSTANCE.getNext();
    }

    protected Document parseDocument(int page) throws RuntimeException {
        Document doc = null;
        try {
            var url = String.format("%s?page=%d&%s", URL, page, query);
            log.info("Parsing: " + url);

            doc = Jsoup.connect(url)
//                        .proxy(proxy)
                    .timeout(30 * 1000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                    .get();

            return doc;
        } catch (ConnectException e) {
            System.out.println("Ошибка соединения с сайтом " + e);
            proxy = ProxyManager.INSTANCE.getNext();
        } catch (IOException e) {
            System.out.println("Ошибка парсинга " + e);
        }
        throw new RuntimeException("Ошибка парсинга");
    }

    public abstract List<Tender> parse(Filter filter);
}
