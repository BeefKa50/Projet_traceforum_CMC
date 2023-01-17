package fr.traceforum.traceforum_app.data_classes;

import java.io.Serializable;

public class ReadingEvent implements Serializable {

    private int forumNumber;
    private Transition startReading;
    private Transition endReading;

    public ReadingEvent(Transition startReading, Transition endReading, int forumNumber) {
        this.startReading = startReading;
        this.endReading = endReading;
        this.forumNumber = forumNumber;
    }

    public int getForumNumber() {
        return forumNumber;
    }

    public void setForumNumber(int forumNumber) {
        this.forumNumber = forumNumber;
    }

    public Transition getStartReading() {
        return startReading;
    }

    public void setStartReading(Transition startReading) {
        this.startReading = startReading;
    }

    public Transition getEndReading() {
        return endReading;
    }

    public void setEndReading(Transition endReading) {
        this.endReading = endReading;
    }
}
