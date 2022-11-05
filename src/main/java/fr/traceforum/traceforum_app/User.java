package fr.traceforum.traceforum_app;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private int userId;
    private String userName;
    private ArrayList<Transition> messagesOpened = new ArrayList<Transition>();
    private ArrayList<Transition> messagesOpenedWithScrollbarDisabled = new ArrayList<Transition>();
    private ArrayList<Transition> readingEvents = new ArrayList<Transition>();

    public User(int userId, String userName, ArrayList<Transition> messagesOpened, ArrayList<Transition> messagesOpenedWithScrollbarDisabled, ArrayList<Transition> readingEvents) {
        this.userId = userId;
        this.userName = userName;
        this.messagesOpened = messagesOpened;
        this.messagesOpenedWithScrollbarDisabled = messagesOpenedWithScrollbarDisabled;
        this.readingEvents = readingEvents;
    }

    public User(){}

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

    public ArrayList<Transition> getMessagesOpened() {
        return messagesOpened;
    }

    public void setMessagesOpened(ArrayList<Transition> messagesOpened) {
        this.messagesOpened = messagesOpened;
    }

    public ArrayList<Transition> getMessagesOpenedWithScrollbarDisabled() {
        return messagesOpenedWithScrollbarDisabled;
    }

    public void setMessagesOpenedWithScrollbarDisabled(ArrayList<Transition> messagesOpenedWithScrollbarDisabled) {
        this.messagesOpenedWithScrollbarDisabled = messagesOpenedWithScrollbarDisabled;
    }

    public ArrayList<Transition> getReadingEvents() {
        return readingEvents;
    }

    public void setReadingEvents(ArrayList<Transition> readingEvents) {
        this.readingEvents = readingEvents;
    }
}
