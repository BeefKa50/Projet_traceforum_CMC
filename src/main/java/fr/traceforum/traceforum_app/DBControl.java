package fr.traceforum.traceforum_app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class DBControl {

    private String url = null;
    private String user = null;
    private String password = null;

    private Connection con;

    public DBControl(String url, String user, String password){
        this.url = url;
        this.user = user;
        this.password = password;
    }

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return con;
    }

    public ResultSet getUsers(){
        con = initializeConnexion();

        Statement stmt= null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("select distinct Utilisateur from transition ");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rs;
    }

    private User getUserData(int id, String username){

        ArrayList<String> dates = new ArrayList<String>();
        ArrayList<Integer> msgs = new ArrayList<Integer>();
        ArrayList<Transition> transitions = new ArrayList<Transition>();

        try{
            con = initializeConnexion();

            // Query the database in order to get the transitions corresponding to the given user
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from transition where Utilisateur = '" + username + "'");

            // Data exploration in the transition table
            while(rs.next()){

                // Selection in order to keep only the interesting transitions according to the indicators we want
                // to provide
                if(rs.getString(3).equals("Bouger la scrollbar en bas - afficher la fin du message")
                        || rs.getString(3).equals("Afficher le contenu d'un message")
                        || rs.getString(3).equals("Poster un nouveau message")){

                    // Deletion of the duplicates (some transitions were recorded two times) by checking that
                    // each complete date (including the precise time) is different
                    if( dates.indexOf(rs.getString(5) + rs.getString(6)) == -1 ){

                        /*
                        Clean the incoming data to keep only one transition for a single message.
                                This way, we will count a message saw by a user only one time, even if he read it
                        multiple times.
                        */


                        // Split the Attributes column in order to distinguish the IDs
                        String[] attributes =  rs.getString(4).split(",");

                        for(int i = 0 ; i< attributes.length ; i++) {

                            // Get for each ID its name and value
                            String[] vals = attributes[i].split("=");

                            // Keep only the IDMsg IDs and check that this message hasn't been taken in account yet
                            if (vals[0].equals("IDMsg") && msgs.indexOf(vals[1]) == -1) {

                                // Store the collected data in a Transition object
                                Transition transition = new Transition(rs.getInt(1),
                                        rs.getString(3),
                                        rs.getString(9));

                                // Add it to the ArrayList
                                transitions.add(transition);

                                // Add the IDMsg to the corresponding ArrayList to indicate that this message
                                // has been taken into account
                                msgs.add(Integer.parseInt(vals[1]));
                            }
                        }
                    }

                    // Add the date to the corresponding ArrayList to indicate that the record related to this precise
                    // date was taken into account
                    dates.add(rs.getString(5) + rs.getString(6));
                }
            }
            con.close();

        }catch(Exception e){ System.out.println(e);}

        User user = new User(id,username,transitions);
        return user;
    }

    public void generateXML(){
        ResultSet rs = getUsers();
        ArrayList<User> users = new ArrayList<User>();

        int i = 1;
        while(true){
            try {
                if (!rs.next()) break;
                User user = getUserData(i,rs.getString(1));
                users.add(user);
                i++;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("test_output.json"), users);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Old version with a previous data model
    /*
    public void generateXML(){
        try{
            con = initializeConnexion();

            // Query the database in order to get the whole transition table
            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from transition");

            // Data exploration in the transition table
            while(rs.next()){

                // Selection in order to keep only the interesting transitions according to the indicators we want
                // to provide
                if(rs.getString(3).equals("Bouger la scrollbar en bas - afficher la fin du message")
                        || rs.getString(3).equals("Afficher le contenu d'un message")
                        || rs.getString(3).equals("Poster un nouveau message")){

                    // Deletion of the duplicates (some transitions were recorded two times) by checking that
                    // each complete date (including the precise time) is different
                    if( dates.indexOf(rs.getString(5) + rs.getString(6)) == -1 ){


                        Clean the incoming data to keep only one transition for a single message.
                        This way, we will count a message saw by a user only one time, even if he read it
                        multiple times.


                        // Split the Attributes column in order to distinguish the IDs
                        String[] attributes =  rs.getString(4).split(",");

                        for(int i = 0 ; i< attributes.length ; i++) {

                            // Get for each ID its name and value
                            String[] vals = attributes[i].split("=");

                            // Keep only the IDMsg IDs and check that this message hasn't been taken in account yet
                            if (vals[0].equals("IDMsg") && msgs.indexOf(vals[1]) == -1) {

                                // Store the collected data in a Transition object
                                Transition transition = new Transition(rs.getInt(1),
                                        rs.getString(2), rs.getString(3),
                                        rs.getString(9));

                                // Add it to the ArrayList
                                transitions.add(transition);

                                // Add the IDMsg to the corresponding ArrayList to indicate that this message
                                // has been taken into account
                                msgs.add(Integer.parseInt(vals[1]));
                            }
                        }
                    }

                    // Add the date to the corresponding ArrayList to indicate that the record related to this precise
                    // date was taken into account
                    dates.add(rs.getString(5) + rs.getString(6));
                }
            }

            // Output : generate an XML file with the clean and selected data
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File("test_output.json"), transitions);


            con.close();
        }catch(Exception e){ System.out.println(e);}
    }
    */

    public static void main(String[] args){
        DBControl dbControl = new DBControl("jdbc:mysql://localhost:3306/traceforum","root","");
        dbControl.generateXML();
    }
}