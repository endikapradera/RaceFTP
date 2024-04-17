package org.raceftp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // INICIO
    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }
}
