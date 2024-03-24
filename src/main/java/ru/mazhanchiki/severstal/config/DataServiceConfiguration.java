package ru.mazhanchiki.severstal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "grpc.data-service")
public class DataServiceConfiguration {
    private String host;
    private int port;

}

