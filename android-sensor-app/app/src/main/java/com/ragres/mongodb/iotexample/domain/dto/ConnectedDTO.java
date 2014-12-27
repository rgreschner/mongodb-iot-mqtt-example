package com.ragres.mongodb.iotexample.domain.dto;

import java.util.Date;

/**
 * Data transfer object
 * signaling client was connected.
 */
public class ConnectedDTO {

    /**
     * Unix timestamp in milliseconds.
     */
    private long timestamp;

    /**
     * Default constructor.
     */
    public ConnectedDTO() {
        Date now = new Date();
        timestamp = now.getTime();
    }
}
