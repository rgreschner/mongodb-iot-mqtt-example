package com.ragres.mongodb.iotexample.domain.dto;

import java.util.Date;

/**
 * Data transfer object for
 * test messages.
 */
public class TestMessageDTO {

    /**
     * Unix timestamp in milliseconds.
     */
    private long timestamp;

    /**
     * Textual test message.
     */
    private String message;

    /**
     * Default constructor.
     */
    public TestMessageDTO() {
        Date now = new Date();
        timestamp = now.getTime();
    }

    /**
     * Get textual test message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set textual test message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
