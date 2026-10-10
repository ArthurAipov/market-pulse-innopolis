package academy.backend.market_pulse.client;

import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.client.impl.reactor.ReactorTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;

/**
 * Простой переключатель реализации клиента (см. «План семинара.md», семинар 7, этап 6) — не
 * полноценный feature-flagging фреймворк, просто `enum` и фабричный метод: вызывающему коду
 * неважно, какая реализация выбрана, обе — {@link TInvestApiClient}.
 */
public enum TInvestClientMode {
    HTTP_CLIENT,
    REACTOR;

    public static TInvestApiClient createClient(TInvestClientMode mode, TInvestApiProperties properties) {
        return switch (mode) {
            case HTTP_CLIENT -> new HttpClientTInvestApiClient(properties);
            case REACTOR -> new ReactorTInvestApiClient(properties);
        };
    }
}
