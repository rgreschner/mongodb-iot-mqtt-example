package com.ragres.mongodb.iotexample.domain.dto.payloads;


import java.util.Date;

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
