package com.ragres.mongodb.iotexample.domain.dto.payloads;


public class TemperatureDataPayload extends AbstractPayload {

   private float degrees;

    public float getDegrees() {
        return degrees;
    }

    public void setDegrees(float degrees) {
        this.degrees = degrees;
    }
}
