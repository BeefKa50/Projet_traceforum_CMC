package fr.traceforum.traceforum_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.traceforum.traceforum_app.exceptions.DataNotYetParsedException;
import fr.traceforum.traceforum_app.exceptions.UserNotFoundException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class IndicatorCalc {

    private static final int AVERAGE = 0;
    private static final int GLOBAL = 1;

    private HashMap<String,Double> indicators = new HashMap<String, Double>();

    private JSONArray json = null;

    public void parse(String filename){
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(filename))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            json = (JSONArray) obj;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> computeMsgsIndicators(String username) throws UserNotFoundException, DataNotYetParsedException {
        int completelyRead = 0;
        int partiallyRead = 0;

        if(json != null){

            boolean userFound = false;

            for(Object obj : json){
                JSONObject parent = (JSONObject) obj;

                if(parent.get("userName").equals(username)){

                    userFound = true;
                    JSONArray msgsScrollbarDisabled = (JSONArray) parent.get("messagesOpenedWithScrollbarDisabled");
                    JSONArray readingEvents = (JSONArray) parent.get("readingEvents");
                    JSONArray msgsOpened = (JSONArray) parent.get("messagesOpened");

                    completelyRead = msgsScrollbarDisabled.size() + readingEvents.size();
                    partiallyRead = msgsOpened.size();
                }
            }
            if(!userFound) throw new UserNotFoundException(username);
        }
        else{
            throw new DataNotYetParsedException();
        }

        indicators.put("messagesPartiallyRead",(double) partiallyRead);
        indicators.put("messagesCompletelyRead",(double) completelyRead);

        ArrayList<Integer> res = new ArrayList<Integer>();
        res.add(partiallyRead);
        res.add(completelyRead);

        return res;
    }

    public int computePostedMsgsNb(String username) throws UserNotFoundException, DataNotYetParsedException {

        int postMsgNb = 0;
        if(json != null){

            boolean userFound = false;

            for(Object obj : json){
                JSONObject parent = (JSONObject) obj;

                if(parent.get("userName").equals(username)){

                    userFound = true;
                    JSONArray postedMsgs = (JSONArray) parent.get("postMsg");

                    postMsgNb = postedMsgs.size();
                }
            }
            if(!userFound) throw new UserNotFoundException(username);
        }
        else{
            throw new DataNotYetParsedException();
        }

        indicators.put("postedMsgsNumber",(double) postMsgNb);

        return postMsgNb;
    }

    public double computeReadingTime(String username, int type) throws UserNotFoundException, DataNotYetParsedException {
        double res = 0.0;
        boolean userFound = false;
        int eventCount = 0;

        if(json != null){

            for(Object obj : json){

                JSONObject parent = (JSONObject) obj;

                if(parent.get("userName").equals(username)) {

                    userFound = true;
                    JSONArray readingEvents = (JSONArray) parent.get("readingEvents");

                    for(Object readingEvent : readingEvents){

                        eventCount++;

                        JSONObject event = (JSONObject) readingEvent;
                        JSONObject start =  (JSONObject) event.get("startReading");
                        JSONObject end =  (JSONObject) event.get("endReading");
                        Date dI = null;
                        Date dF = null;

                        try {
                            dI = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(
                                    start.get("date").toString() + "T" +  start.get("time").toString()
                            );

                            dF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(
                                    end.get("date").toString() + "T" +  end.get("time").toString()
                            );
                        } catch (java.text.ParseException e) {
                            throw new RuntimeException(e);
                        }

                        long timeDiffInSec = (dF.getTime() - dI.getTime()) / 1000;

                        res += timeDiffInSec;
                    }
                }
            }

            if(!userFound) throw new UserNotFoundException(username);
        }
        else{
            throw new DataNotYetParsedException();
        }

        if(type == AVERAGE){
            res = res/eventCount;
            indicators.put(username + "AverageReadingTime",res);
        }
        else if(type == GLOBAL){
            indicators.put(username + "GlobalReadingTime",res);
        }

        return res;
    }

    public void saveComputedIndicators(){
        ObjectMapper objectMapper = new ObjectMapper();
        File output_file = new File("generated_files/indicators.json");

        try {
            objectMapper.writeValue(output_file, indicators);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public static void main(String[] args){
        IndicatorCalc indicatorCalc = new IndicatorCalc();
        indicatorCalc.parse("generated_files/traceforum_data.json");
        try {
            System.out.println("Reading time : " + indicatorCalc.computeReadingTime("mmay",GLOBAL));
            System.out.println("Average reading time : " + indicatorCalc.computeReadingTime("mmay",
                    AVERAGE));
            ArrayList<Integer> res = (ArrayList<Integer>) indicatorCalc.computeMsgsIndicators("mmay");
            System.out.println("Partially read : " + res.get(0));
            System.out.println("Completely read : " + res.get(1));

            System.out.println("Posted messages : " + indicatorCalc.computePostedMsgsNb("mmay"));
            indicatorCalc.saveComputedIndicators();

        } catch (UserNotFoundException | DataNotYetParsedException e) {
            throw new RuntimeException(e);
        }
    }
}
