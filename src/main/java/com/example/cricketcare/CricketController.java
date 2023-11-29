package com.example.cricketcare;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CricketController {

//        @GetMapping("/")
//        public String root(Model model) {
//            model.addAttribute("message", "Cricket Care");
//            return "welcome";
//        }

        @GetMapping("/greeting")
        public String cricket(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
            model.addAttribute("name", name);
            return "greeting";
        }
}
