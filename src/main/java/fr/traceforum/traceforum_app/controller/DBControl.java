package fr.traceforum.traceforum_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.traceforum.traceforum_app.model.ReadingEvent;
import fr.traceforum.traceforum_app.model.Transition;
import fr.traceforum.traceforum_app.model.User;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * This class is used to perform a connexion to an external database containing some forum logs.
 * It may be used to generate a JSON document filled with all data necessary to calculate these indicators :
 *
 * - The completely read messages / partially read messages ratio
 * - The average and global reading time for completely read messages
 * - The exact number of messages posted by a user on a selected time period
 *
 * @see ReadingEvent
 * @see Transition
 * @see User
 */
public class DBControl {

    private String url = null;
    private String user = null;
    private String password = null;

    private Connection con;

    /**
     *
     * @param url database url
     * @param user database access login
     * @param password database access password
     */
    public DBControl(String url, String user, String password){
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * This function can be used to initialize a connexion with the given database
     * @return a Connection object that can be used to query the database
     */
    private Connection initializeConnexion(){
        if(url == null || user == null){
            System.err.println("Error : no connexion url or username given.");
            System.err.println("Make sure that you correctly initialized the class giving a connexion url and" +
                    "a username.");
            return null;
        }

        // Connexion to the traceforum database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url,user,password);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return con;
    }

    /**
     * This function returns the list of all the users of the forum
     * @return The ResultSet containing every single user of the forum
     */
    public ResultSet getUsers(){

        // Connexion to the database
        con = initializeConnexion();

        Statement stmt= null;
        ResultSet rs = null;
        try {
            assert con != null;
            stmt = con.createStatement();

            // Get the complete list of users
            rs = stmt.executeQuery("select distinct Utilisateur from transition ");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rs;
    }

    /**
     * This function reads every row of the given ResultSet and put it into the given ArrayList.
     * It can also operate a specific treatment according to the given type.
     * @param rs the ResultSet object we want the items to be added to the ArrayList
     * @param al the ArrayList in which we want to put the items
     * @param type the type of items we have in the ResultSet. It is used to have a specific treatment for a particular
     *             type.
     */
    private void addResultSetToArrayList(ResultSet rs, ArrayList al, String type){

        // Connexion to the database
        con = initializeConnexion();

        // Iterate on the ResultSet
        while(true){
            try {
                if (!rs.next()) break;

                // SPECIFIC CASE
                // Operate a specific treatment when the event type provided is 'Finish reading a message'
                // We want to get the transition corresponding to the moment when a user started to read a message,
                // then create a ReadingEvent object and finnally add this object to the ArrayList
                if(type == "Finish reading a message"){
                    Statement stmt=con.createStatement();

                    // Get the RefTran ID of the end transition
                    ResultSet rsf =stmt.executeQuery("SELECT RefTran FROM transition WHERE Titre = '" +
                            "Bouger la scrollbar en bas - afficher la fin du message" + "' AND Date = '" +
                            rs.getString(3) + "' AND Heure = '" + rs.getString(4) + "'");

                    rsf.next();

                    // Get the Date and Time corresponding to the first event chronologically with the same RefTran
                    // as the end transition.
                    // This way we get the data of the start transition
                    rsf = stmt.executeQuery("SELECT Date,Heure FROM transition WHERE RefTran = " + rsf.getInt(1) +
                             " ORDER BY Date,Heure ");
                    rsf.next();

                    // Create the start and end transitions
                    Transition start = new Transition("Start reading a message",rsf.getString(1),
                            rsf.getString(2));
                    Transition end = new Transition(type,rs.getString(3),
                            rs.getString(4));

                    // Fuze them into a ReadingEvent object and add it to the ArrayList
                    al.add(new ReadingEvent(start,end));
                }

                // DEFAULT CASE
                else {
                    // Add the current transition to the ArrayList
                    al.add(new Transition(type,rs.getString(3),
                            rs.getString(4)));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // Close the connexion to the database
        try {
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This function returns all the data we need on a single user to compute the specified indicators.
     * @param id the id we want to give to a user
     * @param username the username
     * @return a User object containing all the data needed for a single user in order to compute the indicators.
     */
    private User getUserData(int id, String username){

        // ArrayList objects that will contain the useful data about this user
        ArrayList<Transition> displayMsg = new ArrayList<Transition>();
        ArrayList<Transition> displayMsgScrollbarDisabled = new ArrayList<Transition>();
        ArrayList<ReadingEvent> readingEvents = new ArrayList<ReadingEvent>();
        ArrayList<Transition> postMsg = new ArrayList<Transition>();


        try{
            // Connexion to the database
            con = initializeConnexion();

            Statement stmt=con.createStatement();

            // Get the transitions corresponding to the display of a message
            ResultSet rs1 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = 'Afficher le contenu d''un message' AND " +
                    "Commentaire IS NULL ORDER BY Date,Heure;");

            // Add this data to the displayMsg ArrayList
            addResultSetToArrayList(rs1,displayMsg,"Open a message");

            // Get the transitions corresponding to every displayed message with the scrollbar disabled
            ResultSet rs2 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = 'Afficher le contenu d''un message' AND " +
                    "Commentaire = 'Scrollbar inactive' ORDER BY Date,Heure;");

            // Add this data to the displayMsgScrollbarDisabled ArrayList
            addResultSetToArrayList(rs2,displayMsgScrollbarDisabled,"Open a message - " +
                    " the scrollbar is disabled");

            // Get the transitions corresponding to the moments when a user reached the end of a message
            // by scrolling down
            ResultSet rs3 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = " +
                    "'Bouger la scrollbar en bas - afficher la fin du message' ORDER BY Date,Heure");

            // Add this data to the readingEvents ArrayList
            addResultSetToArrayList(rs3,readingEvents,"Finish reading a message");

            // Get the transitions corresponding to every message posted by this user on the forum
            ResultSet rs4 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = " +
                    "'Poster un nouveau message' ORDER BY Date,Heure");

            // Add this data to the postMsg ArrayList
            addResultSetToArrayList(rs4,postMsg,"Post a new message");

        }catch(Exception e){ e.printStackTrace();}

        return new User(id,username,displayMsg,displayMsgScrollbarDisabled,readingEvents,postMsg);
    }

    /**
     * This method generates a JSON document containing all the data needed to calculate the indicators.
     * This document may later be parsed to compute them.
     */
    public void generateJSON(){

        // Get the complete list of users as a ResultSet
        ResultSet rs = getUsers();

        ArrayList<User> users = new ArrayList<User>();

        int i = 1;
        // Iterate of the users ResultSet
        while(true){
            try {
                if (!rs.next()) break;

                // Get the current user's data as a User object
                User user = getUserData(i,rs.getString(1));

                // Add it to the ArrayList containing all users' data
                users.add(user);
                i++;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // Serialize the ArrayList users in a JSON document
        ObjectMapper objectMapper = new ObjectMapper();
        File output_dir = new File("generated_files");
        if(!output_dir.exists()) output_dir.mkdir();
        File output_file = new File("generated_files/traceforum_data.json");
        try {
            objectMapper.writeValue(output_file, users);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        DBControl dbControl = new DBControl("jdbc:mysql://localhost:3306/traceforum","root","");
        dbControl.generateJSON();
    }
}