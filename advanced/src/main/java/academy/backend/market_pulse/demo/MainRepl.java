package academy.backend.market_pulse.demo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import academy.backend.market_pulse.cli.AddCommand;
import academy.backend.market_pulse.cli.CompareCommand;
import academy.backend.market_pulse.cli.HistoryCommand;
import academy.backend.market_pulse.cli.ListCommand;
import academy.backend.market_pulse.cli.MarketPulseCli;
import academy.backend.market_pulse.cli.MoversCommand;
import academy.backend.market_pulse.cli.QuoteCommand;
import academy.backend.market_pulse.cli.SearchCommand;
import academy.backend.market_pulse.cli.StatsCommand;
import academy.backend.market_pulse.cli.WatchCommand;
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

/**
 * Точка входа интерактивного режима (REPL). В отличие от {@link Main}, команды не передаются
 * аргументами командной строки, а вводятся пользователем в консоль по одной за раз.
 *
 * <p>Поддерживаются те же команды, что и в CLI ({@code search}, {@code add}, {@code list}),
 * а также команды {@code help} и {@code exit}. Регистрация подкоманд выполняется вручную через
 * {@code addSubcommand} — так же, как в {@link Main}.</p>
 */
public class MainRepl {

    public static void main(String[] args) {
        InstrumentRepository repository = new InMemoryInstrumentRepository();
        var quouteSource = new QuoteSource(sampleQuotes(repository));
        QuoteService quoteService = new QuoteService(quouteSource);
        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new SearchCommand(repository))
                .addSubcommand(new AddCommand(repository))
                .addSubcommand(new ListCommand(repository))
                .addSubcommand(new WatchCommand(new Watchlist()))
                .addSubcommand(new QuoteCommand(quoteService))
                .addSubcommand(new StatsCommand(quouteSource))
                .addSubcommand(new CompareCommand(quoteService))
                .addSubcommand(new MoversCommand(quouteSource))
                .addSubcommand(new HistoryCommand(samplePriceHistory()));

        printHelp();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("market-pulse> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                String[] tokens = input.split("\\s+");
                String command = tokens[0].toLowerCase(Locale.ROOT);
                String[] commandArgs = tokens.length >= 1
                        ? Arrays.copyOfRange(tokens, 0, tokens.length)
                        : new String[0];

                switch (command) {
                    case "exit", "quit" -> {
                        System.out.println("До свидания!");
                        return;
                    }
                    case "help" -> printHelp();
                    case "search", "add", "list", "watch", "quote", "stats", "compare", "movers", "history" -> cli.execute(commandArgs);
                    default -> System.out.println("Неизвестная команда: " + command
                            + " (используйте help для списка команд)");
                }
            }
        }
    }

    private static void printHelp() {
        System.out.println("Интерактивный режим market-pulse. Доступные команды:");
        System.out.println("  search <тикер>               — поиск инструмента по тикеру");
        System.out.println("  add <тип> <тикер> <название> <валюта> — добавление инструмента");
        System.out.println("  list [--type ...] [--ticker ...] [--currency ...] [--price-op ...] [--price ...] — список инструментов");
        System.out.println("  quote <тикер> [<тикер> ...]  — получить котировки по одному или нескольким тикерам");
        System.out.println("  help                         — показать эту справку");
        System.out.println("  exit | quit                  — завершить работу");
        System.out.println();
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