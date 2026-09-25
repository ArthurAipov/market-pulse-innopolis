package academy.backend.market_pulse.factory;

import java.math.BigDecimal;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstrumentFactoriesTest {

    @ParameterizedTest
    @ValueSource(strings = {"STOCK", "BOND", "ETF", "stock", "bond", "etf"})
    void registeredFactoriesPreserveInput(String type) {
        Instrument instrument = InstrumentFactories.create(type, "TEST", "Test name", Currency.EUR);
        assertEquals("TEST", instrument.getTicker());
        assertEquals("Test name", instrument.getName());
        assertEquals(Currency.EUR, instrument.getCurrency());
        assertEquals(type.toUpperCase(java.util.Locale.ROOT), instrument.getType());
    }

    @Test
    void usesDefaultsForFieldsNotCollectedByCli() {
        Stock stock = assertInstanceOf(Stock.class,
                InstrumentFactories.create("STOCK", "TEST", "Test", Currency.USD));
        assertEquals("", stock.getSector());
        assertEquals(BigDecimal.ZERO, stock.getDividendYield());

        Bond bond = assertInstanceOf(Bond.class,
                InstrumentFactories.create("BOND", "TEST", "Test", Currency.RUB));
        assertEquals(BigDecimal.ZERO, bond.getCouponRate());
        assertEquals(0, bond.getMaturityYear());

        Etf etf = assertInstanceOf(Etf.class,
                InstrumentFactories.create("ETF", "TEST", "Test", Currency.EUR));
        assertEquals("", etf.getTrackingIndex());
    }

    @Test
    void rejectsUnknownType() {
        assertThrows(IllegalArgumentException.class,
                () -> InstrumentFactories.create("UNKNOWN", "TEST", "Test", Currency.USD));
    }
}
