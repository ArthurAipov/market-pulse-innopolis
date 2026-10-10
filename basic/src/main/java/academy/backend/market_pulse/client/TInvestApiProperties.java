package academy.backend.market_pulse.client;

import java.time.Duration;

/**
 * Настройки клиента T-Invest API — вынесены в отдельный класс, а не захардкожены внутри клиента:
 * клиент принимает их через конструктор вместе с остальными зависимостями (см. «План семинара.md»,
 * семинар 7, этап 2, разговор про DI). {@code baseUrl} по умолчанию указывает на локальный
 * моковый сервер (Docker, см. {@code docker/mockserver}), а не на настоящий T-Invest API.
 */
public class TInvestApiProperties {

    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration requestTimeout;

    public TInvestApiProperties(String baseUrl, Duration connectTimeout, Duration requestTimeout) {
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
    }

    public static TInvestApiProperties localMock() {
        return new TInvestApiProperties("http://localhost:1080", Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }
}
