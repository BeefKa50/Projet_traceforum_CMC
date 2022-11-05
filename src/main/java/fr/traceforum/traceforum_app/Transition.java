package fr.traceforum.traceforum_app;

import java.io.Serializable;

public class Transition implements Serializable {

    private int id;
    private String title;
    private String comment;

    public Transition(){}

    public Transition(int id, String title, String comment) {
        this.id = id;
        this.title = title;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
