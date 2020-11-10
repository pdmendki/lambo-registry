package io.horizen.lambo.car.api.request;

public class EarnMileageTokensRequest {

    public void setProposition(String proposition) {
        this.proposition = proposition;
    }

    public void setMileage(long mileage) {
        this.mileage = mileage;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public String proposition;
    public long mileage;
    public long fee;
}
