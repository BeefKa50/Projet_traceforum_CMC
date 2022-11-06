package fr.traceforum.traceforum_app.controller;

import fr.traceforum.traceforum_app.data_classes.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@RestController
public class RawDataRESTController {

    @GetMapping(value = "/users")
    public ArrayList<String> getUsers () {
        DBControl dbControl = new DBControl("jdbc:mysql://localhost:3306/traceforum","root","");
        ResultSet rs = dbControl.getUsers();
        ArrayList<String> users = new ArrayList<String>();
        while(true){
            try {
                if (!rs.next()) break;
                users.add(rs.getString(1));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return users;
    }

    @GetMapping(value = "/getAsJSON")
    public ArrayList<User> getAsJSON () {
        DBControl dbControl = new DBControl("jdbc:mysql://localhost:3306/traceforum","root","");
        return dbControl.generateJSON();
    }

    @GetMapping(value = "/getAsJSON/{username}")
    public ResponseEntity<User> getUserDataAsJSON (@PathVariable("username") String username) {
        DBControl dbControl = new DBControl("jdbc:mysql://localhost:3306/traceforum","root","");
        User userData = dbControl.getUserData(0,username);
        if(userData == null) return new ResponseEntity(HttpStatus.NOT_FOUND);
        else return new ResponseEntity(userData,HttpStatus.OK);
    }

}
