package br.com.caiqueborges.rinha.entity;

import java.math.BigDecimal;

public record ProcessorSummary(int totalRequests, BigDecimal totalAmount) {
}
