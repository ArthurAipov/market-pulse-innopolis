package academy.backend.market_pulse.demo;

import academy.backend.market_pulse.model.Bond;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Etf;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("JavaPrintToLogpoint")
public class StreamDemo {

    public static void main(String[] args) {
        List<Instrument> instruments = sampleInstruments();

        System.out.println("======== 1. Топ-3 облигации по купону (купон больше 8%) ========");
        System.out.println("  " + topCouponBonds(instruments));

        System.out.println("======== 2. Тикеры акций финансового сектора по алфавиту ========");
        System.out.println("  " + financialStockTickers(instruments));

        System.out.println("======== 3. Сколько акций в каждом секторе ========");
        System.out.println("  " + stocksBySector(instruments));

        System.out.println("======== 4. Средний купон по всем облигациям ========");
        System.out.println("  " + averageCoupon(instruments));

        System.out.println("======== 5. Акция с максимальной дивидендной доходностью ========");
        System.out.println("  " + bestDividendStock(instruments).map(Stock::getTicker).orElse("нет"));

        System.out.println("======== 6. Все валюты, в которых есть инструменты ========");
        System.out.println("  " + currencies(instruments));

        System.out.println("======== 7. Тикеры всех ETF через запятую ========");
        System.out.println("  " + etfTickers(instruments));

        System.out.println("======== 8. По типу → топ-3 тикера по алфавиту в каждой группе ========");
        System.out.println("  " + top3ByTickerInGroup(instruments));

        System.out.println("======== 9. Облигация с самым ранним погашением ========");
        System.out.println("  " + earliestMaturityBond(instruments).map(Bond::getTicker).orElse("нет"));

        System.out.println("======== 10. По сектору → лучшая по дивидендам в каждом ========");
        System.out.println("  " + bestDividendPerSector(instruments));
    }

    /** Задание 1. Вернуть топ-3 облигации по купону, но только с купоном больше 8%. */
    private static List<Bond> topCouponBonds(List<Instrument> instruments) {
        // TODO: отфильтруй облигации (instanceof Bond), оставь купон > 8,
        //       отсортируй по купону по убыванию и возьми первые 3.
        return List.of();
    }

    /** Задание 2. Вернуть тикеры акций финансового сектора в алфавитном порядке. */
    private static List<String> financialStockTickers(List<Instrument> instruments) {
        // TODO: отфильтруй акции (instanceof Stock), оставь сектор «Финансы»,
        //       возьми тикеры и отсортируй по алфавиту.
        return List.of();
    }

    /** Задание 3. Вернуть карту «сектор → сколько акций в нём». */
    private static Map<String, Long> stocksBySector(List<Instrument> instruments) {
        // TODO: отфильтруй акции (instanceof Stock) и сгруппируй по сектору с подсчётом.
        return Map.of();
    }

    /** Задание 4. Вернуть средний купон по всем облигациям (0, если облигаций нет). */
    private static double averageCoupon(List<Instrument> instruments) {
        // TODO: отфильтруй облигации (instanceof Bond) и посчитай средний купон.
        return 0.0;
    }

    /** Задание 5. Вернуть акцию с максимальной дивидендной доходностью (пусто, если нет акций). */
    private static Optional<Stock> bestDividendStock(List<Instrument> instruments) {
        // TODO: отфильтруй акции (instanceof Stock) и возьми максимум по доходности.
        return Optional.empty();
    }

    /** Задание 6. Вернуть список всех валют, в которых есть инструменты, без повторов. */
    private static List<Currency> currencies(List<Instrument> instruments) {
        // TODO: возьми валюту каждого инструмента и убери повторы.
        return List.of();
    }

    /** Задание 7. Вернуть тикеры всех ETF одной строкой через запятую. */
    private static String etfTickers(List<Instrument> instruments) {
        // TODO: отфильтруй ETF (instanceof Etf), возьми тикеры и соедини через «, ».
        return null;
    }

    /** Задание 8. Вернуть карту «тип → топ-3 тикера по алфавиту в группе». */
    private static Map<String, List<String>> top3ByTickerInGroup(List<Instrument> instruments) {
        // TODO: сгруппируй по типу, затем в каждой группе заново открой стрим,
        //       возьми тикеры, отсортируй и оставь первые 3.
        return Map.of();
    }

    /** Задание 9. Вернуть облигацию с самым ранним погашением (пусто, если нет облигаций). */
    private static Optional<Bond> earliestMaturityBond(List<Instrument> instruments) {
        // TODO: отфильтруй облигации (instanceof Bond) и возьми минимум по году погашения.
        return Optional.empty();
    }

    /** Задание 10. Вернуть карту «сектор → лучшая по дивидендам акция в секторе». */
    private static Map<String, Stock> bestDividendPerSector(List<Instrument> instruments) {
        // TODO: сгруппируй акции (instanceof Stock) по сектору, затем в каждой группе
        //       заново открой стрим и возьми максимум по дивидендной доходности.
        return Map.of();
    }

