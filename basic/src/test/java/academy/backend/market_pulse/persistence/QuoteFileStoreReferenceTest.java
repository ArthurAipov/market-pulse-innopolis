package academy.backend.market_pulse.persistence;

/**
 * Прогон общего контракта ({@link QuoteFileStoreContractTest}) на эталонной реализации — сверка
 * для семинариста, студентам заранее не показывать (см. «План семинара.md», семинар 7, этап 5).
 */
class QuoteFileStoreReferenceTest extends QuoteFileStoreContractTest {

    @Override
    protected QuoteFileStore createStore() {
        return new QuoteFileStoreReference();
    }
}
