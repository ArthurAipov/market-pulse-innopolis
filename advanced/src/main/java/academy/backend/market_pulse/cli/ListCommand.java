package academy.backend.market_pulse.cli;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import academy.backend.market_pulse.filter.FilterFactory;
import academy.backend.market_pulse.filter.InstrumentFilter;
import academy.backend.market_pulse.filter.PriceFilter;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "Список инструментов")
public class ListCommand implements Callable<Integer> {

    @Option(names = "--type", description = "Фильтр по типу инструмента")
    private String type;

    @Option(names = "--ticker", description = "Фильтр по подстроке в тикере")
    private String ticker;

    @Option(names = "--currency", description = "Фильтр по валюте инструмента")
    private Currency currency;

    @Option(names = "--price-op", description = "Оператор сравнения цены: GE, LE или EQ")
    private PriceFilter.Operator priceOperator;

    @Option(names = "--price", description = "Пороговое значение цены (дивидендная доходность акции)")
    private BigDecimal price;

    private final InstrumentRepository repository;

    public ListCommand(InstrumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer call() {
        Predicate<Instrument> filter = (type == null)
                ? _ -> true
                : instrument -> instrument.getClass().getSimpleName().equalsIgnoreCase(type);

        if (currency != null) {
            filter = filter.and(instrument -> instrument.getCurrency() == currency);
        }

        List<Instrument> matched = new ArrayList<>();
        for (Instrument instrument : repository) {
            if (filter.test(instrument)) {
                matched.add(instrument);
            }
        }

        if (ticker != null) {
            matched.sort(Comparator.comparing(Instrument::getTicker));
        }

        matched.forEach(instrument -> System.out.println(instrument.getDescription()));
        return 0;
    }
}