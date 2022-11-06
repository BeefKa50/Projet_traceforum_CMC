package fr.traceforum.traceforum_app.data_classes;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private int userId;
    private String userName;
    private ArrayList<Transition> messagesOpened = new ArrayList<Transition>();
    private ArrayList<Transition> messagesOpenedWithScrollbarDisabled = new ArrayList<Transition>();
    private ArrayList<ReadingEvent> readingEvents = new ArrayList<ReadingEvent>();

    private ArrayList<Transition> postMsg = new ArrayList<Transition>();

    public User(int userId, String userName, ArrayList<Transition> messagesOpened,
                ArrayList<Transition> messagesOpenedWithScrollbarDisabled, ArrayList<ReadingEvent> readingEvents,
                ArrayList<Transition> postMsg) {
        this.userId = userId;
        this.userName = userName;
        this.messagesOpened = messagesOpened;
        this.messagesOpenedWithScrollbarDisabled = messagesOpenedWithScrollbarDisabled;
        this.readingEvents = readingEvents;
        this.postMsg = postMsg;
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

    public ArrayList<ReadingEvent> getReadingEvents() {
        return readingEvents;
    }

    public void setReadingEvents(ArrayList<ReadingEvent> readingEvents) {
        this.readingEvents = readingEvents;
    }

    public ArrayList<Transition> getPostMsg() {
        return postMsg;
    }

    public void setPostMsg(ArrayList<Transition> postMsg) {
        this.postMsg = postMsg;
    }
}
