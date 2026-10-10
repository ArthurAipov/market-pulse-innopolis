package academy.backend.market_pulse.client;

/** Прогон общего контракта ({@link TInvestApiClientContractTestBase}) на опорной реализации. */
class HttpClientTInvestApiClientTest extends TInvestApiClientContractTestBase {

    @Override
    protected TInvestApiClient createClient(TInvestApiProperties properties) {
        return new HttpClientTInvestApiClient(properties);
    }
}
