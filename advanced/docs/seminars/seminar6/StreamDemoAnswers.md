# StreamDemo — ответы к задачнику по Stream API

Семинар 6, этап 1. Готовые решения к задачнику `src/main/java/academy/backend/market_pulse/demo/StreamDemo.java`.
Студентам не выдавать: у них в коде стоят `// TODO`, которые заполняются на занятии.

Данные — каталог `List<Instrument>` (50 инструментов), см. `sampleInstruments()` в `StreamDemo.java`.

---

## 1. Топ-3 облигации по купону (купон больше 8%)

```java
private static List<Bond> topCouponBonds(List<Instrument> instruments) {
    return instruments.stream()
            .filter(Bond.class::isInstance)                  // instanceof Bond
            .map(instrument -> (Bond) instrument)            // каст к Bond
            .filter(bond -> bond.getCouponRate().compareTo(new BigDecimal("8")) > 0)
            .sorted(Comparator.comparing(Bond::getCouponRate).reversed())
            .limit(3)
            .toList();
}
```

Операции: `filter` + `map` (каст) + `filter` + `sorted` + `limit`. Деньги/проценты сравниваем через
`BigDecimal.compareTo`, а не через `>`.

---

## 2. Тикеры акций финансового сектора по алфавиту

```java
private static List<String> financialStockTickers(List<Instrument> instruments) {
    return instruments.stream()
            .filter(Stock.class::isInstance)
            .map(instrument -> (Stock) instrument)
            .filter(stock -> "Финансы".equals(stock.getSector()))
            .map(Stock::getTicker)
            .sorted()
            .toList();
}
```

Операции: `filter` + `map` (каст) + `filter` + `map` + `sorted`.

---

## 3. Сколько акций в каждом секторе

```java
private static Map<String, Long> stocksBySector(List<Instrument> instruments) {
    return instruments.stream()
            .filter(Stock.class::isInstance)
            .map(instrument -> (Stock) instrument)
            .collect(Collectors.groupingBy(Stock::getSector, Collectors.counting()));
}
```

Коллектор `groupingBy` с вторичным коллектором `counting`.

---

## 4. Средний купон по всем облигациям

```java
private static double averageCoupon(List<Instrument> instruments) {
    return instruments.stream()
            .filter(Bond.class::isInstance)
            .map(instrument -> (Bond) instrument)
            .mapToDouble(bond -> bond.getCouponRate().doubleValue())
            .average()
            .orElse(0.0);
}
```

Операции: `filter` + `map` (каст) + `mapToDouble` + `average`. `average()` возвращает
`OptionalDouble` — для отображения процента допустим перевод в `double` (это не деньги).

---

## 5. Акция с максимальной дивидендной доходностью

```java
private static Optional<Stock> bestDividendStock(List<Instrument> instruments) {
    return instruments.stream()
            .filter(Stock.class::isInstance)
            .map(instrument -> (Stock) instrument)
            .max(Comparator.comparing(Stock::getDividendYield));
}
```

`max` — терминальная операция, возвращает `Optional<Stock>` (пустой, если акций нет).

---

## 6. Все валюты, в которых есть инструменты, без повторов

```java
private static List<Currency> currencies(List<Instrument> instruments) {
    return instruments.stream()
            .map(Instrument::getCurrency)
            .distinct()
            .toList();
}
```

`map` + `distinct` убирает дубликаты валют.

---

## 7. Тикеры всех ETF одной строкой через запятую

```java
private static String etfTickers(List<Instrument> instruments) {
    return instruments.stream()
            .filter(instrument -> instrument instanceof Etf)   // instanceof без каста — тикер есть у Instrument
            .map(Instrument::getTicker)
            .collect(Collectors.joining(", "));
}
```

Коллектор `joining`. Здесь `instanceof` нужен только для отбора — каст не требуется, потому что
`getTicker()` объявлен в базовом `Instrument`.

