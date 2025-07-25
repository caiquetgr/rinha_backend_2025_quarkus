package br.com.caiqueborges.rinha.worker;

import br.com.caiqueborges.rinha.repository.redis.PaymentRedisRepository;
import br.com.caiqueborges.rinha.service.PaymentService;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.subscription.Cancellable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@Startup
@ApplicationScoped
public class PaymentWorker {

    private static final int NUM_WORKERS = 5;

    private final PaymentRedisRepository paymentRedisRepository;
    private final PaymentService paymentService;
    private final Cancellable[] workers = new Cancellable[NUM_WORKERS];

    public PaymentWorker(
            final PaymentRedisRepository paymentRedisRepository,
            final PaymentService paymentService) {
        this.paymentRedisRepository = paymentRedisRepository;
        this.paymentService = paymentService;
    }

    @PostConstruct
    void startWorkers() {
        System.out.println("Starting workers");
        for (int i = 0; i < NUM_WORKERS; i++) {
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
                .onItem().invoke((p) -> System.out.println("Worker " + workerId + "Processando pagamento " + p.value()))
                .onItem().ifNotNull().call(kv -> paymentService.processPayment(kv.value()))
                .onFailure().recoverWithNull()
                .repeat().indefinitely()
                .subscribe()
                .with(ignored -> {
                });
    }
}
