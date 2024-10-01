package org.cyducks.satark.grpc;

import org.cyducks.generated.ReportServiceGrpc.ReportServiceStub;
import org.cyducks.generated.ReportServiceGrpc.ReportServiceBlockingStub;

public interface GrpcRunnable {
    String run(ReportServiceBlockingStub blockingStub, ReportServiceStub asyncStub) throws Exception;
}
