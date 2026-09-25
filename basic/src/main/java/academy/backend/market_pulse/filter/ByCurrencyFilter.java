package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;

public class ByCurrencyFilter implements InstrumentFilter {

    private final Currency currency;

    public ByCurrencyFilter(Currency currency) {
        this.currency = currency;
    }

    @Override
    public boolean matches(Instrument instrument) {
        return instrument.getCurrency() == currency;
    }
}
