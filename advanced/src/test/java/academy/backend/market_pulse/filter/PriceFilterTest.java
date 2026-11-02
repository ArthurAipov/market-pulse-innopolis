package academy.backend.market_pulse.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Stock;
import org.junit.jupiter.params.provider.EnumSource;
import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Etf;

/**
 * Тест фильтра по цене: пишется вживую человеком. Эталон — {@code PriceFilterReferenceTest}.
 */
class PriceFilterTest {

    // NOTICE: техника — классы эквивалентности + граничные значения. Порог один (5.0), три класса
    // для акции: ниже порога, ровно на пороге, выше порога. Результат трёх операторов (GE/LE/EQ)
    // различается ТОЛЬКО на границе и рядом с ней — именно её проверяем в первую очередь.
    // NOTICE: отдельный класс эквивалентности — не акция (облигация, ETF): фильтр не проходят никогда,
    // независимо от оператора и порога.
    // NOTICE: одна таблица вместо девяти копий теста — @ParameterizedTest + @CsvSource
    // (оператор, дивидендная доходность, ожидаемый результат); проверка — assertThat(...).isEqualTo(...)
    // из AssertJ.
    @ParameterizedTest(name = "{0}, доходность {1} -> {2}")
    @CsvSource({
            "GE, 4.9, false",
            "GE, 5.1, true",
            "LE, 4.9, true",
            "EQ, 5.00, true",
            "LE, 5.1, false",
            "EQ, 4.9, false",
            "EQ, 5.0, true",
            "GE, 5.0, true",
            "LE, 5.0, true",
            "EQ, 5.1, false"
    })
    void stockMatchesByDividendYield(PriceFilter.Operator operator, String dividendYield, boolean expected) {
        Stock stock = new Stock("SBER", "Сбер",
                Currency.RUB, "Финансы", new BigDecimal(dividendYield));
        PriceFilter filter = new PriceFilter(operator, new BigDecimal("5.0"));
        boolean actual = filter.matches(stock);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest(name = "Облигация и ETF, оператор {0}")
    @EnumSource(PriceFilter.Operator.class)
    void nonStockIsNeverMatched(PriceFilter.Operator operator) {
        PriceFilter filter = new PriceFilter(operator, new BigDecimal("5.0"));

        Bond bond = new Bond("OFZ26233", "ОФЗ",
                Currency.RUB, new BigDecimal("8.5"), 2036);

        Etf etf = new Etf( "FXUS", "FinEx",
                Currency.USD, "S&P 500");

        assertThat(filter.matches(etf)).isFalse();
        assertThat(filter.matches(bond)).isFalse();

    }

}
