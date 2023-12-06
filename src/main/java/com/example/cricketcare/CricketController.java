package com.example.cricketcare;

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
import java.util.Map;


@Controller
public class CricketController {
    String service = "http://localhost:8081/api/v1/reservation";
    private final RestTemplate restTemplate;
    Long clientId = -1L;

        @Autowired
        public CricketController(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        @GetMapping("/greeting")
        public String cricket(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
            model.addAttribute("/name", name);
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

//            String url = service + "/cancelReservation?reservationId=" + reservationId;
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Content-Type", "application/json");
//            HttpEntity<Long> entity = new HttpEntity<Long>(Long.valueOf(reservationId), headers);
//            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//            String clientInfo = response.getBody();
//
//            model.addAttribute("clientInfo", clientInfo);
//            return "cancel_success";
//        }
//        @DeleteMapping("/cancelReservation")
//        public String cancelSubmit(@ModelAttribute EditReservationForm reservation, Model model){
//            Long reservationId = reservation.getReservationId();
//            String url = service + "/cancelReservation/" + reservationId;
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Content-Type", "application/json");
//            HttpEntity<Long> entity = new HttpEntity<Long>(reservationId, headers);
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null,
//                                                                String.class, reservationId);
//            String clientInfo = response.getBody();
//
//            model.addAttribute("clientInfo", clientInfo);
//            return "cancel_success";
//        }
        @DeleteMapping("/cancelReservation/{reservationId}")
        public String cancelSubmit(@PathVariable Long reservationId, Model model) {
            //System.out.println(reservationId);
            String url = service + "/1";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, entity, String.class);

            String clientInfo = response.getBody();
            /**Map<String, String> params = new HashMap<String, String>();
            params.put("reservationId", "1");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete(url, params); */
//            model.addAttribute("clientInfo", clientInfo);

            return "cancel_success";
        }

        @PutMapping("/updateReservations")
        public String updateClientReservations(Model model){
            String url = service + "/updateClientReservations?clientId=" + clientId;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Long> entity = new HttpEntity<Long>(clientId, headers);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);
            String clientInfo = response.getBody();

            model.addAttribute("clientInfo", clientInfo);
            return "reservation_info";
        }

        @PostMapping("/updateReservation")
        public String updateReservation(Model model){
            String url = service + "/updateReservation?" + clientId;
            return "update_reservation";
        }



}
