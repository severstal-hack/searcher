package ru.mazhanchiki.severstal.proxy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProxyManager {
    public static ProxyManager INSTANCE = new ProxyManager();

    public List<Proxy> proxies;

    public ProxyManager() {
        this.proxies = new ArrayList<>();
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
                        proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port)));
                        break;
                    case "socks4", "socks5":
                        proxies.add(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ip, port)));
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
