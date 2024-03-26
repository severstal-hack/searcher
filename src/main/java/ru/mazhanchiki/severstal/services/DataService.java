package ru.mazhanchiki.severstal.services;

import com.example.grpc.DataServiceGrpc;
import com.example.grpc.DataServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mazhanchiki.severstal.config.DataServiceConfiguration;
import ru.mazhanchiki.severstal.entities.Tender;
import ru.mazhanchiki.severstal.grpc.LinkStreamObserver;

import java.util.List;

final class Connection {
    private final DataServiceGrpc.DataServiceStub stub;
    private final Runnable close;
    Connection(DataServiceGrpc.DataServiceStub stub, Runnable close) {
        this.stub = stub;
        this.close = close;
    }
    public DataServiceGrpc.DataServiceStub stub() {
        return stub;
    }
    public void close() {
        close.run();
    }
    @Override
    public String toString() {
        return "Connection[" +
                "stub=" + stub + ", " +
                "close=" + close + ']';
    }
}

@Service
@Slf4j(topic = "dataServiceGRPC")
public class DataService {

    private final DataServiceConfiguration config;

    @Autowired
    public DataService(DataServiceConfiguration dataServiceConfiguration) {
        config = dataServiceConfiguration;
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

    private Connection connect() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(config.getHost(), config.getPort())
                .usePlaintext()
                .build();

        log.info("Connecting to {}:{}", config.getHost(), config.getPort());
        return new Connection(DataServiceGrpc.newStub(channel), channel::shutdown);
    }

    public void AddLinks(List<Tender> tenders) {

        var connection = connect();
        var stub = connection.stub();

        int limit = 10000;

        StreamObserver<DataServiceOuterClass.AddRequest> observer = stub.addLinks(new LinkStreamObserver());

        var totalTenders = tenders.size();

        tenders = tenders
                        .stream()
                        .filter(tender -> tender.getLink() != null)
                        .toList();

        var tendersCount = tenders.size();
        var sent = 0;

        log.info("Tenders prepared to send: {}", tendersCount);
        log.info("Tenders without link count: {}", totalTenders - tendersCount);
        for (int i = 0; i < tendersCount; i+=limit) {
            var requestBuilder = DataServiceOuterClass.AddRequest.newBuilder();
            var window = Math.min(limit, tendersCount - i);

            for (int j = 0; j < window; j++) {
                var tender = tenders.get(i + j);
                var tenderDto = DataServiceOuterClass.Tender.newBuilder()
                        .setLink(tender.getLink())
                        .setDomain(tender.getDomain()
                ).build();
                requestBuilder.addTenders(tenderDto);
            }
            observer.onNext(requestBuilder.build());
            sent += window;
            log.info("Remaining {} tenders", tendersCount - sent);
        }

        observer.onCompleted();

        connection.close();
    }
}
