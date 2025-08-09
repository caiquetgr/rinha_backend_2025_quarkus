package br.com.caiqueborges.rinha.worker;

import br.com.caiqueborges.rinha.repository.redis.PaymentRedisRepository;
import br.com.caiqueborges.rinha.service.HealthCheckService;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@Startup
@ApplicationScoped
public class HealthCheckWorker {

    private final HealthCheckService healthCheckService;
    private final String serviceMode;
    private Cancellable worker;

    public HealthCheckWorker(
            PaymentRedisRepository paymentRedis,
            HealthCheckService healthCheckService,
            @ConfigProperty(name = "service.mode") final String serviceMode) {
        this.healthCheckService = healthCheckService;
        this.serviceMode = serviceMode;
    }

    @PostConstruct
    void startWorker() {
        if (!"worker".equals(serviceMode)) {
            System.out.println("No health-check workers needed");
            return;
        }

        System.out.println("Starting health check worker");

        worker = healthCheckService.acquireLock()
                .onFailure().recoverWithItem(false)
                .onItem().call(acquiredLock -> acquiredLock ? healthCheckService.updateHealthCheck() : Uni.createFrom().voidItem())
                .onFailure().invoke(() -> System.out.println("Falha ao atualizar health-check"))
                .onFailure().recoverWithNull()
                .onItem().delayIt().by(Duration.ofSeconds(1))
                .repeat().indefinitely()
                .subscribe()
                .with(ignored -> {
                });

        System.out.println("Started health check worker");
    }

    @PreDestroy
    void stopWorker() {
        System.out.println("Cancelling health check worker");

        if (worker != null) {
            worker.cancel();
        }

        System.out.println("Cancelled health check worker");
    }
}
