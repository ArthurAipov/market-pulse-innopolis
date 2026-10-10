package academy.backend.market_pulse.config;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Настройки клиента T-Invest API — вынесены в отдельный класс, а не захардкожены внутри клиента:
 * обе реализации ({@code HttpClientTInvestApiClient}, реакторная) принимают их через конструктор
 * вместе с остальными зависимостями (см. «План семинара.md», семинар 7, этап 2). {@code host}/
 * {@code port} указывают на локальный моковый сервер (Docker, см. {@code docker/mockserver}), а не
 * на настоящий T-Invest API.
 */
@Getter
@RequiredArgsConstructor
public class TInvestApiProperties {

    private final String host;
    private final int port;
    private final Duration connectTimeout;
    private final Duration requestTimeout;

    public static TInvestApiProperties localMock() {
        return new TInvestApiProperties("localhost", 1080, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }
}
