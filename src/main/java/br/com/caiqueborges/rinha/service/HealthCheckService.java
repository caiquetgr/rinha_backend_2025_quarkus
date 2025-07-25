package br.com.caiqueborges.rinha.service;

import br.com.caiqueborges.rinha.entity.ProcessorsHealthCheck;
import br.com.caiqueborges.rinha.repository.processors.PaymentProcessorDefault;
import br.com.caiqueborges.rinha.repository.processors.PaymentProcessorFallback;
import br.com.caiqueborges.rinha.repository.processors.dto.HealthCheckResponse;
import br.com.caiqueborges.rinha.repository.redis.PaymentRedisRepository;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
public class HealthCheckService {

    private final PaymentRedisRepository paymentRedisRepository;
    private final PaymentProcessorDefault paymentProcessorDefault;
    private final PaymentProcessorFallback paymentProcessorFallback;

    public HealthCheckService(
            PaymentRedisRepository paymentRedisRepository,
            @RestClient PaymentProcessorDefault paymentProcessorDefault,
            @RestClient PaymentProcessorFallback paymentProcessorFallback
    ) {
        this.paymentRedisRepository = paymentRedisRepository;
        this.paymentProcessorDefault = paymentProcessorDefault;
        this.paymentProcessorFallback = paymentProcessorFallback;
    }

    @CacheResult(cacheName = "health-check-cache")
    public Uni<ProcessorsHealthCheck> getHealthCheckCache() {
        return paymentRedisRepository.getProcessorsHealthCheck()
                .onItem()
                .ifNull().continueWith(ProcessorsHealthCheck.buildEmpty());
    }

    public Uni<Boolean> acquireLock() {
        return paymentRedisRepository.acquireLock();
    }

    public Uni<Void> updateHealthCheck() {
        return Uni.combine()
                .all()
                .unis(paymentProcessorDefault.healthCheck(), paymentProcessorFallback.healthCheck())
                .with(this::mapToProcessorsHealthCheck)
                .flatMap(paymentRedisRepository::setProcessorsHealthCheck)
                .replaceWithVoid();
    }

    private ProcessorsHealthCheck mapToProcessorsHealthCheck(
            RestResponse<HealthCheckResponse> defaulti,
            RestResponse<HealthCheckResponse> fallback
    ) {
        HealthCheckResponse defaultiResponse = defaulti.getEntity();
        HealthCheckResponse fallbackResponse = fallback.getEntity();
        return new ProcessorsHealthCheck(
                defaultiResponse.failing(), defaultiResponse.minResponseTime(),
                fallbackResponse.failing(), fallbackResponse.minResponseTime()
        );
    }
}
