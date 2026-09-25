package academy.backend.market_pulse.cli;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.filter.ByCurrencyFilter;
import academy.backend.market_pulse.filter.ByPriceFilter;
import academy.backend.market_pulse.filter.ByTickerFilter;
import academy.backend.market_pulse.filter.ByTypeFilter;
import academy.backend.market_pulse.filter.InstrumentFilter;
import academy.backend.market_pulse.filter.NoOpFilter;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "list", description = "Список инструментов")
public class ListCommand implements Callable<Integer> {

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    private Selection selection;

    @Spec
    private CommandSpec spec;

    private static class Selection {
        @Option(names = "--type", description = "Фильтр по типу инструмента (STOCK, BOND, ETF)")
        private String type;

        @Option(names = "--ticker", description = "Подстрока тикера без учёта регистра")
        private String ticker;

        @Option(names = "--currency", description = "Фильтр по валюте (${COMPLETION-CANDIDATES})")
        private Currency currency;

        @ArgGroup(exclusive = false, multiplicity = "0..1")
        private PriceSelection price;
    }

    private static class PriceSelection {
        @Option(names = "--price-op", required = true,
                description = "Сравнение доходности акции: ${COMPLETION-CANDIDATES}")
        private ByPriceFilter.Operator operator;

        @Option(names = "--price", required = true, description = "Дивидендная доходность акции")
        private BigDecimal value;
    }

    private final InstrumentRepository repository;

    public ListCommand(InstrumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer call() {
        InstrumentFilter filter = createFilter();
        for (Instrument instrument : repository) {
            if (filter.matches(instrument)) {
                spec.commandLine().getOut().println(instrument.getDescription());
            }
        }
        return 0;
    }

    private InstrumentFilter createFilter() {
        if (selection == null) {
            return new NoOpFilter();
        }
        if (selection.type != null) {
            return new ByTypeFilter(selection.type);
        }
        if (selection.ticker != null) {
            return new ByTickerFilter(selection.ticker);
        }
        if (selection.currency != null) {
            return new ByCurrencyFilter(selection.currency);
        }
        return new ByPriceFilter(selection.price.operator, selection.price.value);
    }
}
