package br.com.caiqueborges.rinha.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentsSummary(@JsonProperty("default") ProcessorSummary defaulti, ProcessorSummary fallback) {
}