---

## 8. По типу → топ-3 тикера по алфавиту в каждой группе

```java
private static Map<String, List<String>> top3ByTickerInGroup(List<Instrument> instruments) {
    return instruments.stream()
            .collect(Collectors.groupingBy(
                    Instrument::getType,
                    Collectors.collectingAndThen(
                            Collectors.toList(),
                            group -> group.stream()
                                    .map(Instrument::getTicker)
                                    .sorted()
                                    .limit(3)
                                    .toList())));
}
```

Повторное открытие стрима после `collect`: `groupingBy` группирует по типу, а вторичный коллектор
`collectingAndThen` внутри каждой группы заново открывает стрим (`sorted` + `limit(3)`).

---

## 9. Облигация с самым ранним погашением

```java
private static Optional<Bond> earliestMaturityBond(List<Instrument> instruments) {
    return instruments.stream()
            .filter(Bond.class::isInstance)
            .map(instrument -> (Bond) instrument)
            .min(Comparator.comparing(Bond::getMaturityYear));
}
```

`min` по году погашения — возвращает `Optional<Bond>`.

---

## 10. По сектору → лучшая по дивидендам акция в каждом

```java
private static Map<String, Stock> bestDividendPerSector(List<Instrument> instruments) {
    return instruments.stream()
            .filter(Stock.class::isInstance)
            .map(instrument -> (Stock) instrument)
            .collect(Collectors.groupingBy(Stock::getSector))
            .entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                            .max(Comparator.comparing(Stock::getDividendYield))
                            .orElseThrow()));
}
```

Повторное открытие стрима после `collect`: после `groupingBy(Stock::getSector)` снова открываем стрим
по `entrySet()`, а внутри каждой группы (список акций сектора) — ещё один стрим с `max` по
доходности. Вариант без второго `collect`:

```java
Map<String, Stock> bySector = instruments.stream()
        .filter(Stock.class::isInstance)
        .map(instrument -> (Stock) instrument)
        .collect(Collectors.groupingBy(Stock::getSector));
bySector.replaceAll((sector, stocks) -> stocks.stream()
        .max(Comparator.comparing(Stock::getDividendYield))
        .orElseThrow());
return bySector;
```

---

## Ожидаемый вывод

При корректных решениях `main` печатает:

```
======== 1. Топ-3 облигации по купону (купон больше 8%) ========
  [OFZ29006 — ОФЗ 29006 (...), OFZ29011 — ОФЗ 29011 (...), OFZ26228 — ОФЗ 26228 (...)]
======== 2. Тикеры акций финансового сектора по алфавиту ========
  [SBER, TCSG, VTBR]
======== 3. Сколько акций в каждом секторе ========
  {Телеком=1, Финансы=3, Металлургия=3, Технологии=6, Добыча=2, Энергетика=6, Потребление=2}
======== 4. Средний купон по всем облигациям ========
  7.517647058823529
======== 5. Акция с максимальной дивидендной доходностью ========
  NLMK
======== 6. Все валюты, в которых есть инструменты ========
  [RUB, USD, EUR]
======== 7. Тикеры всех ETF через запятую ========
  FXUS, FXRL, FXMM, SBGB, FXGD, FXIT, FXCN, FXDE, FXKZ, TSPX
======== 8. По типу → топ-3 тикера по алфавиту в каждой группе ========
  {ETF=[FXCN, FXDE, FXGD], BOND=[OFZ26215, OFZ26218, OFZ26221], STOCK=[AAPL, ALRS, CHMF]}
======== 9. Облигация с самым ранним погашением ========
  RUS25
======== 10. По сектору → лучшая по дивидендам в каждом ========
  {Телеком=MTSS (...), Финансы=SBER (...), Металлургия=NLMK (...), Технологии=MSFT (...), Добыча=ALRS (...), Энергетика=TATN (...), Потребление=MGNT (...)}
```