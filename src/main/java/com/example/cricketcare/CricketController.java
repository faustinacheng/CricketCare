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
import org.springframework.http.HttpMethod;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

@Controller
public class CricketController {
    String service = "http://localhost:8081/api/v1/reservation";
    //String service = "https://cricketcare.uc.r.appspot.com/setup";
    private final RestTemplate restTemplate;
    Long clientId = -1L;
    Boolean booked = false;
    Set<String> customFields = new HashSet<>(); // doctorID,

        @Autowired
        public CricketController(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        @GetMapping("/login")
        public String login(Model model){
            model.addAttribute("login", new LoginForm());
            return "login";
        }

        @PostMapping("/login")
        public String login(@ModelAttribute LoginForm login, Model model){
            Long clientId = Long.valueOf(login.getClientId());

            String url = service + "/getClient?clientId=" + clientId;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Long> entity = new HttpEntity<Long>(clientId, headers);

            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);
                this.clientId = clientId;
                model.addAttribute("clientId", clientId);
                return "setup_blocked";
            }
            catch (Exception e){
                this.clientId = -1L;
                model.addAttribute("login", new LoginForm());
                return "login";
            }
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
            String url = service + "/createClient";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            System.out.println(setup.getJson());
            HttpEntity<String> entity = new HttpEntity<String>(setup.getJson(), headers);
            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            clientId = response.getBody();

            model.addAttribute("clientId", Long.valueOf(clientId));
            return "setup_success";
        }
        @GetMapping("/createReservation")
        public String createReservation(Model model) {
            if (clientId == -1L) {
                return "client_info_blocked";
            }

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
            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            Long reservationId = response.getBody();

            model.addAttribute("reservationId", reservationId);
            booked = true;
            return "reservation_success";
        }

        @GetMapping("/getClientInfo")
        public String getClientInfo(Model model) {
            if (clientId == -1L) {
                return "client_info_blocked";
            }
            String url = service + "/getClient?clientId=" + clientId;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<Long> entity = new HttpEntity<Long>(clientId, headers);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, entity);
            String clientInfo = response.getBody();

            String jsonPart = clientInfo.substring(clientInfo.indexOf('{'));
            JSONObject jsonObj = new JSONObject(jsonPart);
            
            model.addAttribute("clientId", jsonObj.optInt("clientId"));
            model.addAttribute("startTime", jsonObj.optString("startTime"));
            model.addAttribute("endTime", jsonObj.optString("endTime"));
            model.addAttribute("slotLength", jsonObj.optInt("slotLength"));
            model.addAttribute("reservationsPerSlot", jsonObj.optInt("reservationsPerSlot"));

            String urlCustom = service + "/getClientCustoms?clientId=" + clientId;
            HttpHeaders headersCustom = new HttpHeaders();
            headersCustom.add("Content-Type", "application/json");
            HttpEntity<Long> entityCustom = new HttpEntity<Long>(clientId, headersCustom);
            ResponseEntity<String> responseCustom = restTemplate.getForEntity(urlCustom, String.class, entityCustom);
            String clientInfoCustom = responseCustom.getBody();

            String jsonPartCustom = clientInfoCustom.substring(clientInfoCustom.indexOf('{'));
            JSONObject jsonObjCustom = new JSONObject(jsonPartCustom);

            String customFields = jsonObjCustom.optString("customValues");
            String jsonPartCustomFields = customFields.substring(customFields.indexOf('{'));
            JSONObject jsonObjCustomFields = new JSONObject(jsonPartCustomFields);

            Map<String, Object> customValues = jsonObjCustomFields.toMap();
            model.addAttribute("customValues", customValues);
            return "client_info";
        }

        // gets all reservations for a client
        @GetMapping("/getReservations")
        public String getClientReservations(Model model){
            if (!booked) {
                return "get_reservations_blocked";
            }
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
            String url = service + "?reservationId=" + reservation.getReservationId(); // Use the dynamic reservationId in the URL
            HttpHeaders headers = new HttpHeaders();
            // Make a DELETE request
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

            return "cancel_success";
        }

        @GetMapping("/updateReservation")
        public String updateReservation(Model model){
            if (clientId == -1L) {
                return "client_info_blocked";
            }
            
            model.addAttribute("update", new UpdateForm());
            return "update_reservation";
        }
        @PostMapping("/updateReservation")
        public String updateClientReservations(@ModelAttribute UpdateForm update, Model model){
            String replaced = update.getJson().replace("\r\n", " ");
            String replaced2 = "{ \"reservationId\": " + update.getReservationId() + ", \"updateValues\": " + replaced + "}";
            update.setJson(replaced2);
            model.addAttribute("update", update);
            String url = service;

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json"); // This line can be removed if not needed
            HttpEntity<String> entity = new HttpEntity<String>(update.getJson(), headers);
            System.out.println(update.getJson());

            // Make a DELETE request
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity, String.class);

            return "update_success";
        }

        @ExceptionHandler(Exception.class)
        public String handleException(Exception e, Model model) {
            String errorString = e.getMessage();
            String jsonPart = errorString.substring(errorString.indexOf('{'));
            JSONObject jsonObj = new JSONObject(jsonPart);
            
            model.addAttribute("timestamp", jsonObj.optString("timestamp"));
            model.addAttribute("status", jsonObj.optInt("status"));
            model.addAttribute("errorMessage", jsonObj.optString("message"));
            model.addAttribute("path", jsonObj.optString("path"));
            return "error_view";
        }
}