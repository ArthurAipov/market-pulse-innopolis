package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

import java.math.BigDecimal;

public class StockFactory implements InstrumentFactory {

    static {
        InstrumentFactories.register("STOCK", new StockFactory());
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        return new Stock(
                ticker,
                name,
                currency,
                "",
                BigDecimal.ZERO
        );
    }
}
