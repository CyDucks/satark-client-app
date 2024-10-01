package org.cyducks.satark.grpc;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.cyducks.generated.ReportServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcWorker extends Worker {
    private final GrpcRunnable grpcRunnable;
    private final ManagedChannel managedChannel;

    public GrpcWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) throws Exception {
        super(context, workerParams);
        this.grpcRunnable = getRunnable(getInputData());
        this.managedChannel = getChannel(getInputData());
    }

    @NonNull
    @Override
    public Result doWork() {
        String status;

        try {
            status = grpcRunnable.run(ReportServiceGrpc.newBlockingStub(managedChannel), ReportServiceGrpc.newStub(managedChannel));
            Data outputData = new Data.Builder()
                    .putString("WORK_RESULT", "Success: " + status)
                    .build();

            managedChannel.shutdown();
            return Result.success(outputData);
        } catch (Exception e) {
            Data outputData = new Data.Builder()
                    .putString("WORKER_RESULT", e.getMessage())
                    .build();
            Log.d("MYAPP", "doWork: " + e);
            e.printStackTrace();

            managedChannel.shutdown();
            return Result.failure(outputData);
        }
    }

    private GrpcRunnable getRunnable(Data inputData) throws Exception {
        String requestType = inputData.getString("request_type");

        if(requestType != null) {
            switch (requestType) {
                case "sendReport":
                    String zoneId = getInputData().getString("zone_id");
                    String modId = getInputData().getString("mod_id");

                    float lat = getInputData().getFloat("lat", -1);
                    float lng = getInputData().getFloat("lng", -1);

                    String type = getInputData().getString("type");

                    return new SendReportRunnable(zoneId, modId, lat, lng, type);
                default:
                    return null;
            }
        }

        throw new Exception("Request type is invalid");
    }

    private ManagedChannel getChannel(Data inputData) {
        return ManagedChannelBuilder.forAddress("10.0.2.2", 9000).usePlaintext().build();
    }
}
