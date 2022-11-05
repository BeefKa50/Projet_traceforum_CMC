package fr.traceforum.traceforum_app;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private int userId;
    private String userName;
    private ArrayList<Transition> transitions = new ArrayList<Transition>();

    public User(){}

    public User(int userId, String userName, ArrayList<Transition> transitions) {
        this.userId = userId;
        this.userName = userName;
        this.transitions = transitions;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArrayList<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(ArrayList<Transition> transitions) {
        this.transitions = transitions;
    }
}
