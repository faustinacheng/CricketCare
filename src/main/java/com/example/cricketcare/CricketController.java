package com.example.cricketcare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


@Controller
public class CricketController {
//    String service = "http://localhost:8081/api/v1/reservation";
    String service = "https://bugyourspot-407405.uc.r.appspot.com/api/v1/reservation";
    private final RestTemplate restTemplate;
    Long clientId = -1L;
    Set<String> customFields = new HashSet<>(); // doctorID,

        @Autowired
        public CricketController(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        @GetMapping("/greeting")
        public String cricket(Model model) {
            return "greeting";
        }

        @GetMapping("/setup")
        public String setup(Model model) {
            if (clientId != -1L) {
                model.addAttribute("clientId", clientId);
                return "setup_blocked";
            }
            model.addAttribute("setup", new SetupForm());
            return "setup";
        }

        @PostMapping("/setup")
        public String setupSubmit(@ModelAttribute SetupForm setup, Model model){
            String replaced = setup.getJson().replace("\r\n", " ");

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(replaced);

                JsonNode customValuesNode = jsonNode.path("customValues");

                Iterator<String> fieldNames = customValuesNode.fieldNames();
                while (fieldNames.hasNext()) {
                    customFields.add(fieldNames.next());
                }
            } catch (Exception e) {
                return "greeting";
            }

            setup.setJson(replaced);
            model.addAttribute("setup", setup);
//            String json = setup.getJson();
            String url = service + "/createClient";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            System.out.println(setup.getJson());
            HttpEntity<String> entity = new HttpEntity<String>(setup.getJson(), headers);
            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            clientId = response.getBody();

            model.addAttribute("clientId", Long.valueOf(clientId));
//            restTemplate.postForObject(url, setup.getJson(), String.class);
            return "setup_success";
        }
        @GetMapping("/createReservation")
        public String createReservation(Model model) {
            model.addAttribute("reservation", new ReservationForm());
            return "create_reservation";
        }

        @PostMapping("/createReservation")
        public String reservationSubmit(@ModelAttribute ReservationForm reservation, Model model) {
            String replaced = reservation.getCustomValues().replace("\r\n", " ");
            reservation.setCustomValues(replaced);
            reservation.setClientId(clientId);
            model.addAttribute("reservation", reservation);
            String url = service;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            String json = "{\n" +
                    "  \"clientId\": " + reservation.getClientId() + ",\n" +
                    "  \"userId\": \"" + reservation.getUserId() + "\",\n" +
                    "  \"startTime\": \"" + reservation.getStartTime() + "\",\n" +
                    "  \"numSlots\": " + reservation.getNumSlots() + ",\n" +
                    "  \"customValues\": " + reservation.getCustomValues() + "\n" +
                    "}";
            System.out.println(json);
            HttpEntity<String> entity = new HttpEntity<String>(json, headers);
//            restTemplate.postForEntity(url, entity, Long.class);
            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            Long reservationId = response.getBody();

            model.addAttribute("reservationId", reservationId);
            return "reservation_success";
        }

        @GetMapping("/getClientInfo")
        public String getClientInfo(Model model) {
            String url = service + "/getClient?clientId=" + clientId;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Long> entity = new HttpEntity<Long>(clientId, headers);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);
            String clientInfo = response.getBody();

            model.addAttribute("clientInfo", clientInfo);
            return "client_info";
        }

        // gets all reservations for a client
        @GetMapping("/getReservations")
        public String getClientReservations(Model model){
            String url = service + "/getClientReservations?clientId=" + clientId;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Long> entity = new HttpEntity<Long>(clientId, headers);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);
            String clientInfo = response.getBody();

            model.addAttribute("clientInfo", clientInfo);
            return "reservation_info";
        }

        @GetMapping("/cancelReservation")
        public String cancelReservations(Model model) {
            model.addAttribute("reservation", new EditReservationForm());
            return "cancel_reservation";
        }

        @PostMapping("/cancelReservation")
        public String cancelSubmit(@ModelAttribute EditReservationForm reservation, Model model) {
            String url = service + "/" + reservation.getReservationId(); // Use the dynamic reservationId in the URL

            HttpHeaders headers = new HttpHeaders();
            // headers.add("Content-Type", "application/json"); // This line can be removed if not needed

            // Make a DELETE request
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

            // Handle the response based on your API's behavior
            // If the response body contains valuable information, uncomment the next line
            // model.addAttribute("clientInfo", response.getBody());

            return "cancel_success";
        }

        @GetMapping("/updateReservation")
        public String updateReservation(Model model){
            model.addAttribute("update", new UpdateForm());
            return "update_reservation";
        }
        @PostMapping("/updateReservation")
        public String updateClientReservations(@ModelAttribute UpdateForm update, Model model){
//            String url = service + "/updateClientReservations?clientId=" + clientId;
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Content-Type", "application/json");
//            HttpEntity<Long> entity = new HttpEntity<Long>(clientId, headers);
//            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);
//            String clientInfo = response.getBody();
//
//            model.addAttribute("clientInfo", clientInfo);
            String replaced = update.getJson().replace("\r\n", " ");
            update.setJson(replaced);
            model.addAttribute("update", update);
//            String json = setup.getJson();
            String url = service + "/" + update.getReservationId(); // Use the dynamic reservationId in the URL

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json"); // This line can be removed if not needed
            HttpEntity<String> entity = new HttpEntity<String>(update.getJson(), headers);
            System.out.println(update.getJson());
            // Make a DELETE request
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, String.class);


//            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);

            return "reservation_info";
        }





}
