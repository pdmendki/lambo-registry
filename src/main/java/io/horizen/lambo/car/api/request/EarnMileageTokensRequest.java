package io.horizen.lambo.car.api.request;

public class EarnMileageTokensRequest {

    public void setProposition(String proposition) {
        this.proposition = proposition;
    }

    public void setDrive(long drive) {
        this.drive = drive;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public String proposition;
    public long drive;
    public long fee;
}
