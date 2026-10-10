package academy.backend.market_pulse.client;

import java.time.Duration;

/**
 * Настройки клиента T-Invest API — вынесены в отдельный класс, а не захардкожены внутри клиента:
 * обе реализации ({@link HttpClientTInvestApiClient}, реакторная из {@code client.reactor})
 * принимают их через конструктор вместе с остальными зависимостями (см. «План семинара.md»,
 * семинар 7, этап 2). {@code host}/{@code port} указывают на локальный моковый сервер (Docker, см.
 * {@code docker/mockserver}), а не на настоящий T-Invest API.
 */
public class TInvestApiProperties {

    private final String host;
    private final int port;
    private final Duration connectTimeout;
    private final Duration requestTimeout;

    public TInvestApiProperties(String host, int port, Duration connectTimeout, Duration requestTimeout) {
        this.host = host;
        this.port = port;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    public static TInvestApiProperties localMock() {
        return new TInvestApiProperties("localhost", 1080, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }
}
