package fr.traceforum.traceforum_app;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return con;
    }

    public ResultSet getUsers(){
        con = initializeConnexion();

        Statement stmt= null;
        ResultSet rs = null;
        try {
            assert con != null;
            stmt = con.createStatement();
            rs = stmt.executeQuery("select distinct Utilisateur from transition ");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return rs;
    }

    private int addResultSetToArrayList(ResultSet rs, ArrayList al, String type){
        int count = 0;
        con = initializeConnexion();

        while(true){
            try {
                if (!rs.next()) break;

                if(type == "Finish reading a message"){
                    Statement stmt=con.createStatement();

                    ResultSet rsf =stmt.executeQuery("SELECT RefTran FROM transition WHERE Titre = '" +
                            "Bouger la scrollbar en bas - afficher la fin du message" + "' AND Date = '" +
                            rs.getString(3) + "' AND Heure = '" + rs.getString(4) + "'");

                    rsf.next();

                    rsf = stmt.executeQuery("SELECT Date,Heure FROM transition WHERE RefTran = " + rsf.getInt(1) +
                             " ORDER BY Date,Heure ");
                    rsf.next();

                    Transition start = new Transition("Start reading a message",rsf.getString(1),
                            rsf.getString(2));
                    Transition end = new Transition(type,rs.getString(3),
                            rs.getString(4));

                    al.add(new ReadingEvent(start,end));
                }
                else {
                    al.add(new Transition(type,rs.getString(3),
                            rs.getString(4)));
                }
                count++;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }



    private User getUserData(int id, String username){

        ArrayList<Integer> msgs = new ArrayList<Integer>();
        ArrayList<Transition> displayMsg = new ArrayList<Transition>();
        ArrayList<Transition> displayMsgScrollbarDisabled = new ArrayList<Transition>();
        ArrayList<ReadingEvent> readingEvents = new ArrayList<ReadingEvent>();
        ArrayList<Transition> postMsg = new ArrayList<Transition>();



        try{
            int i = 0;

            int completely = 0;
            int partially = 0;

            con = initializeConnexion();

            Statement stmt=con.createStatement();
            ResultSet rs1 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = 'Afficher le contenu d''un message' AND " +
                    "Commentaire IS NULL ORDER BY Date,Heure;");

            partially += addResultSetToArrayList(rs1,displayMsg,"Open a message");

            ResultSet rs2 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = 'Afficher le contenu d''un message' AND " +
                    "Commentaire = 'Scrollbar inactive' ORDER BY Date,Heure;");

            completely += addResultSetToArrayList(rs2,displayMsgScrollbarDisabled,"Open a message - " +
                    " the scrollbar is disabled");

            ResultSet rs3 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = " +
                    "'Bouger la scrollbar en bas - afficher la fin du message' ORDER BY Date,Heure");

            completely += addResultSetToArrayList(rs3,readingEvents,"Finish reading a message");

            ResultSet rs4 = stmt.executeQuery("SELECT DISTINCT Titre,Commentaire,Date,Heure FROM `transition` WHERE "
                    + "Utilisateur = '" + username + "' AND Titre = " +
                    "'Poster un nouveau message' ORDER BY Date,Heure");

            addResultSetToArrayList(rs4,postMsg,"Post a new message");

            System.out.println("User : " + username);
            System.out.println("Partially read : "+partially);
            System.out.println("Completely read : "+completely);

        }catch(Exception e){ e.printStackTrace();}

        return new User(id,username,displayMsg,displayMsgScrollbarDisabled,readingEvents,postMsg);
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

    public static void main(String[] args){
        DBControl dbControl = new DBControl("jdbc:mysql://localhost:3306/traceforum","root","");
        dbControl.generateXML();
    }
}