package fr.traceforum.traceforum_app;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class TestDB {

    private static ArrayList<String> dates = new ArrayList<String>();
    private static ArrayList<Integer> msgs = new ArrayList<Integer>();
    private static ArrayList<Transition> transitions = new ArrayList<Transition>();
    public static void main(String[] args){
        try{
            // Connexion to the traceforum database
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection("jdbc:mysql://localhost:3306/traceforum","root","");

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
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
                    new FileOutputStream("test_output.xml")));
            encoder.writeObject(transitions);
            encoder.close();

            con.close();
        }catch(Exception e){ System.out.println(e);}
    }
}