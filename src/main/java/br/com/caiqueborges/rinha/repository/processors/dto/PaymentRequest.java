package br.com.caiqueborges.rinha.repository.processors.dto;

import br.com.caiqueborges.rinha.entity.Payment;
import br.com.caiqueborges.rinha.utils.DateHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRequest(
        UUID correlationId,
        BigDecimal amount,
        String requestedAt,
        @JsonIgnore long requestedAtMillis
) {
    public static PaymentRequest fromPayment(Payment payment) {
        long epochMilli = Instant.now().toEpochMilli();
        return new PaymentRequest(
                payment.getCorrelationId(),
                payment.getAmount(),
                DateHelper.getNowIsoUtc(epochMilli),
                epochMilli
        );
    }
}
