package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.service.IPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController extends RestBaseController {

    @Autowired
    private IPaymentService paymentService;

    @PostMapping("/checkout/{reservationId}")
    public RootEntity<?> createCheckoutSession(@PathVariable Integer reservationId) {
        return ok(paymentService.createCheckoutSession(reservationId));
    }

    @GetMapping("/success")
    public RootEntity<?> confirmPayment(@RequestParam("session_id") String sessionId) {
        return ok(paymentService.confirmPayment(sessionId));
    }

    @GetMapping("/cancel")
    public RootEntity<?> cancelPayment(@RequestParam("reservationId") Integer reservationId) {
        return ok(paymentService.cancelPayment(reservationId));
    }

    @GetMapping("/reservation/{reservationId}")
    public RootEntity<?> getPaymentByReservationId(@PathVariable Integer reservationId) {
        return ok(paymentService.getPaymentByReservationId(reservationId));
    }
}
