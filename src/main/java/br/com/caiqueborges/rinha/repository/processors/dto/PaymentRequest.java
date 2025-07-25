package br.com.caiqueborges.rinha.repository.processors.dto;

import br.com.caiqueborges.rinha.entity.Payment;
import br.com.caiqueborges.rinha.utils.DateHelper;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID correlationId,
        BigDecimal amount,
        String requestedAt
) {
    public static PaymentRequest fromPayment(Payment payment) {
        return new PaymentRequest(
                payment.getCorrelationId(),
                payment.getAmount(),
                DateHelper.getNowIsoUtc(payment.getRequestedAt())
        );
    }
}
