package academy.backend.market_pulse.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.client.impl.reactor.ReactorTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;

/**
 * Живой замер этапа 7 (см. «План семинара.md», семинар 7): пропускная способность блокирующего
 * клиента (поток на запрос, через пул) против реакторного (один поток событийного цикла на все
 * запросы) при растущем числе одновременных запросов к моковому серверу. Требует поднятого
 * мокового сервера (Docker, см. {@code docker/mockserver}) — без него оба варианта просто упадут с
 * {@code TInvestApiException}.
 *
 * <p>Пул для блокирующего варианта создаётся размером ровно в {@code concurrentRequests} —
 * иначе сравнение было бы нечестным (блокирующий клиент и так проигрывает на большом числе
 * запросов из-за накладных на поток, но при искусственно малом пуле проиграл бы ещё и просто из-за
 * нехватки потоков, а не из-за самой блокирующей природы I/O).
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ClientThroughputBenchmark {

    @Param({"10", "100"})
    private int concurrentRequests;

    private HttpClientTInvestApiClient blockingClient;
    private ReactorTInvestApiClient reactorClient;
    private ExecutorService blockingPool;

    @Setup
    public void setup() {
        TInvestApiProperties properties = TInvestApiProperties.localMock();
        blockingClient = new HttpClientTInvestApiClient(properties);
        reactorClient = new ReactorTInvestApiClient(properties);
        blockingPool = Executors.newFixedThreadPool(concurrentRequests);
    }

    @TearDown
    public void tearDown() {
        blockingPool.shutdownNow();
        reactorClient.close();
    }

    @Benchmark
    public void blockingClient(Blackhole blackhole) throws InterruptedException {
        List<Future<?>> futures = new ArrayList<>(concurrentRequests);
        for (int i = 0; i < concurrentRequests; i++) {
            futures.add(blockingPool.submit(() -> blackhole.consume(blockingClient.fetchQuotes())));
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new IllegalStateException(e.getCause());
            }
        }
    }

    @Benchmark
    public void reactorClient(Blackhole blackhole) {
        List<CompletableFuture<?>> futures = new ArrayList<>(concurrentRequests);
        for (int i = 0; i < concurrentRequests; i++) {
            futures.add(reactorClient.fetchQuotesAsync());
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.forEach(future -> blackhole.consume(future.join()));
    }
}
