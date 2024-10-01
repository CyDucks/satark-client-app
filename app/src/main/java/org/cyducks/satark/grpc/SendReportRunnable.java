package org.cyducks.satark.grpc;

import org.cyducks.generated.Point;
import org.cyducks.generated.Report;
import org.cyducks.generated.ReportAck;
import org.cyducks.generated.ReportServiceGrpc.ReportServiceBlockingStub;
import org.cyducks.generated.ReportServiceGrpc.ReportServiceStub;

import java.util.Date;

public class SendReportRunnable implements GrpcRunnable{
    private String zoneId;
    private String modId;
    private float lat;
    private float lng;
    private String type;

    public SendReportRunnable(String zoneId, String modId, float lat, float lng, String type) {
        this.zoneId = zoneId;
        this.modId = modId;
        this.lat = lat;
        this.lng = lng;
        this.type = type;
    }


    @Override
    public String run(ReportServiceBlockingStub blockingStub, ReportServiceStub asyncStub) throws Exception {
        return sendReport(blockingStub);
    }

    public String sendReport(ReportServiceBlockingStub blockingStub) {
        Point requestLocation = Point
                .newBuilder()
                .setLat(lat)
                .setLng(lng)
                .build();

        Report reportRequest = Report
                .newBuilder()
                .setZoneId(zoneId)
                .setModeratorId(modId)
                .setType(type)
                .setTimestamp(new Date().toString())
                .setLocation(requestLocation)
                .build();


        ReportAck ack = blockingStub.sendReport(reportRequest);

        return Integer.toString(ack.getStatus());
    }
}
