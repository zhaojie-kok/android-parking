package com.example.abcapp.Notif;

import com.example.abcapp.Carparks.Carpark;

import java.io.Serializable;
import java.util.Calendar;

public class Notification implements Serializable {
    private String name;
    private final int id;
    private Calendar calendar;
    private Calendar arrival;
    private boolean enabled = true;
    private Carpark carpark = null;

    public Notification(String name, int id, Calendar calendar){
        this.name = name;
        this.id = id;
        this.calendar = calendar;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setCalendar(Calendar calendar){
        this.calendar = calendar;
    }

    public void setCarpark(Carpark carpark){ this.carpark = carpark; }

    public void setArrival(Calendar arrival) {this.arrival = arrival; }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public Calendar getCalendar(){
        return calendar;
    }

    public Carpark getCarpark(){ return carpark; }

    public String getCarparkName(){ return carpark.getAddress(); }

    public Calendar getArrival(){ return arrival; }

    public boolean toggleEnabled(){
        enabled = !enabled;
        return enabled;
    }

    public boolean isEnabled(){
        return enabled;
    }
}
