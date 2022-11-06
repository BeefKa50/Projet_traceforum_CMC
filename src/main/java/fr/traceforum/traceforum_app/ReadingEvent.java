package fr.traceforum.traceforum_app;

import java.io.Serializable;

public class ReadingEvent implements Serializable {
    private Transition startReading;
    private Transition endReading;

    public ReadingEvent(Transition startReading, Transition endReading) {
        this.startReading = startReading;
        this.endReading = endReading;
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
