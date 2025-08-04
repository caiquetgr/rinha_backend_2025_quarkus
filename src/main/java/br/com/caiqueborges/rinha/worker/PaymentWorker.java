package br.com.caiqueborges.rinha.worker;

import br.com.caiqueborges.rinha.repository.redis.PaymentRedisRepository;
import br.com.caiqueborges.rinha.service.PaymentService;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Startup
@ApplicationScoped
public class PaymentWorker {


    private final PaymentRedisRepository paymentRedisRepository;
    private final PaymentService paymentService;
    private final Cancellable[] workers;
    private final int numWorkers;

    public PaymentWorker(
            final PaymentRedisRepository paymentRedisRepository,
            final PaymentService paymentService,
            @ConfigProperty(name = "payment-workers.size") final int numWorkers) {
        this.paymentRedisRepository = paymentRedisRepository;
        this.paymentService = paymentService;
        this.numWorkers = numWorkers;
        this.workers = new Cancellable[numWorkers];
    }

    @PostConstruct
    void startWorkers() {
        System.out.println("Starting workers");
        for (int i = 0; i < numWorkers; i++) {
            System.out.println("Starting worker " + i);
            this.workers[i] = pollLoop(i);
        }
        System.out.println("Workers started");
    }

    @PreDestroy
    void stopWorkers() {
        for (var worker : workers) {
            if (worker != null) {
                worker.cancel();
            }
        }
        System.out.println("Stopped workers");
    }

    private Cancellable pollLoop(int workerId) {
        return paymentRedisRepository.dequeuePayment()
                .onItem().ifNotNull().call(payment -> paymentService.processPayment(payment))
                .onFailure().recoverWithNull()
                .repeat().indefinitely()
                .subscribe()
                .with(ignored -> {
                });
    }
}
