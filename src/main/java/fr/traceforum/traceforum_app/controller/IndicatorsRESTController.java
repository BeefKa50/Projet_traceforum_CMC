package fr.traceforum.traceforum_app.controller;

import fr.traceforum.traceforum_app.data_classes.Indicators;
import fr.traceforum.traceforum_app.data_classes.User;
import fr.traceforum.traceforum_app.exceptions.DataNotYetParsedException;
import fr.traceforum.traceforum_app.exceptions.UserNotFoundException;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
public class IndicatorsRESTController {

    @PostMapping(path = "/indicators/{username}")
    @ResponseBody
    public ResponseEntity<User> computeIndicators (@PathVariable("username") String username,
                                                   @RequestBody Indicators indicators) {

        // Get all the users
        DBControl dbControl = new DBControl("jdbc:mysql://localhost:3306/traceforum", "root", "");
        ResultSet usersSet = dbControl.getUsers();

        // Check that the given user exists
        boolean userExists = false;
        while (true) {
            try {
                if (!usersSet.next()) break;
                if (usersSet.getString(1).equals(username)) userExists = true;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // If the given user don't exist return a NOT FOUND error
        if (!userExists) return new ResponseEntity(HttpStatus.NOT_FOUND);

        // Check that the raw JSON data exists
        try {
            FileReader reader = new FileReader("generated_files/traceforum_data.json");
        } catch (FileNotFoundException e) {

            // If there is no JSON data in directory generate it
            dbControl.generateJSON();
            e.printStackTrace();
        }

        // Load the JSON data
        IndicatorCalc indicatorCalc = new IndicatorCalc();
        try {
            indicatorCalc.parse("generated_files/traceforum_data.json");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // Iterate on the given indicator list
        for (String indicator : indicators.getIndicators()) {
            try {

                // Determine the kind of indicator requested and compute it

                if (indicator.equals("postStats")) {
                    indicatorCalc.computePostedMsgsNb(username);
                } else if (indicator.equals("readingCompletionStats")) {
                    indicatorCalc.computeMsgsIndicators(username);
                } else if (indicator.equals("averageReadingTime")) {
                    indicatorCalc.computeReadingTime(username, IndicatorCalc.AVERAGE);
                } else if (indicator.equals("globalReadingTime")) {
                    indicatorCalc.computeReadingTime(username, IndicatorCalc.GLOBAL);
                } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            } catch (UserNotFoundException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (DataNotYetParsedException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // Save the computed indicators in a JSON file
        indicatorCalc.saveIndicators();

        // Return the indicators to the client
        return new ResponseEntity(indicatorCalc.indicators, HttpStatus.OK);
    }
}
