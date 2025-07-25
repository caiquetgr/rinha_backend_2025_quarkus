package br.com.caiqueborges.rinha.repository.processors;

import br.com.caiqueborges.rinha.repository.processors.dto.HealthCheckResponse;
import br.com.caiqueborges.rinha.repository.processors.dto.PaymentRequest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/payments")
@RegisterRestClient(configKey = "payment-processor-fallback")
public interface PaymentProcessorFallback {
    @GET
    @Path("/service-health")
    Uni<RestResponse<HealthCheckResponse>> healthCheck();

    @POST
    Uni<RestResponse<Void>> processPayment(PaymentRequest paymentRequest);
}
