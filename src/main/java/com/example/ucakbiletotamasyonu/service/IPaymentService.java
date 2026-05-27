package com.example.ucakbiletotamasyonu.service;

import com.example.ucakbiletotamasyonu.dto.CheckoutResponseDto;
import com.example.ucakbiletotamasyonu.dto.PaymentDto;

public interface IPaymentService {

    CheckoutResponseDto createCheckoutSession(Integer reservationId);

    PaymentDto confirmPayment(String sessionId);

    PaymentDto cancelPayment(Integer reservationId);

    PaymentDto getPaymentByReservationId(Integer reservationId);
}
