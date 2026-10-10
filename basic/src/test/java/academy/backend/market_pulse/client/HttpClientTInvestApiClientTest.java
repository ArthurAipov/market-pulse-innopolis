package academy.backend.market_pulse.client;

/** Прогон общего контракта ({@link TInvestApiClientContractTest}) на опорной реализации. */
class HttpClientTInvestApiClientTest extends TInvestApiClientContractTest {

    @Override
    protected TInvestApiClient createClient(TInvestApiProperties properties) {
        return new HttpClientTInvestApiClient(properties);
    }
}
