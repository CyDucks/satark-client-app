package org.cyducks.satark.network.request;

public class SafetyScoreRequest {
    private String review;


    public SafetyScoreRequest(String review) {
        this.review = review;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
