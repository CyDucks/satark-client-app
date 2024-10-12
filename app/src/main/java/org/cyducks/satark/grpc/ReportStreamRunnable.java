package org.cyducks.satark.grpc;


import org.cyducks.generated.Report;
import org.cyducks.generated.ReportRequest;
import org.cyducks.generated.ReportServiceGrpc;

import io.grpc.stub.StreamObserver;

public class ReportStreamRunnable implements GrpcRunnable{
    private final String modId;
    private final StreamObserver<Report> reportStreamObserver;

    public ReportStreamRunnable(String modId, StreamObserver<Report> reportStreamObserver) {
        this.modId = modId;
        this.reportStreamObserver = reportStreamObserver;
    }

    @Override
    public String run(ReportServiceGrpc.ReportServiceBlockingStub blockingStub, ReportServiceGrpc.ReportServiceStub asyncStub) throws Exception {
        return getReports(asyncStub);
    }

    public String getReports(ReportServiceGrpc.ReportServiceStub asyncStub) {
        StringBuilder logBuilder = new StringBuilder();

        ReportRequest reportRequest = ReportRequest.newBuilder()
                .setModeratorId(modId)
                .build();

        asyncStub.getLiveReports(reportRequest, reportStreamObserver);

        return logBuilder.toString();
    }
}
