package ru.mazhanchiki.severstal.proxy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import ru.mazhanchiki.severstal.utils.CircularQueue;

import java.io.IOException;
import java.net.*;

public class ProxyManager {
    public static ProxyManager INSTANCE = new ProxyManager();

    public CircularQueue<Proxy> proxies;

    public ProxyManager() {
        this.proxies = new CircularQueue<>();
    }

    private boolean check(Proxy proxy) {
        SocketAddress addr = new InetSocketAddress("ya.ru", 80); // Замените на любой доступный хост
        try (Socket socket = new Socket(proxy)) {
            socket.connect(addr, 5000); // Попробуйте установить соединение через прокси за 5 секунд
            System.out.println(proxy + " is available");
            socket.close();
            return true;
        } catch (IOException e) {
            System.out.println(proxy + " is not available");
        }
        return false;
    }

    public Proxy getNext() {
        for (int i = 0; i < proxies.size(); i++) {
            Proxy proxy = proxies.dequeue();
            boolean available = check(proxy);
            if (available) {
                return proxy;
            }
        }
        return null;
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
