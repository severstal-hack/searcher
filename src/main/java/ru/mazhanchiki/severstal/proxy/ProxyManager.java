package ru.mazhanchiki.severstal.proxy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import ru.mazhanchiki.severstal.exception.OutOfProxyException;
import ru.mazhanchiki.severstal.utils.CircularQueue;

import java.io.IOException;
import java.net.*;

@Slf4j
public class ProxyManager {
    public static ProxyManager INSTANCE = new ProxyManager();

    public CircularQueue<Proxy> proxies;

    public ProxyManager() {
        this.proxies = new CircularQueue<>();
    }

    private boolean check(Proxy proxy) {
        log.info("Checking proxy " + proxy);
        SocketAddress addr = new InetSocketAddress("ya.ru", 80); // Замените на любой доступный хост
        try (Socket socket = new Socket(proxy)) {
            socket.connect(addr, 10000);
            log.info(proxy + " is available");
            socket.close();
            return true;
        } catch (IOException e) {
            log.info(proxy + " is unavailable");
        }
        return false;
    }

    public boolean isEmpty() {
        return proxies.isEmpty();
    }

    public void clean() {
        log.info("start cleaning proxies: (count: " + proxies.size() + ")");
        for (int i = 0; i < proxies.size(); i++) {
            if (!check(proxies.peek())) {
                this.remove(proxies.peek());
            }
            proxies.dequeue();
        }

        log.info("Cleaned proxies available proxies: " + proxies.size());
    }

    private void remove(Proxy proxy) {
        proxies.remove(proxy);
        log.info("Removed proxy " + proxy + " (remaining: " + proxies.size() + ")");
    }

    public Proxy getNext() throws OutOfProxyException {

        return proxies.dequeue();

//        if (check(proxies.peek())) {
//            return proxies.dequeue();
//        }
//
//        for (int i = 0; i < proxies.size(); i++) {
//            Proxy proxy = proxies.peek();
//            boolean available = check(proxy);
//
//            if (available) {
//                return proxies.dequeue();
//            }
//
//            this.remove(proxy);
//        }
//        throw new OutOfProxyException("No proxy available");
    }

    public void fetchProxies(String url) throws IOException {

        var response = JSON.parseArray(new URL(url));

        response.forEach(o -> {
            if(o instanceof JSONObject) {
                var obj = (JSONObject) o;

                var protocol = obj.getString("protocol");
                var ip = obj.getString("ip");
                var port = obj.getIntValue("port");

                switch (protocol) {
                   case "http":
                        proxies.enqueue(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port)));
                        break;
                    case "socks4", "socks5":
                        proxies.enqueue(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ip, port)));
                        break;
                }
            }
        });
    }
}
