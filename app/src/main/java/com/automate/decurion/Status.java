package com.automate.decurion;



public class Status {
    private String id;
    private String message;
    private int severity;

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
    private long time;

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getSeverity() {
        return severity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }
}
