package fr.traceforum.traceforum_app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class DashboardController {

    String appName = "Dashboard CMC";

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        return "dashboard";
    }

}