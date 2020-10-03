package com.example.abcapp.Notif;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;

public class Notification implements Serializable {
    private String name;
    private final int id;
    private Calendar calendar;
    private boolean enabled = true;

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

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public Calendar getCalendar(){
        return this.calendar;
    }

    public boolean toggleEnabled(){
        enabled = !enabled;
        return enabled;
    }

    public boolean isEnabled(){
        return enabled;
    }
}
