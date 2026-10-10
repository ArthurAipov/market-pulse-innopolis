package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.audit.UserActionLog;
import academy.backend.market_pulse.cli.*;
import academy.backend.market_pulse.client.HttpClientTInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiProperties;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PriceHistoryStoreReference;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.persistence.FileBackedQuoteRepository;
import academy.backend.market_pulse.persistence.QuoteFileStore;
import academy.backend.market_pulse.persistence.QuoteFileStoreReference;
import academy.backend.market_pulse.persistence.QuoteFileWatcher;
import academy.backend.market_pulse.persistence.WatchlistStore;
import academy.backend.market_pulse.persistence.WatchlistStoreReference;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.service.QuoteService;
import academy.backend.market_pulse.service.QuoteSource;
import picocli.CommandLine;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Точка входа CLI. Команды регистрируются вручную через {@code addSubcommand}, а не через
 * атрибут {@code subcommands} аннотации {@code @Command}: у команд нет конструктора без
 * аргументов, picocli не смог бы создать их самостоятельно по одному классу — каждой нужен уже
 * готовый {@code repository}.
 */
public class Main {

    public static void main(String[] args) {
        InstrumentRepository repository = new InstrumentRepository();
        QuoteSource source = new QuoteSource(sampleQuotes(repository));
        QuoteService quoteService = new QuoteService(source);

        // Пользовательские действия (добавление инструмента, watchlist, обновление котировок,
        // в будущем — покупка/продажа) — партиционированный по дате лог, независимый от остального
        // хранилища.
        UserActionLog actionLog = new UserActionLog(Path.of("logs"));

        // Watchlist переживает перезапуск CLI — читается при старте, дописывается на диск сразу
        // при изменении (см. WatchCommand).
        WatchlistStore watchlistStore = new WatchlistStoreReference();
        Path watchlistPath = Path.of("data", "watchlist.json");
        Watchlist watchlist = new Watchlist(watchlistStore.load(watchlistPath));

        // Семинар 7: клиент к instruments-service (моковый сервер в Docker, см.
        // docker/mockserver) — настройки отдельным классом, клиент создаётся один раз здесь и
        // дальше инжектится через конструктор, а не создаётся заново на каждый вызов.
        TInvestApiClient apiClient = new HttpClientTInvestApiClient(TInvestApiProperties.localMock());

        // Семинар 7: котировки отслеживаемых инструментов — на диске, а не только в памяти
        // процесса. QuoteFileStoreReference — эталон; студенты на этапе 6 пишут свой.
        QuoteFileStore quoteFileStore = new QuoteFileStoreReference();
        FileBackedQuoteRepository fileBackedQuotes =
                new FileBackedQuoteRepository(quoteFileStore, Path.of("data", "quotes.txt"));
        new QuoteFileWatcher(fileBackedQuotes).start();

        // История цен — для будущей статистики: новая точка (новый файл) на каждое update.
        PriceHistoryStore historyStore = new PriceHistoryStoreReference();
        Path historyDirectory = Path.of("data", "history");

        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository, actionLog))
                .addSubcommand(new ListCommand(repository))
                .addSubcommand(new WatchCommand(watchlist, watchlistStore, watchlistPath, actionLog))
                .addSubcommand(new QuoteCommand(quoteService))
                .addSubcommand(new StatsCommand(source))
                .addSubcommand(new CompareCommand(quoteService))
                .addSubcommand(new MoversCommand(source))
                .addSubcommand(new UpdateCommand(apiClient, watchlist, fileBackedQuotes,
                        historyStore, historyDirectory, actionLog));

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
}
