package br.com.caiqueborges.rinha.repository.redis;

import br.com.caiqueborges.rinha.entity.Payment;
import br.com.caiqueborges.rinha.entity.ProcessorsHealthCheck;
import br.com.caiqueborges.rinha.repository.redis.dbo.PaymentDBO;
import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.list.KeyValue;
import io.quarkus.redis.datasource.list.ReactiveListCommands;
import io.quarkus.redis.datasource.sortedset.ReactiveSortedSetCommands;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.redis.client.Command;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class PaymentRedisRepository {
    private static final String QUEUE_NAME = "payments_to_process";
    private static final String LOCK_NAME = "health_check_lock";
    private static final String HEALTH_CHECK_KEY = "health_check_key";
    private static final String NX = "NX";
    private static final String EX = "EX";
    private static final String EX_VALUE = "5";
    private static final String PAYMENTS_BY_DATE = "payments_by_date";

    private final ReactiveRedisDataSource redis;
    private final ReactiveListCommands<String, Payment> redisPaymentList;
    private final ReactiveValueCommands<String, ProcessorsHealthCheck> processorHealthCheckRedisValue;
    private final ReactiveSortedSetCommands<String, PaymentDBO> paymentSortedSet;

    public PaymentRedisRepository(ReactiveRedisDataSource redis) {
        this.redis = redis;
        this.redisPaymentList = redis.list(String.class, Payment.class);
        this.processorHealthCheckRedisValue = redis.value(ProcessorsHealthCheck.class);
        this.paymentSortedSet = redis.sortedSet(PaymentDBO.class);
    }

    public Uni<Long> enqueuePayment(Payment payment) {
        // TODO: validar otimizar enviando apenas string, sem serializar o payment
        return redisPaymentList.lpush(QUEUE_NAME, payment);
    }

    public Uni<KeyValue<String, Payment>> dequeuePayment() {
        return redisPaymentList.brpop(Duration.ofSeconds(1), QUEUE_NAME);
    }

    public Uni<List<PaymentDBO>> getPayments(long from, long to) {
        return paymentSortedSet.zrangebyscore(PAYMENTS_BY_DATE, ScoreRange.from(from, to));
    }

    // Se execute retornar nulo, não conseguiu adquirir o lock (criar uma chave-valor com expiração)
    // NX = Criar a chave somente se não existir
    // EX = Expirar a chave em EX_VALUE segundos
    public Uni<Boolean> acquireLock() {
        return redis
                .execute(Command.SET, LOCK_NAME, LOCK_NAME, NX, EX, EX_VALUE)
                .onItem().transform(Objects::nonNull)
                .onFailure().invoke(Throwable::printStackTrace);
    }

    public Uni<Void> setProcessorsHealthCheck(final ProcessorsHealthCheck phc) {
        return processorHealthCheckRedisValue.set(HEALTH_CHECK_KEY, phc);
    }

    public Uni<ProcessorsHealthCheck> getProcessorsHealthCheck() {
        return processorHealthCheckRedisValue.get(HEALTH_CHECK_KEY);
    }

    public Uni<Payment> savePayment(final Tuple2<String, Payment> tuple) {
        final Payment payment = tuple.getItem2();
        final String processorName = tuple.getItem1();
        final PaymentDBO paymentDBO = new PaymentDBO(payment.getCorrelationId(), payment.getAmount(), processorName);

        return paymentSortedSet
                .zadd(PAYMENTS_BY_DATE, payment.getRequestedAt(), paymentDBO)
                .replaceWith(payment);
    }

    public Uni<Void> purgePayments() {
        return redis.execute("DEL", PAYMENTS_BY_DATE).replaceWithVoid();
    }

}
