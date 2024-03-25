package ru.mazhanchiki.severstal.grpc;

import com.example.grpc.DataServiceOuterClass;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LinkStreamObserver implements StreamObserver<DataServiceOuterClass.AddResponse> {
    @Override
    public void onNext(DataServiceOuterClass.AddResponse addResponse) {
        log.info("successfully added {} links", addResponse.getCount());
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        log.info("stream completed");
    }
}
