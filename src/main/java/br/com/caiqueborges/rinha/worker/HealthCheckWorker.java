package br.com.caiqueborges.rinha.worker;

import br.com.caiqueborges.rinha.repository.redis.PaymentRedisRepository;
import br.com.caiqueborges.rinha.service.HealthCheckService;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;

@Startup
@ApplicationScoped
public class HealthCheckWorker {

    private final HealthCheckService healthCheckService;
    private Cancellable worker;

    public HealthCheckWorker(
            PaymentRedisRepository paymentRedis,
            HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @PostConstruct
    void startWorker() {
        System.out.println("Starting health check worker");

        worker = healthCheckService.acquireLock()
                .onFailure().recoverWithItem(false)
                .onItem().invoke((lock) -> System.out.println("Conseguiu lock? " + lock))
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
