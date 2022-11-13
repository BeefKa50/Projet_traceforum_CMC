package fr.traceforum.traceforum_app.exceptions;

public class DataNotYetParsedException extends Exception {

    public DataNotYetParsedException() {
        super("No JSON data has been parsed. Use the parse method before " +
                "trying to compute this indicator.");
    }
}

