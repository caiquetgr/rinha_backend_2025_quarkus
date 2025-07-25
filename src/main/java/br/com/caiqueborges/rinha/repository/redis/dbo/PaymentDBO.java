package br.com.caiqueborges.rinha.repository.redis.dbo;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@RegisterForReflection
public final class PaymentDBO {
    private final UUID correlationId;
    private final BigDecimal amount;
    private final String processorName;

    public PaymentDBO(UUID correlationId, BigDecimal amount, String processorName) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.processorName = processorName;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getProcessorName() {
        return processorName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PaymentDBO) obj;
        return Objects.equals(this.correlationId, that.correlationId) &&
                Objects.equals(this.amount, that.amount) &&
                Objects.equals(this.processorName, that.processorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, amount, processorName);
    }

    @Override
    public String toString() {
        return "PaymentDBO[" +
                "correlationId=" + correlationId + ", " +
                "amount=" + amount + ", " +
                "processorName=" + processorName + ']';
    }

}
