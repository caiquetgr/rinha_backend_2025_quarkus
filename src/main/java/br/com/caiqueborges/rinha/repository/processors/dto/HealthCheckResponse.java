package br.com.caiqueborges.rinha.repository.processors.dto;

public record HealthCheckResponse (
        boolean failing,
        int minResponseTime
){
}
