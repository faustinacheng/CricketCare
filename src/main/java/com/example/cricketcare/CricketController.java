package com.example.cricketcare;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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

        @GetMapping("/setup")
        public String setup(Model model) {
            model.addAttribute("setup", new SetupForm());
            return "setup";
        }

        @PostMapping("/setup")
        public String greetingSubmit(@ModelAttribute SetupForm setup, Model model) {
            System.out.println(setup.getJson());
            model.addAttribute("setup", setup);
            return "setup_success";
        }

}
