package fr.traceforum.traceforum_app.exceptions;

public class UnknownIndicatorException extends Exception{
    public UnknownIndicatorException(String indicator){
        super(indicator + "do not exist or wasn't computed yet.");
    }
}