    /**
     * Единый каталог инструментов (50 шт.): акции в разных секторах и валютах, облигации с разными
     * купонами и сроками погашения, ETF с отслеживаемыми индексами. Набор достаточно большой, чтобы
     * задачи (топ-N, группировки, средние) давали содержательные результаты.
     */
    private static List<Instrument> sampleInstruments() {
        return List.of(
                // Акции: сектор + дивидендная доходность
                new Stock("SBER", "Сбербанк", Currency.RUB, "Финансы", new BigDecimal("6.5")),
                new Stock("VTBR", "ВТБ", Currency.RUB, "Финансы", new BigDecimal("4.2")),
                new Stock("TCSG", "Тинькофф", Currency.RUB, "Финансы", new BigDecimal("2.1")),
                new Stock("GAZP", "Газпром", Currency.RUB, "Энергетика", new BigDecimal("8.0")),
                new Stock("LKOH", "Лукойл", Currency.RUB, "Энергетика", new BigDecimal("7.2")),
                new Stock("NVTK", "Новатэк", Currency.RUB, "Энергетика", new BigDecimal("9.1")),
                new Stock("ROSN", "Роснефть", Currency.RUB, "Энергетика", new BigDecimal("7.5")),
                new Stock("TATN", "Татнефть", Currency.RUB, "Энергетика", new BigDecimal("10.2")),
                new Stock("SNGSP", "Сургутнефтегаз", Currency.RUB, "Энергетика", new BigDecimal("8.8")),
                new Stock("CHMF", "Северсталь", Currency.RUB, "Металлургия", new BigDecimal("9.5")),
                new Stock("NLMK", "НЛМК", Currency.RUB, "Металлургия", new BigDecimal("10.8")),
                new Stock("GMKN", "Норникель", Currency.RUB, "Металлургия", new BigDecimal("7.8")),
                new Stock("ALRS", "Алроса", Currency.RUB, "Добыча", new BigDecimal("8.4")),
                new Stock("PLZL", "Полюс", Currency.RUB, "Добыча", new BigDecimal("6.2")),
                new Stock("YNDX", "Яндекс", Currency.RUB, "Технологии", new BigDecimal("0.0")),
                new Stock("AAPL", "Apple", Currency.USD, "Технологии", new BigDecimal("0.5")),
                new Stock("MSFT", "Microsoft", Currency.USD, "Технологии", new BigDecimal("0.8")),
                new Stock("NVDA", "Nvidia", Currency.USD, "Технологии", new BigDecimal("0.1")),
                new Stock("GOOGL", "Alphabet", Currency.USD, "Технологии", new BigDecimal("0.0")),
                new Stock("META", "Meta Platforms", Currency.USD, "Технологии", new BigDecimal("0.4")),
                new Stock("MGNT", "Магнит", Currency.RUB, "Потребление", new BigDecimal("5.9")),
                new Stock("FIVE", "X5 Group", Currency.RUB, "Потребление", new BigDecimal("3.4")),
                new Stock("MTSS", "МТС", Currency.RUB, "Телеком", new BigDecimal("9.7")),

                // Облигации: купон + год погашения
                new Bond("OFZ26233", "ОФЗ 26233", Currency.RUB, new BigDecimal("7.5"), 2030),
                new Bond("OFZ26228", "ОФЗ 26228", Currency.RUB, new BigDecimal("9.8"), 2031),
                new Bond("OFZ29006", "ОФЗ 29006", Currency.RUB, new BigDecimal("11.2"), 2035),
                new Bond("SU26245", "ОФЗ 26245", Currency.RUB, new BigDecimal("8.6"), 2032),
                new Bond("RUS30", "Россия 2030", Currency.USD, new BigDecimal("5.2"), 2030),
                new Bond("OFZ26215", "ОФЗ 26215", Currency.RUB, new BigDecimal("8.2"), 2027),
                new Bond("OFZ26221", "ОФЗ 26221", Currency.RUB, new BigDecimal("9.4"), 2029),
                new Bond("OFZ26224", "ОФЗ 26224", Currency.RUB, new BigDecimal("7.8"), 2033),
                new Bond("OFZ29011", "ОФЗ 29011", Currency.RUB, new BigDecimal("10.4"), 2038),
                new Bond("OFZ26218", "ОФЗ 26218", Currency.RUB, new BigDecimal("7.2"), 2036),
                new Bond("RUS28", "Россия 2028", Currency.USD, new BigDecimal("4.8"), 2028),
                new Bond("RUS29", "Россия 2029", Currency.USD, new BigDecimal("5.1"), 2029),
                new Bond("RUS32", "Россия 2032", Currency.USD, new BigDecimal("5.6"), 2032),
                new Bond("RUS35", "Россия 2035", Currency.USD, new BigDecimal("6.0"), 2035),
                new Bond("SU26241", "ОФЗ 26241", Currency.RUB, new BigDecimal("8.9"), 2031),
                new Bond("SU26238", "ОФЗ 26238", Currency.RUB, new BigDecimal("7.6"), 2034),
                new Bond("RUS25", "Россия 2025", Currency.USD, new BigDecimal("4.5"), 2025),

                // ETF: отслеживаемый индекс
                new Etf("FXUS", "FinEx Акции США", Currency.USD, "S&P 500"),
                new Etf("FXRL", "FinEx Россия", Currency.USD, "MOEX Russia"),
                new Etf("FXMM", "FinEx Money Market", Currency.USD, "SOFR"),
                new Etf("SBGB", "SB Гос. облигации", Currency.RUB, "RGBI"),
                new Etf("FXGD", "FinEx Золото", Currency.USD, "Gold"),
                new Etf("FXIT", "FinEx ИТ США", Currency.USD, "NASDAQ-100"),
                new Etf("FXCN", "FinEx Китай", Currency.USD, "CSI 300"),
                new Etf("FXDE", "FinEx Германия", Currency.EUR, "DAX"),
                new Etf("FXKZ", "FinEx Казахстан", Currency.USD, "KASE"),
                new Etf("TSPX", "Т-Акции США", Currency.RUB, "S&P 500"));
    }
}
