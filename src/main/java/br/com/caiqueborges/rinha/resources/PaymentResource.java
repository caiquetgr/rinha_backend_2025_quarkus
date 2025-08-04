package br.com.caiqueborges.rinha.resources;

import br.com.caiqueborges.rinha.entity.Payment;
import br.com.caiqueborges.rinha.entity.PaymentsSummary;
import br.com.caiqueborges.rinha.entity.ProcessorsHealthCheck;
import br.com.caiqueborges.rinha.service.HealthCheckService;
import br.com.caiqueborges.rinha.service.PaymentService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import java.time.Instant;

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
    public Uni<RestResponse> receivePayment(final Payment payment) {
        return Uni.createFrom()
                .item(payment)
                .flatMap(paymentService::enqueuePayment)
                .replaceWith(RestResponse.status(Status.ACCEPTED));
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
