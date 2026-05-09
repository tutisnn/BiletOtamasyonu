package com.example.ucakbiletotamasyonu.mapper;

import com.example.ucakbiletotamasyonu.dto.PaymentDto;
import com.example.ucakbiletotamasyonu.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDto paymentToDto(Payment payment) {
        if (payment == null) return null;

        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setCreateTime(payment.getCreateTime());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());

        if (payment.getReservation() != null) {
            dto.setReservationId(payment.getReservation().getId());

            if (payment.getReservation().getFlight() != null) {
                dto.setFlightNum(payment.getReservation().getFlight().getFlightNo());
            }

            if (payment.getReservation().getPassenger() != null) {
                dto.setPassengerFullName(
                        payment.getReservation().getPassenger().getFirstName() + " " +
                                payment.getReservation().getPassenger().getLastName()
                );
            }
        }

        return dto;
    }
}
