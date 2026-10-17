package academy.backend.market_pulse.config;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.service.QuoteService;
import academy.backend.market_pulse.service.QuoteSource;

/**
 * Композиционный корень приложения: единственное место, где создаются все зависимости, и каждая —
 * ровно один раз (синглтон на время работы процесса). {@link academy.backend.market_pulse.cli.CommandLineFactory}
 * берёт готовые объекты отсюда, а не создаёт их сам — команды и их зависимости собираются в разных
 * классах, каждый со своей ответственностью.
 */
@Getter
public class AppConfig {

    private final InstrumentRepository instrumentRepository;
    private final QuoteSource quoteSource;
    private final QuoteService quoteService;

    private final Watchlist watchlist;

    public AppConfig() {
        this.instrumentRepository = new InstrumentRepository();
        this.quoteSource = new QuoteSource(sampleQuotes(instrumentRepository));
        this.quoteService = new QuoteService(quoteSource);

        this.watchlist = new Watchlist();
    }

    /**
     * Заглушка каталога и котировок: репозиторий уже содержит базовый набор (засеян в конструкторе
     * {@code InstrumentRepository}), здесь добавляются остальные инструменты и по каждому строится
     * котировка. Возвращает котировки для {@link QuoteSource}.
     */
    private static Map<String, Quote> sampleQuotes(InstrumentRepository repository) {
        Instrument sber = repository.findByTicker("SBER").orElseThrow();
        Instrument ofz = repository.findByTicker("OFZ26233").orElseThrow();
        Instrument fxus = repository.findByTicker("FXUS").orElseThrow();

        Instrument gazp = InstrumentFactories.create("STOCK", "GAZP", "Газпром", Currency.RUB);
        Instrument lkoh = InstrumentFactories.create("STOCK", "LKOH", "Лукойл", Currency.RUB);
        Instrument aapl = InstrumentFactories.create("STOCK", "AAPL", "Apple", Currency.USD);
        repository.add(gazp);
        repository.add(lkoh);
        repository.add(aapl);

        Map<String, Quote> quotes = new LinkedHashMap<>();
        quotes.put("SBER", new Quote(sber, new BigDecimal("250.00"), new BigDecimal("1.20")));
        quotes.put("GAZP", new Quote(gazp, new BigDecimal("120.50"), new BigDecimal("-0.80")));
        quotes.put("LKOH", new Quote(lkoh, new BigDecimal("7100.00"), new BigDecimal("2.30")));
        quotes.put("AAPL", new Quote(aapl, new BigDecimal("225.30"), new BigDecimal("0.50")));
        quotes.put("OFZ26233", new Quote(ofz, new BigDecimal("780.00"), new BigDecimal("-0.15")));
        quotes.put("FXUS", new Quote(fxus, new BigDecimal("6.85"), new BigDecimal("0.90")));
        return quotes;
    }
}
