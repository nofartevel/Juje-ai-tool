package com.example.jujeaibackend;

import java.util.List;

public class AiKnownInfo {

    private List<String> age_groups;
    private String feeding_mode;
    private String location;
    private String transport;

    public AiKnownInfo() {
    }

    public List<String> getAge_groups() {
        return age_groups;
    }

    public void setAge_groups(List<String> age_groups) {
        this.age_groups = age_groups;
    }

    public String getFeeding_mode() {
        return feeding_mode;
    }

    public void setFeeding_mode(String feeding_mode) {
        this.feeding_mode = feeding_mode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }
}