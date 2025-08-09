package br.com.caiqueborges.rinha.resources;

import br.com.caiqueborges.rinha.entity.Payment;
import br.com.caiqueborges.rinha.entity.PaymentsSummary;
import br.com.caiqueborges.rinha.entity.ProcessorsHealthCheck;
import br.com.caiqueborges.rinha.service.HealthCheckService;
import br.com.caiqueborges.rinha.service.PaymentService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/")
public class PaymentResource {

    private final PaymentService paymentService;
    private final HealthCheckService healthCheckService;

    @Inject
    public PaymentResource(PaymentService paymentService, HealthCheckService healthCheckService) {
        this.paymentService = paymentService;
        this.healthCheckService = healthCheckService;
    }

    @Path("/payments")
    @POST
    public Uni<Response> receivePayment(final Payment payment) {
        paymentService.enqueuePayment(payment)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .subscribe()
                .with(
                        result -> {},
                        failure -> System.err.println("Erro ao enfileirar pagamento: " + payment)
                );

        return Uni.createFrom().item(Response.accepted().build());
    }

    @Path("/payments-summary")
    @GET
    public Uni<PaymentsSummary> getPaymentsSummary(
            @RestQuery final String from,
            @RestQuery final String to) {
        return paymentService.getPaymentSummary(from, to);
    }

    @Path("/purge-payments")
    @POST
    public Uni<Void> purgePayments() {
        return paymentService.purgePayments();
    }

    @Path("/health-check-cache")
    @GET
    public Uni<ProcessorsHealthCheck> getCache() {
        return healthCheckService.getHealthCheckCache();
    }
}
