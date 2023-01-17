package fr.traceforum.traceforum_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.traceforum.traceforum_app.exceptions.DataNotYetParsedException;
import fr.traceforum.traceforum_app.exceptions.UserNotFoundException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This class aims at computing several indicators by parsing a JSON file.
 */
public class IndicatorCalc {

    public static final int AVERAGE = 0;
    public static final int GLOBAL = 1;

    // HashMap that will contain the computed indicators
    public HashMap<String,HashMap<String,Object>> indicators = null;

    // JSON Data loaded from file
    private JSONArray json = null;

    /**
     * This method allows the user to load a JSON document and store its content in a JSONArray object.
     * @param filename
     * @throws ParseException exception thrown when a problem occurs while parsing the JSON document
     */
    public void parse(String filename) throws ParseException {
        // Initialize a JSON Parser
        JSONParser jsonParser = new JSONParser();

        FileReader reader = null;
        Object obj = null;
        try {
            // Parse the JSON data and save its content in the object obj
            reader = new FileReader(filename);
            obj = jsonParser.parse(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Cast the data to a JSONArray and store it in the json var in order to make it accessible wherever
        // in the class
        json = (JSONArray) obj;

        indicators = new HashMap<>();
    }

    /**
     * This function computes two indicators : the number of messages that a user has successfully completely
     * read and the number of messages that this user has just partially read.
     *
     * @param username username of the user we want to compute this indicator
     * @return a list of integers with firstly the number of messages partially read and secondly the number of messages
     * completely read
     * @throws UserNotFoundException exception thrown when the given user does not exist
     * @throws DataNotYetParsedException exception thrown when no JSON data has been already parsed
     */
    public List<Integer> computeMsgsIndicators(String username) throws UserNotFoundException, DataNotYetParsedException {
        int completelyRead = 0;
        int partiallyRead = 0;

        // Check that the JSON data has been loaded
        if(json != null){

            boolean userFound = false;

            // Go through the JSON data
            // Each iteration corresponds to a user's data
            for(Object obj : json){

                JSONObject parent = (JSONObject) obj;

                // Check that the current data corresponds to the given user
                if(parent.get("userName").equals(username)){

                    userFound = true;

                    // Get the needed fields
                    JSONArray msgsScrollbarDisabled = (JSONArray) parent.get("messagesOpenedWithScrollbarDisabled");
                    JSONArray readingEvents = (JSONArray) parent.get("readingEvents");
                    JSONArray msgsOpened = (JSONArray) parent.get("messagesOpened");

                    // Compute the number of completely and partially read messages
                    completelyRead = msgsScrollbarDisabled.size() + readingEvents.size();
                    partiallyRead = msgsOpened.size() - readingEvents.size();
                }
            }
            if(!userFound) throw new UserNotFoundException(username);
        }
        else{
            throw new DataNotYetParsedException();
        }

        // Generate a HashMap to store this user's data if it does not already exist
        HashMap<String,Object> resMap = new HashMap<>();
        indicators.putIfAbsent(username,resMap);

        // Store the computed indicators
        indicators.get(username).put("messagesPartiallyRead",(double) partiallyRead);
        indicators.get(username).put("messagesCompletelyRead",(double) completelyRead);

        ArrayList<Integer> res = new ArrayList<Integer>();
        res.add(partiallyRead);
        res.add(completelyRead);

        return res;
    }

    /**
     * This function computes the number of messages posted by a user on the forum.
     * @param username username of the user we want to compute this indicator
     * @return the number of messages posted on the forum for the given user
     * @throws UserNotFoundException exception thrown when the given user does not exist
     * @throws DataNotYetParsedException exception thrown when no JSON data has been already parsed
     */
    public int computePostedMsgsNb(String username) throws UserNotFoundException, DataNotYetParsedException {

        int postMsgNb = 0;

        // Check that the JSON data has been loaded
        if(json != null){

            boolean userFound = false;

            // Go through the JSON data
            // Each iteration corresponds to a user's data
            for(Object obj : json){
                JSONObject parent = (JSONObject) obj;

                // Check that the current data corresponds to the given user
                if(parent.get("userName").equals(username)){

                    userFound = true;

                    // Get the postMsg field
                    JSONArray postedMsgs = (JSONArray) parent.get("postMsg");

                    // Calculate the number of messages posted by the given user
                    postMsgNb = postedMsgs.size();             }
            }
            if(!userFound) throw new UserNotFoundException(username);
        }
        else{
            throw new DataNotYetParsedException();
        }

        // Generate a HashMap to store this user's data if it does not already exist and store the indicators
        HashMap<String,Object> resMap = new HashMap<>();
        indicators.putIfAbsent(username,resMap);
        indicators.get(username).put("postedMsgNumber",(double) postMsgNb);

        return postMsgNb;
    }

    /**
     * This function calculates the average or global reading time of a user.
     * @param username username of the user we want to compute this indicator
     * @param type whether AVERAGE (0 constant) to compute the average reading time or GLOBAL (1 constant) to calculate
     *             the global reading time of a user.
     * @return average or global reading time according to the specified type
     * @throws UserNotFoundException exception thrown when the given user does not exist
     * @throws DataNotYetParsedException exception thrown when no JSON data has been already parsed
     */
    public HashMap<Integer,Double> computeReadingTime(String username, int type) throws UserNotFoundException, DataNotYetParsedException {
        boolean userFound = false;

        HashMap<Integer,List<Double>> timePerForum = new HashMap<>();

        // Check that the JSON data has been loaded
        if(json != null){

            // Go through the JSON data
            // Each iteration corresponds to a user's data
            for(Object obj : json){

                JSONObject parent = (JSONObject) obj;

                // Check that the current data corresponds to the given user
                if(parent.get("userName").equals(username)) {

                    userFound = true;

                    // Get the readingEvents field
                    JSONArray readingEvents = (JSONArray) parent.get("readingEvents");

                    // Iterate to get the start and end dates
                    for(Object readingEvent : readingEvents){

                        JSONObject event = (JSONObject) readingEvent;

                        // Get the start and end reading events
                        JSONObject start =  (JSONObject) event.get("startReading");
                        JSONObject end =  (JSONObject) event.get("endReading");
                        int numForum = Integer.parseInt(event.get("forumNumber").toString());

                        Date dI = null;
                        Date dF = null;

                        try {
                            // Convert the date and time fields in a date format
                            dI = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(
                                    start.get("date").toString() + "T" +  start.get("time").toString()
                            );

                            dF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(
                                    end.get("date").toString() + "T" +  end.get("time").toString()
                            );
                        } catch (java.text.ParseException e) {
                            throw new RuntimeException(e);
                        }

                        // Calculate the precise reading time
                        long timeDiffInSec = (dF.getTime() - dI.getTime()) / 1000;

                        timePerForum.putIfAbsent(numForum,new ArrayList<Double>());
                        List<Double> times = timePerForum.get(numForum);
                        times.add((double) timeDiffInSec);
                        timePerForum.put(numForum,times);
                    }
                }
            }

            if(!userFound) throw new UserNotFoundException(username);
        }
        else{
            throw new DataNotYetParsedException();
        }

        // Generate a HashMap to store this user's data if it does not already exist
        HashMap<String,Object> resMap = new HashMap<>();
        indicators.putIfAbsent(username,resMap);

        HashMap<Integer,Double> finalRes = new HashMap<>();

        // Compute the global reading time per forum
        for (int key : timePerForum.keySet()) {
            List<Double> times = timePerForum.get(key);
            int eventCount = 0;
            double res = 0;
            for (Double time : times){
                eventCount++;
                res += time;
            }
            if(type == AVERAGE){
                if(eventCount != 0){
                    finalRes.put(key,res/eventCount);
                    //indicators.get(username).put("averageReadingTimeForum" + key,(double) res/eventCount);
                }
                else{
                    finalRes.put(key,0.0);
                    //indicators.get(username).put("averageReadingTimeForum" + key,0.0);
                }
            }
            else if(type == GLOBAL){
                finalRes.put(key,res);
                //indicators.get(username).put("globalReadingTimeForum" + key,(double) res);
            }
        }

        if(type == GLOBAL) indicators.get(username).put("globalReadingTimePerForum", finalRes);
        else indicators.get(username).put("averageReadingTimePerForum", finalRes);

        return finalRes;
    }

    /**
     * This method is used to save all the indicators computed since the instantiation of this class.
     */
    public void saveIndicators(){

        ObjectMapper objectMapper = new ObjectMapper();

        // Store the indicators in a JSON file called indicators.json
        File output_file = new File("generated_files/indicators.json");

        try {
            objectMapper.writeValue(output_file, indicators);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
