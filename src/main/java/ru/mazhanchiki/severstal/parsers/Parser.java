package ru.mazhanchiki.severstal.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

public abstract class Parser {
    protected String URL;
    protected List<Tender> tenders;
    protected int pageCount;
    protected Proxy proxy;

    public Parser() {
        this.tenders = new ArrayList<>();
//        this.proxy = ProxyManager.INSTANCE.getNext();
    }

    protected Document parseDocument(int page, int tries) throws RuntimeException{
        Document doc = null;
        while (tries != 0) {
            try {
                var url = String.format("%s?page=%d", URL, page);
                System.out.println("Парсинг " + url);

                doc = Jsoup.connect(url)
//                        .proxy(proxy)
                        .timeout(30 * 1000)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                        .get();

                System.out.println("Парсинг завершен (" + URL + ")");
                return doc;
            } catch (ConnectException e) {
                System.out.println("Ошибка соединения с сайтом " + e);
                tries--;
                proxy = ProxyManager.INSTANCE.getNext();
            } catch (IOException e) {
                System.out.println("Ошибка парсинга " + e);
            }
        }
        throw new RuntimeException("Ошибка парсинга");
    }

    public abstract List<Tender> parse();
}
