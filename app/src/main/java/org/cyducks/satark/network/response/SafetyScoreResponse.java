package org.cyducks.satark.network.response;

import com.google.gson.annotations.SerializedName;

public class SafetyScoreResponse {
    @SerializedName("score")
    private String score;


    public String getScore() {
        return score;
    }
}
