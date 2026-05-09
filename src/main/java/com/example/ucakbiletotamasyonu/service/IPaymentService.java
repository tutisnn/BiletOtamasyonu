package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;

public interface IPaymentService {

    GenericResponse<?> createCheckoutSession(Integer reservationId);

    GenericResponse<?> confirmPayment(String sessionId);

    GenericResponse<?> cancelPayment(Integer reservationId);

    GenericResponse<?> getPaymentByReservationId(Integer reservationId);
}
