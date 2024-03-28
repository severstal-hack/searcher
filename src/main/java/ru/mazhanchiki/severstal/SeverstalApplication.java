package ru.mazhanchiki.severstal;

import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.mazhanchiki.severstal.proxy.ProxyManager;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class SeverstalApplication {

	public static void main(String[] args) {

		// Костылёчек
		log.info("Installing playwright browsers");
		Playwright playwright = Playwright.create();
		playwright.close();
		log.info("Installed playwright browsers");

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
