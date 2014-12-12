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
     * Payload identifier.
     * Needed since serialization to
     * JSON removes type information.
     */
    private String payloadType;

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
     * @param payloadType Type identifier of payload.
     * @param payload Actual payload.
     * @return Created DTO.
     */
    public static SensorDataDTO createWithPayload(String payloadType, AbstractPayload payload) {
        SensorDataDTO created = new SensorDataDTO();
        created.setPayloadType(payloadType);
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

    /**
     * Get payload identifier.
     * @return Payload identifier.
     */
    public String getPayloadType() {
        return payloadType;
    }

    /**
     * Set payload identifier.
     * @param payloadType Payload identifier.
     */
    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }
}
