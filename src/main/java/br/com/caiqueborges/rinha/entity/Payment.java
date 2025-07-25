package br.com.caiqueborges.rinha.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Payment {
    private final UUID correlationId;
    private final BigDecimal amount;
    private long requestedAt;

    public Payment(UUID correlationId, BigDecimal amount, long requestedAt) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.requestedAt = requestedAt;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(long requestedAt) {
        this.requestedAt = requestedAt;
    }

    public void updateRequestedAt() {
        this.requestedAt = Instant.now().toEpochMilli();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return requestedAt == payment.requestedAt && Objects.equals(correlationId, payment.correlationId) && Objects.equals(amount, payment.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, amount, requestedAt);
    }
}
