package academy.backend.market_pulse.filter;

import java.util.Objects;

import academy.backend.market_pulse.model.Instrument;

public class ByTypeFilter implements InstrumentFilter {

    private final String type;

    public ByTypeFilter(String type) {
        this.type = type;
    }

    @Override
    public boolean matches(Instrument instrument) {
        return Objects.equals(instrument.getType(), type);
    }
}
