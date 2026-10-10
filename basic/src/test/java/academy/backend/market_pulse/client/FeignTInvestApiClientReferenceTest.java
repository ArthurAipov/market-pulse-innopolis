package academy.backend.market_pulse.client;

/**
 * Прогон общего контракта ({@link TInvestApiClientContractTest}) на эталонной Feign-реализации —
 * сверка для семинариста, студентам заранее не показывать (см. «План семинара.md», семинар 7,
 * этап 3).
 */
class FeignTInvestApiClientReferenceTest extends TInvestApiClientContractTest {

    @Override
    protected TInvestApiClient createClient(TInvestApiProperties properties) {
        return new FeignTInvestApiClientReference(properties);
    }
}
