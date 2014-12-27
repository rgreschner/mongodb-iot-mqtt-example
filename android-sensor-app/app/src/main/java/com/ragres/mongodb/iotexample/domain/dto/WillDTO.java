package com.ragres.mongodb.iotexample.domain.dto;


import java.util.Date;

/**
 * Data transfer object
 * for will & testament functionality of
 * MQTT broker.
 */
public class WillDTO {

    private long creationDate;

    public WillDTO() {
        Date now = new Date();
        creationDate = now.getTime();
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
}
