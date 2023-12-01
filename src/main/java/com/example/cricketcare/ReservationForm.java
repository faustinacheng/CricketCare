package com.example.cricketcare;

import java.time.LocalDateTime;

public class ReservationForm {

    private Long clientId;
    private String userId;
    private LocalDateTime startTime;
    private String numSlots;
    private String customValues;

    public String getCustomValues() {
        return this.customValues;
    }

    public void setCustomValues(String customValues) {
        this.customValues = customValues;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getNumSlots() {
        return this.numSlots;
    }

    public void setNumSlots(String numSlots) {
        this.numSlots = numSlots;
    }


//    public String toString() {
//        return "Person(Name: " + this.name + ", Age: " + this.age + ")";
//    }
}
