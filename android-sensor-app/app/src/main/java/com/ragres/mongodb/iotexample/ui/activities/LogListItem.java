package com.ragres.mongodb.iotexample.ui.activities;

import java.util.Date;

public class LogListItem {

    private LogListItemType type;

    private Date timestamp;

    public LogListItemType getType() {
        return type;
    }

    public void setType(LogListItemType type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
