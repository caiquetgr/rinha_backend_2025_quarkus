package br.com.caiqueborges.rinha.service;

import br.com.caiqueborges.rinha.entity.Payment;
import br.com.caiqueborges.rinha.entity.PaymentsSummary;
import br.com.caiqueborges.rinha.entity.ProcessorSummary;
import br.com.caiqueborges.rinha.entity.ProcessorsHealthCheck;
import br.com.caiqueborges.rinha.repository.processors.PaymentProcessorDefault;
import br.com.caiqueborges.rinha.repository.processors.PaymentProcessorFallback;
import br.com.caiqueborges.rinha.repository.processors.dto.PaymentRequest;
import br.com.caiqueborges.rinha.repository.redis.PaymentRedisRepository;
import br.com.caiqueborges.rinha.repository.redis.dbo.PaymentDBO;
import br.com.caiqueborges.rinha.utils.DateHelper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@ApplicationScoped
public class PaymentService {

    private static final String FALLBACK = "FALLBACK";
    private static final String DEFAULT = "DEFAULT";

    private final PaymentRedisRepository paymentRedisRepository;
    private final HealthCheckService healthCheckService;
    private final PaymentProcessorDefault paymentProcessorDefault;
    private final PaymentProcessorFallback paymentProcessorFallback;

    @Inject
    public PaymentService(
            PaymentRedisRepository paymentRedisRepository,
            HealthCheckService healthCheckService,
            @RestClient PaymentProcessorDefault paymentProcessorDefault,
            @RestClient PaymentProcessorFallback paymentProcessorFallback
    ) {
        this.paymentRedisRepository = paymentRedisRepository;
        this.healthCheckService = healthCheckService;
        this.paymentProcessorDefault = paymentProcessorDefault;
        this.paymentProcessorFallback = paymentProcessorFallback;
    }

    public Uni<Void> enqueuePayment(final Payment payment) {
        return paymentRedisRepository.enqueuePayment(payment).replaceWithVoid();
    }

    public Uni<PaymentsSummary> getPaymentSummary(String from, String to) {
        long fromMilli = DateHelper.parseIsoUtcToEpochMillis(from);
        long toMilli = DateHelper.parseIsoUtcToEpochMillis(to);

        return paymentRedisRepository.getPayments(fromMilli, toMilli)
                .onItem()
                .transform(payments ->
                        new PaymentsSummary(
                                summarizeProcessor(payments, "DEFAULT"),
                                summarizeProcessor(payments, "FALLBACK")));
    }

    private ProcessorSummary summarizeProcessor(List<PaymentDBO> payments, String processorName) {
        List<PaymentDBO> filtered = payments.stream()
                .filter(p -> processorName.equals(p.getProcessorName()))
                .toList();

        BigDecimal totalAmount = filtered.stream()
                .map(PaymentDBO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalRequests = filtered.size();

        return new ProcessorSummary(totalRequests, totalAmount);
    }

    public Uni<Void> processPayment(final Payment payment) {
        return selectPaymentProcessor()
                .flatMap(processorName -> sendPaymentToProcessor(processorName, payment))
                .flatMap(paymentRedisRepository::savePayment)
                .replaceWithVoid()
                .onFailure().invoke(throwable -> System.out.println("Falha ao enviar pagamento: " + throwable.getMessage()))
                .onFailure().recoverWithUni(enqueuePayment(payment)); // se falhar alguma etapa, reenfileirar a mensagem
    }

    private Uni<String> selectPaymentProcessor() {
        return healthCheckService.getHealthCheckCache()
                .map(this::decideProcessorToSendPayment)
                .onItem().ifNull().failWith(() -> new IllegalStateException("Nenhum processor disponível no momento"));
    }

    // Define qual processor deve ser usado baseado em saúde do serviço e tempo de resposta mínimo,
    // e retorna o método do processor a ser chamado
    private @Nullable String decideProcessorToSendPayment(ProcessorsHealthCheck healthCheck) {
        if (healthCheck.isDefaultOnlineAndFaster()) {
            return DEFAULT;
        }

        if (healthCheck.isDefaultFailingButFallbackNotFailing()) {
            return FALLBACK;
        }

        if (healthCheck.isFallbackAtLeast300msFaster()) {
            return FALLBACK;
        }

        if (healthCheck.areBothFailing()) {
            return null;
        }

        return DEFAULT;
    }

    private Uni<Tuple2<String, Payment>> sendPaymentToProcessor(String processorName, Payment payment) {
        Function<PaymentRequest, Uni<RestResponse<Void>>> processor =
                DEFAULT.equals(processorName) ? paymentProcessorDefault::processPayment : paymentProcessorFallback::processPayment;

        return processor
                .apply(PaymentRequest.fromPayment(payment))
                .onItem().transformToUni(response -> {
                    if ((response.getStatus() >= 200 && response.getStatus() < 300) || response.getStatus() == 422) {
                        System.out.println("Aceito!");
                        return Uni.createFrom().item(Tuple2.of(processorName, payment));
                    } else {
                        return Uni.createFrom().failure(() -> new InternalServerErrorException("Falha ao enviar pagamento"));
                    }
                });
    }

}
