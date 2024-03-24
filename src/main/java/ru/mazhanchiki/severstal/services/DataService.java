package ru.mazhanchiki.severstal.services;

import com.example.grpc.DataServiceGrpc;
import com.example.grpc.DataServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mazhanchiki.severstal.config.DataServiceConfiguration;
import ru.mazhanchiki.severstal.dtos.grpc.TenderDto;
import ru.mazhanchiki.severstal.entities.Tender;

import java.util.List;

@Service
@Slf4j(topic = "dataServiceGRPC")
public class DataService {

    private final DataServiceConfiguration config;
    private final DataServiceGrpc.DataServiceBlockingStub stub;

    @Autowired
    public DataService(DataServiceConfiguration dataServiceConfiguration) {
        config = dataServiceConfiguration;
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(dataServiceConfiguration.getHost(), dataServiceConfiguration.getPort())
                .usePlaintext()
                .build();

        log.info("Connecting to {}:{}", dataServiceConfiguration.getHost(), dataServiceConfiguration.getPort());
        this.stub = DataServiceGrpc.newBlockingStub(channel);
    }
    public boolean healthCheck() {
        ManagedChannel channel = null;
        try {
            channel = ManagedChannelBuilder.forAddress(config.getHost(), config.getPort())
                    .usePlaintext()
                    .build();

            if (channel != null) {
                return !channel.isTerminated() || !channel.isShutdown();
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }
    }
    public void AddLinks(List<Tender> tenders) {
//        log.info("Adding links: {}", links);
        DataServiceOuterClass.AddRequest.Builder builder = DataServiceOuterClass.AddRequest
                .newBuilder();


        builder.addAllTenders(tenders.stream()
                        .filter(tender -> tender.getLink() != null)
                        .map(tender -> DataServiceOuterClass.Tender.newBuilder()
                        .setLink(tender.getLink())
                        .setDomain(tender.getDomain())
                        .build())
                .toList());

        DataServiceOuterClass.AddRequest request = builder.build();
        try {
            var response = stub.addLinks(request);
            log.info("Links added successfully (count: {})", response.getCount());
        } catch (Exception e) {
            log.error("Error while send links to grpc:AddLinks");
            throw new RuntimeException();
        }
    }
}
