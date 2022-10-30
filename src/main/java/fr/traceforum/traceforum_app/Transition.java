package fr.traceforum.traceforum_app;

import java.io.Serializable;

public class Transition implements Serializable {

    private int id;
    private String user;
    private String title;
    private String comment;

    public Transition(){}

    public Transition(int id, String user, String title, String comment) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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
