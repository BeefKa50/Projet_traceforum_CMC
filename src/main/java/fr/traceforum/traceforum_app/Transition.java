package fr.traceforum.traceforum_app;

import java.io.Serializable;
import java.util.Date;

public class Transition implements Serializable {
    private String type;
    private String date;
    private String time;


    public Transition(){}

    public Transition(String type, String date, String time) {
        this.type = type;
        this.date = date;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
