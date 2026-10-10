package academy.backend.market_pulse.client.reactor;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiClientContractTestBase;
import academy.backend.market_pulse.client.TInvestApiProperties;

/** Прогон общего контракта ({@link TInvestApiClientContractTestBase}) на реакторной реализации. */
class ReactorTInvestApiClientTest extends TInvestApiClientContractTestBase {

    @Override
    protected TInvestApiClient createClient(TInvestApiProperties properties) {
        return new ReactorTInvestApiClient(properties);
    }
}
