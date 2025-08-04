package br.com.caiqueborges.rinha.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class Payment {
    private final UUID correlationId;
    private final BigDecimal amount;

    public Payment(UUID correlationId, BigDecimal amount) {
        this.correlationId = correlationId;
        this.amount = amount;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(correlationId, payment.correlationId) && Objects.equals(amount, payment.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, amount);
    }
}
