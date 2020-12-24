package com.dropbox.payment.controller;

import com.dropbox.payment.service.Payment123Service;
import com.dropbox.payment.service.Payment2C2PService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
public class PaymentController {

    @Autowired
    Payment123Service payment123Service;

    @Autowired
    Payment2C2PService payment2C2PService;

    @GetMapping(value = "/ping")
    public String test() {
        log.info("PaymentService()");
        return "OK";
    }

    @PostMapping(value = "/one23/startPayRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String one23StartPayRequest(@RequestBody String jsonInput) {
        log.info(" -> 123PayRequest() ->");
        return payment123Service.one23StartPayRequest(jsonInput);
    }

    @PostMapping(value = "/one23/getStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String one23GetStatus(@RequestBody String jsonInput) {
        log.info(" -> 123GetStatus() ->");
        return payment123Service.one23GetInternalStatus(jsonInput);
    }

    @PostMapping(value = "/one23/cancel", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String one23CancelPayment(@RequestBody String jsonInput) {
        log.info(" -> 123CancelPayment() ->");
        return payment123Service.one23CancelPayment(jsonInput);
    }

    @PostMapping(value = "/one23/notification", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String one23Notification(@RequestBody String jsonInput) throws UnsupportedEncodingException {
        log.debug(" <- 123Noti() {}", jsonInput);
        return payment123Service.one23Notification(jsonInput);
    }

    // For Dev
    @PostMapping(value = "/one23/paid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String one23TestPaid(@RequestBody String jsonInput) throws UnsupportedEncodingException {
        log.debug(" <- 123TestPaid() {}", jsonInput);
        return payment123Service.one23TestPaid(jsonInput);
    }

    @PostMapping(value = "/twoC2P/startPayRequest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String twoC2PStartPayRequest(@RequestBody String jsonInput) {
        log.info(" -> 2C2PPayRequest() ->");
        return payment2C2PService.twoC2PStartPayRequest(jsonInput);
    }

    @PostMapping(value = "/twoC2P/callback/backend", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String twoC2PCallbackBackend(@RequestBody MultiValueMap<String, String> responseData) throws IOException {
        log.info(" <- 2C2PCallbackBackend() <-");
        return payment2C2PService.twoC2PCallback(responseData);
    }

    @PostMapping(value = "/twoC2P/getStatus", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String twoC2PGetStatus(@RequestBody String jsonInput) {
        log.info(" -> 2C2PGetStatus() ->");
        return payment2C2PService.twoC2PGetInternalStatus(jsonInput);
    }

    // For Dev
    @PostMapping(value = "/twoC2P/paid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String twoC2PTestPaid(@RequestBody String jsonInput) throws UnsupportedEncodingException {
        log.debug(" <- 2C2PTestPaid() {}", jsonInput);
        return payment2C2PService.twoC2PTestPaid(jsonInput);
    }
}
