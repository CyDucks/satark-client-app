package org.cyducks.satark;

import java.util.Arrays;
import java.util.List;

public class AppConstants {
    public static final List<String> SUPPORTED_CITIES = Arrays.asList("Nagpur", "Pune", "Mumbai", "Delhi");
    public static final String GRPC_SERVER_ADDRESS="34.93.47.28";
    public static final String REST_SERVER_BASE_URL="http://34.93.47.28:8080";
    public static final String SAFETY_SCORE_URL="http://34.93.47.28:8085/";
    public static final String TAG = "APP";
}