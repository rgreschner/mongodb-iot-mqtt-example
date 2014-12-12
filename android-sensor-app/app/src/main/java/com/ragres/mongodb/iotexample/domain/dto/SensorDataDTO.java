package com.ragres.mongodb.iotexample.domain.dto;

import com.ragres.mongodb.iotexample.domain.dto.payloads.AbstractPayload;

import java.util.Date;

/**
 * Data transfer object for
 * sensor data.
 */
public class SensorDataDTO {

    /**
     * Actual sensor data payload.
     */
    private AbstractPayload payload;

    /**
     * Unix timestamp in milliseconds.
     */
    private long timestamp;

    /**
     * Default constructor.
     */
    public SensorDataDTO() {
        Date now = new Date();
        timestamp = now.getTime();
    }

    /**
     * Get payload.
     * @return Payload.
     */
    public AbstractPayload getPayload() {
        return payload;
    }

    /**
     * Set payload.
     * @param payload Payload.
     */
    public void setPayload(AbstractPayload payload) {
        this.payload = payload;
    }

    /**
     * Create sensor data DTO with payload.
     * @param payload Actual payload.
     * @return Created DTO.
     */
    public static SensorDataDTO createWithPayload(AbstractPayload payload) {
        SensorDataDTO created = new SensorDataDTO();
        created.setPayload(payload);
        return created;
    }

    /**
     * Get timestamp of measurement.
     *
     * @return Timestamp of measurement.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp of measurement.
     *
     * @param timestamp Timestamp of measurement..
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
