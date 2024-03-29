package ru.mazhanchiki.severstal.services;

import com.example.grpc.DataServiceGrpc;
import com.example.grpc.DataServiceOuterClass;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final String host;
    private final int port;


    public DataService() {
        host = System.getenv("GRPC_DATA_SERVICE_HOST");
        port = Integer.parseInt(System.getenv("GRPC_DATA_SERVICE_PORT"));
        log.info("GRPC_DATA_SERVICE_HOST={}", host);
        log.info("GRPC_DATA_SERVICE_PORT={}", port);
    }

    private Connection connect() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        log.info("Connecting to {}:{}", host, port);
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
            log.trace("Remaining {} tenders", tendersCount - sent);
        }

        observer.onCompleted();

        connection.close();
    }

    public String[] getProducts() {


        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        var stub = DataServiceGrpc.newBlockingStub(channel);

        log.info("Connecting to {}:{}", host, port);


        var req = DataServiceOuterClass.EmptyRequest.newBuilder().build();

        var response = stub.getProducts(req);

        log.info("Products count: {}", response.getProductsCount());


        var p = response
                .getProductsList()
                .stream()
                .map(DataServiceOuterClass.Product::getName)
                .toArray(String[]::new);

        channel.shutdown();

        return p;
    }

    public List<ru.mazhanchiki.severstal.entities.dto.Tender> match(String phrase) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        var stub = DataServiceGrpc.newBlockingStub(channel);

        log.info("Connecting to {}:{}", host, port);


        var req = DataServiceOuterClass.MatchRequest.newBuilder().setPhrase(phrase).build();
        var response = stub.match(req);

        log.info("Matched items count: {}", response.getItemsCount());


        var r = response.getItemsList()
                .stream()
                .map(t -> new ru.mazhanchiki.severstal.entities.dto.Tender(t.getName(), t.getLink()))
                .toList();
        channel.shutdown();
        return r;
    }

}
