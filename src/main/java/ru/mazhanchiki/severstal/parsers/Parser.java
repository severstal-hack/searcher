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
    protected int page;
    protected Filter filter;


    public Parser(Filter filter) {
        this.tenders = new ArrayList<>();
        this.filter = filter;
        this.page = 0;
//        this.proxy = ProxyManager.INSTANCE.getNext();
    }


    public abstract List<Tender> parse();
}
