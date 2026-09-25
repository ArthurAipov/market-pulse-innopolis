package academy.backend.market_pulse.filter;

import java.math.BigDecimal;
import java.util.Objects;

import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

public class ByPriceFilter implements InstrumentFilter {

    public enum Operator {
        EQ, GE, LE
    }

    private final Operator operator;
    private final BigDecimal price;

    public ByPriceFilter(Operator operator, BigDecimal price) {
        this.operator = Objects.requireNonNull(operator);
        this.price = Objects.requireNonNull(price);
    }

    @Override
    public boolean matches(Instrument instrument) {
        if (!(instrument instanceof Stock stock)) {
            return false;
        }

        int comparison = stock.getDividendYield().compareTo(price);
        return switch (operator) {
            case EQ -> comparison == 0;
            case GE -> comparison >= 0;
            case LE -> comparison <= 0;
        };
    }
}
