package com.example.ucakbiletotamasyonu.controller;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
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
public class PaymentController {

    @Autowired
    private IPaymentService paymentService;

    @PostMapping("/checkout/{reservationId}")
    public GenericResponse<?> createCheckoutSession(@PathVariable Integer reservationId) {
        return paymentService.createCheckoutSession(reservationId);
    }

    @GetMapping("/success")
    public GenericResponse<?> confirmPayment(@RequestParam("session_id") String sessionId) {
        return paymentService.confirmPayment(sessionId);
    }

    @GetMapping("/cancel")
    public GenericResponse<?> cancelPayment(@RequestParam("reservationId") Integer reservationId) {
        return paymentService.cancelPayment(reservationId);
    }

    @GetMapping("/reservation/{reservationId}")
    public GenericResponse<?> getPaymentByReservationId(@PathVariable Integer reservationId) {
        return paymentService.getPaymentByReservationId(reservationId);
    }
}
