package fr.traceforum.traceforum_app.exceptions;

public class UserNotFoundException extends Exception{

    private String username;

    public UserNotFoundException(String username) {
        super("User not found : " + username);
        this.username = username;
    }
}
