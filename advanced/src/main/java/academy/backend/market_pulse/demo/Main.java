package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.cli.*;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.service.QuoteService;
import academy.backend.market_pulse.service.QuoteSource;
import picocli.CommandLine;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Точка входа CLI. Команды регистрируются вручную через {@code addSubcommand}, а не через
 * атрибут {@code subcommands} аннотации {@code @Command}: у команд нет конструктора без
 * аргументов, picocli не смог бы создать их самостоятельно по одному классу — каждой нужен уже
 * готовый {@code repository}.
 */
public class Main {

    public static void main(String[] args) {
        InstrumentRepository repository = new InMemoryInstrumentRepository();
        Watchlist watchlist = new Watchlist();
        QuoteSource source = new QuoteSource(sampleQuotes(repository));
        QuoteService quoteService = new QuoteService(source);

        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository))
                .addSubcommand(new ListCommand(repository))
                .addSubcommand(new WatchCommand(watchlist))
                .addSubcommand(new QuoteCommand(quoteService))
                .addSubcommand(new StatsCommand(source))
                .addSubcommand(new CompareCommand(quoteService))
                .addSubcommand(new MoversCommand(source))
                .addSubcommand(new HistoryCommand(samplePriceHistory()));

        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    /**
     * Заглушка каталога и котировок: репозиторий уже содержит базовый набор (засеян в конструкторе
     * {@code InMemoryInstrumentRepository}), здесь добавляются остальные инструменты и по каждому
     * строится котировка. Возвращает котировки для {@link QuoteSource}. Реальный сетевой источник
     * придёт на смену на семинаре 7.
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

    /**
     * Заглушка истории цен по тикерам — для команды {@code history} (скользящее среднее через
     * Gatherer API). Отдельная {@code Map}, не часть {@code QuoteSource}: истории нет у реального
     * источника, который появится лишь на семинаре 7.
     */
    private static Map<String, List<BigDecimal>> samplePriceHistory() {
        Map<String, List<BigDecimal>> history = new LinkedHashMap<>();
        history.put("SBER", List.of(
                new BigDecimal("244.00"), new BigDecimal("246.50"), new BigDecimal("245.00"),
                new BigDecimal("248.00"), new BigDecimal("250.00"), new BigDecimal("249.50"),
                new BigDecimal("251.20")));
        history.put("LKOH", List.of(
                new BigDecimal("6950.00"), new BigDecimal("7020.00"), new BigDecimal("6990.00"),
                new BigDecimal("7080.00"), new BigDecimal("7100.00"), new BigDecimal("7150.00")));
        return history;
    }
}
