package academy.backend.market_pulse.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.stream.Stream;

import academy.backend.market_pulse.filter.ByTypeFilter;
import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;
import academy.backend.market_pulse.repository.InstrumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListCommandTest {

    private final Instrument low = new Stock("AAPL", "Apple", Currency.USD, "Tech", new BigDecimal("5"));
    private final Instrument middle = new Stock("SBER", "Sber", Currency.RUB, "Bank", new BigDecimal("10.00"));
    private final Instrument high = new Stock("SAP", "SAP", Currency.EUR, "Software", new BigDecimal("15"));
    private final Instrument bond = new Bond("BOND1", "Bond", Currency.RUB, new BigDecimal("10"), 2030);
    private final Instrument etf = new Etf("ETF1", "Fund", Currency.USD, "Index");
    private final Instrument[] instruments = {low, middle, high, bond, etf};

    @Test
    void listsAllInRepositoryOrderWithoutFilter() {
        assertOutput(new String[0], instruments);
    }

    @Test
    void filtersByType() {
        assertOutput(new String[]{"--type", "BOND"}, bond);
    }

    @Test
    void typeUsesDeclaredCodeRatherThanClassName() {
        Instrument custom = new Instrument("CUSTOM", "Custom", Currency.USD) {
            @Override
            public String getDescription() {
                return "Custom instrument";
            }

            @Override
            public String getType() {
                return "BOND";
            }
        };
        assertTrue(new ByTypeFilter("BOND").matches(custom));
        assertFalse(new ByTypeFilter("STOCK").matches(custom));
    }

    @Test
    void tickerMatchesSubstringIgnoringCase() {
        assertOutput(new String[]{"--ticker", "aP"}, low, high);
    }

    @Test
    void filtersByCurrency() {
        assertOutput(new String[]{"--currency", "USD"}, low, etf);
    }

    @Test
    void priceEqualityIgnoresDecimalScaleAndExcludesOtherTypes() {
        assertOutput(new String[]{"--price-op", "EQ", "--price", "10"}, middle);
    }

    @Test
    void priceGreaterOrEqualIncludesBoundary() {
        assertOutput(new String[]{"--price-op", "GE", "--price", "10"}, middle, high);
    }

    @Test
    void priceLessOrEqualIncludesBoundaryAndAcceptsReversedOptions() {
        assertOutput(new String[]{"--price", "10", "--price-op", "LE"}, low, middle);
    }

    @Test
    void noMatchesProducesNoOutput() {
        assertOutput(new String[]{"--ticker", "UNKNOWN"});
    }

    @Test
    void emptyRepositoryProducesNoOutput() {
        Result result = execute(new InstrumentRepository());
        assertEquals(0, result.exitCode());
        assertEquals("", result.out());
        assertEquals("", result.err());
    }

    @ParameterizedTest
    @MethodSource("conflictingCriteria")
    void rejectsEveryPairOfCriteria(String[] args) {
        assertInvalid(args);
    }

    static Stream<Arguments> conflictingCriteria() {
        String[][] criteria = {
                {"--type", "STOCK"},
                {"--ticker", "AAP"},
                {"--currency", "USD"},
                {"--price-op", "GE", "--price", "10"}
        };
        Stream.Builder<Arguments> cases = Stream.builder();
        for (int i = 0; i < criteria.length; i++) {
            for (int j = i + 1; j < criteria.length; j++) {
                String[] combined = Stream.concat(Stream.of(criteria[i]), Stream.of(criteria[j]))
                        .toArray(String[]::new);
                cases.add(Arguments.of((Object) combined));
            }
        }
        return cases.build();
    }

    @ParameterizedTest
    @MethodSource("invalidArguments")
    void rejectsIncompleteOrInvalidOptions(String[] args) {
        assertInvalid(args);
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(
                new String[]{"--price", "10"},
                new String[]{"--price-op", "GE"},
                new String[]{"--price-op", "GT", "--price", "10"},
                new String[]{"--price-op", "EQ", "--price", "abc"},
                new String[]{"--currency", "INVALID"},
                new String[]{"--ticker"}
        ).map(args -> Arguments.of((Object) args));
    }

    private void assertInvalid(String[] args) {
        Result result = execute(repository(), args);
        assertEquals(CommandLine.ExitCode.USAGE, result.exitCode());
        assertEquals("", result.out(), "Ошибочный ввод не должен запускать вывод инструментов");
        assertFalse(result.err().isBlank());
    }

    private void assertOutput(String[] args, Instrument... expected) {
        Result result = execute(repository(), args);
        assertEquals(0, result.exitCode(), result.err());
        StringBuilder output = new StringBuilder();
        for (Instrument instrument : expected) {
            output.append(instrument.getDescription()).append(System.lineSeparator());
        }
        assertEquals(output.toString(), result.out());
        assertEquals("", result.err());
    }

    private InstrumentRepository repository() {
        InstrumentRepository repository = new InstrumentRepository();
        for (Instrument instrument : instruments) {
            repository.add(instrument);
        }
        return repository;
    }

    private Result execute(InstrumentRepository repository, String... args) {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new ListCommand(repository))
                .setOut(new PrintWriter(out, true))
                .setErr(new PrintWriter(err, true));
        String[] command = Stream.concat(Stream.of("list"), Stream.of(args)).toArray(String[]::new);
        int exitCode = cli.execute(command);
        return new Result(exitCode, out.toString(), err.toString());
    }

    private record Result(int exitCode, String out, String err) {
    }
}
