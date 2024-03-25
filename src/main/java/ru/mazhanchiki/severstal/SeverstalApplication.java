package ru.mazhanchiki.severstal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.io.IOException;

@SpringBootApplication
public class SeverstalApplication {

	public static void main(String[] args) {

//        try {
//            ProxyManager.INSTANCE.fetchProxies("https://raw.githubusercontent.com/proxifly/free-proxy-list/main/proxies/all/data.json");
//            ProxyManager.INSTANCE.fetchProxies("https://raw.githubusercontent.com/proxifly/free-proxy-list/main/proxies/countries/RU/data.json");
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        ProxyManager.INSTANCE.clean();

        SpringApplication.run(SeverstalApplication.class, args);
	}

}
