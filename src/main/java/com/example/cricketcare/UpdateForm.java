package com.example.cricketcare;

public class UpdateForm {

    private Long reservationId;
    private String json;

    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public Long getReservationId() {
        return this.reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }


//    public String toString() {
//        return "Person(Name: " + this.name + ", Age: " + this.age + ")";
//    }
}

