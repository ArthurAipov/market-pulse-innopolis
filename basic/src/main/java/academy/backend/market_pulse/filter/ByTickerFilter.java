package academy.backend.market_pulse.filter;

import java.util.Locale;

import academy.backend.market_pulse.model.Instrument;

public class ByTickerFilter implements InstrumentFilter {

    private final String substring;

    public ByTickerFilter(String substring) {
        this.substring = substring.toUpperCase(Locale.ROOT);
    }

    @Override
    public boolean matches(Instrument instrument) {
        return instrument.getTicker().toUpperCase(Locale.ROOT).contains(substring);
    }
}
