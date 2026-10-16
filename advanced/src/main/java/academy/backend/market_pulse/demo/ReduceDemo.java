package academy.backend.market_pulse.demo;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

/**
 * Демо {@code Stream.reduce} — терминальной операции «свёртки», которая превращает
 * поток элементов в одно значение. В отличие от {@code collect} (который накапливает в
 * изменяемый контейнер), {@code reduce} строит результат «из ничего», комбинируя элементы
 * бинарным оператором.
 *
 * <p><b>Три перегрузки {@code reduce}:</b></p>
 * <ol>
 *   <li>{@code reduce(T identity, BinaryOperator<T>)} — с «начальным значением» (identity);
 *       результат никогда не пуст.</li>
 *   <li>{@code reduce(BinaryOperator<T>)} — без identity; возвращает {@code Optional}.</li>
 *   <li>{@code reduce(U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner)}
 *       — «свёртка с изменением типа» (например, поток строк в одно число), заточенная
 *       под параллельные потоки: {@code combiner} склеивает промежуточные результаты потоков.</li>
 * </ol>
 *
 * <p><b>Зачем {@code combiner} в параллельных потоках:</b> при {@code parallel()} поток делится
 * на сегменты, каждый обрабатывается своим потоком и даёт свой промежуточный результат.
 * Затем все эти результаты склеиваются именно оператором {@code combiner}. Это тот же принцип,
 * что fork/join: разделить → посчитать части → объединить.</p>
 */
@SuppressWarnings("JavaPrintToLogpoint")
public class ReduceDemo {

    /**
     * Точка входа демо. Не имеет аргументов.
     */
    public static void main(String[] args) {
        List<Integer> prices = List.of(10, 20, 30, 40, 50); // «цены акций» для наглядности

        System.out.println("=============== ReduceDemo: свёртка потоков ===============");
        System.out.println("Цены: " + prices);

        // 1) reduce с identity — суммирование. Начальное значение 0, дальше (a, b) -> a + b.
        //    Результат: 0 + 10 + 20 + 30 + 40 + 50 = 150.
        int sum = prices.stream()
                .reduce(0, (a, b) -> a + b);
        System.out.println("1. Сумма (reduce(identity=0)): " + sum);

        // Тот же результат через метод-ссылку — эквивалентно Integer::sum.
        int sumRef = prices.stream()
                .reduce(0, Integer::sum);
        System.out.println("   То же через Integer::sum : " + sumRef);

        // 2) reduce без identity — тоже сумма, но вернёт Optional<Integer>.
        //    Если поток пуст — Optional.empty(). Здесь поток непустой, поэтому есть значение.
        Optional<Integer> sumOpt = prices.stream()
                .reduce(Integer::sum);
        System.out.println("2. Сумма (reduce без identity): " + sumOpt.orElse(0));

        // 3) reduce без identity на ПУСТОМ потоке — наглядный случай Optional.empty().
        Optional<Integer> emptySum = List.<Integer>of().stream()
                .reduce(Integer::sum);
        System.out.println("3. Сумма пустого потока: " + emptySum); // Optional.empty

        // 4) Максимум через reduce — «аккумулятор держит текущий максимум».
        Optional<Integer> max = prices.stream()
                .reduce(Integer::max);
        System.out.println("4. Максимум через reduce(Integer::max): " + max.orElse(-1));

        // 5) reduce с изменением типа: поток строк свести в одно число.
        //    accumulator: прибавляет длину очередной строки; combiner: складывает части (для parallel()).
        List<String> tickers = List.of("SBER", "GAZP", "NLMK");
        int totalLength = tickers.parallelStream()
                .reduce(0,
                        (acc, s) -> acc + s.length(),        // accumulator — как посчитать один элемент
                        Integer::sum);                       // combiner — как склеить результаты потоков
        System.out.println("5. Суммарная длина тикеров (parallel reduce): " + totalLength);

        // 6) Последовательный reduce для сравнения с пунктом 5 — тот же результат, один поток.
        int totalLengthSeq = tickers.stream()
                .reduce(0, (acc, s) -> acc + s.length(), Integer::sum);
        System.out.println("   То же последовательно: " + totalLengthSeq);

        // 7) Сворачиваем поток чисел в строку-представление (reduce с изменением типа).
        String joined = IntStream.rangeClosed(1, 5)
                .mapToObj(String::valueOf)
                .reduce("", (acc, s) -> acc.isEmpty() ? s : acc + "-" + s);
        System.out.println("7. Числа через reduce в строку: " + joined); // "1-2-3-4-5"
    }
}