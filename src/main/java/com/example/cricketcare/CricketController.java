package com.example.cricketcare;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;


@Controller
public class CricketController {
    String service = "http://localhost:8081/api/v1/reservation/";
    private final RestTemplate restTemplate;
    Long clientId = -1L;

        @Autowired
        public CricketController(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

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
        public String setupSubmit(@ModelAttribute SetupForm setup, Model model){
            String replaced = setup.getJson().replace("\r\n", " ");
            setup.setJson(replaced);
            model.addAttribute("setup", setup);
//            String json = setup.getJson();
            String url = service + "createClient";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            System.out.println(setup.getJson());
            HttpEntity<String> entity = new HttpEntity<String>(setup.getJson(), headers);
            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            clientId = response.getBody();

            model.addAttribute("clientId", clientId);
//            restTemplate.postForObject(url, setup.getJson(), String.class);
            return "setup_success";
        }

        @GetMapping("/getClientInfo")
        public String getClientInfo(Model model) {
            String url = service + "getClient?clientId=" + clientId;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Long> entity = new HttpEntity<Long>(clientId, headers);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);
            String clientInfo = response.getBody();

            model.addAttribute("clientInfo", clientInfo);
            return "client_info";
        }

}
