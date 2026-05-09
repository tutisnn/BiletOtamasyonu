package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.ResponseMessage.Constants;
import com.example.ucakbiletotamasyonu.ResponseMessage.GenericResponse;
import com.example.ucakbiletotamasyonu.dto.CheckoutResponseDto;
import com.example.ucakbiletotamasyonu.enums.PaymentStatus;
import com.example.ucakbiletotamasyonu.enums.ReservationStatus;
import com.example.ucakbiletotamasyonu.enums.SeatStatus;
import com.example.ucakbiletotamasyonu.mapper.PaymentMapper;
import com.example.ucakbiletotamasyonu.model.Payment;
import com.example.ucakbiletotamasyonu.model.Reservation;
import com.example.ucakbiletotamasyonu.repository.PaymentRepository;
import com.example.ucakbiletotamasyonu.repository.ReservationRepository;
import com.example.ucakbiletotamasyonu.repository.SeatRepository;
import com.example.ucakbiletotamasyonu.service.IPaymentService;
import com.example.ucakbiletotamasyonu.service.ITicketService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class PaymentServiceImpl implements IPaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private ITicketService ticketService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    public GenericResponse<?> createCheckoutSession(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return GenericResponse.error("Cancelled reservation cannot be paid");
        }

        Optional<Payment> existingPayment = paymentRepository.findByReservation(reservation);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.SUCCESS) {
            return GenericResponse.error("This reservation is already paid");
        }

        Payment payment = existingPayment.orElseGet(Payment::new);
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);

        Stripe.apiKey = stripeSecretKey;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl + "?reservationId=" + reservation.getId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("try")
                                                    .setUnitAmount(toStripeAmount(reservation.getTotalPrice()))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Flight Reservation #" + reservation.getId())
                                                                    .setDescription("Flight ticket payment")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("reservationId", reservation.getId().toString())
                    .build();

            Session session = Session.create(params);

            payment.setStripeSessionId(session.getId());
            if (session.getPaymentIntent() != null) {
                payment.setStripePaymentIntentId(session.getPaymentIntent());
            }

            Payment savedPayment = paymentRepository.save(payment);

            CheckoutResponseDto responseDto = new CheckoutResponseDto();
            responseDto.setReservationId(reservation.getId());
            responseDto.setPaymentId(savedPayment.getId());
            responseDto.setSessionId(session.getId());
            responseDto.setSessionUrl(session.getUrl());
            responseDto.setStatus(savedPayment.getStatus().name());

            return GenericResponse.success(responseDto);
        } catch (StripeException e) {
            return GenericResponse.error("Stripe checkout session could not be created: " + e.getMessage());
        }
    }

    @Override
    public GenericResponse<?> confirmPayment(String sessionId) {
        Stripe.apiKey = stripeSecretKey;

        try {
            Session session = Session.retrieve(sessionId);

            Payment payment = paymentRepository.findByStripeSessionId(sessionId).orElse(null);
            if (payment == null) {
                return GenericResponse.error(Constants.EMPTY_PAYMENT);
            }

            Reservation reservation = payment.getReservation();
            if (reservation == null) {
                return GenericResponse.error(Constants.EMPTY_RESERVATION);
            }

            if ("paid".equalsIgnoreCase(session.getPaymentStatus())) {
                payment.setStatus(PaymentStatus.SUCCESS);
                if (session.getPaymentIntent() != null) {
                    payment.setStripePaymentIntentId(session.getPaymentIntent());
                }
                reservation.setStatus(ReservationStatus.CONFIRMED);

                if (reservation.getSeat() != null) {
                    reservation.getSeat().setStatus(SeatStatus.SOLD);
                    seatRepository.save(reservation.getSeat());
                }

                paymentRepository.save(payment);
                reservationRepository.save(reservation);
                GenericResponse<?> ticketResponse = ticketService.createTicketFromReservation(reservation.getId());

                if (ticketResponse.getData() == null) {
                    return GenericResponse.error("Payment succeeded but ticket could not be created");
                }

                return GenericResponse.success(paymentMapper.paymentToDto(payment));
            }

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return GenericResponse.error("Payment was not completed");
        } catch (StripeException e) {
            return GenericResponse.error("Stripe payment confirmation failed: " + e.getMessage());
        }
    }

    @Override
    public GenericResponse<?> cancelPayment(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        Payment payment = paymentRepository.findByReservation(reservation).orElse(null);
        if (payment == null) {
            payment = new Payment();
            payment.setReservation(reservation);
            payment.setAmount(reservation.getTotalPrice());
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        reservation.setStatus(ReservationStatus.CANCELLED);
        if (reservation.getSeat() != null) {
            reservation.getSeat().setStatus(SeatStatus.AVAILABLE);
            seatRepository.save(reservation.getSeat());
        }
        reservationRepository.save(reservation);

        return GenericResponse.success(paymentMapper.paymentToDto(payment));
    }

    @Override
    public GenericResponse<?> getPaymentByReservationId(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return GenericResponse.error(Constants.EMPTY_RESERVATION);
        }

        return paymentRepository.findByReservation(reservation)
                .map(payment -> GenericResponse.success(paymentMapper.paymentToDto(payment)))
                .orElseGet(() -> GenericResponse.error(Constants.EMPTY_PAYMENT));
    }

    private Long toStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }
}
