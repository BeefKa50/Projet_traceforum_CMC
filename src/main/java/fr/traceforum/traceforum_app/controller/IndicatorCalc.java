package fr.traceforum.traceforum_app.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IndicatorCalc {

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

    public void computeGlobalReadingTime(String username){
        if(json != null){
            for(Object obj : json){
                JSONObject parent = (JSONObject) obj;
                if(parent.get("userName").equals(username)) {
                    JSONArray readingEvents = (JSONArray) parent.get("readingEvents");

                    double globalTime = 0.0;
                    for(Object readingEvent : readingEvents){
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

                        System.out.println("Reading time : " +  timeDiffInSec);
                    }
                }
            }
        }
    }

    public static void main(String[] args){
        IndicatorCalc indicatorCalc = new IndicatorCalc();
        indicatorCalc.parse("generated_files/traceforum_data.json");
        indicatorCalc.computeGlobalReadingTime("mmay");
    }
}
