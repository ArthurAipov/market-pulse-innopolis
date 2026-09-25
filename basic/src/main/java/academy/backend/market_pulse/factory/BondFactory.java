package academy.backend.market_pulse.factory;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

import java.math.BigDecimal;
public class BondFactory implements InstrumentFactory {

    static {
        InstrumentFactories.register("BOND", new BondFactory());
    }

    @Override
    public Instrument create(String ticker, String name, Currency currency) {
        return new Bond(
                ticker,
                name,
                currency,
                BigDecimal.ZERO,
                0
        );
    }
}
