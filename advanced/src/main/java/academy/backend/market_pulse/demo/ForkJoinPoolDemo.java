package academy.backend.market_pulse.demo;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

/**
 * Демо {@link ForkJoinPool} (пул задач «разделяй и властвуй»). Типовой, несложный пример —
 * сортировка массива методом слияния (merge sort), распараллеленная через {@link RecursiveTask}.
 *
 * <p><b>Зачем нужен {@code ForkJoinPool}:</b> это специальный пул потоков для задач, которые
 * рекурсивно дробятся на подзадачи. В отличие от обычного {@code ThreadPoolExecutor} он
 * использует <i>работу-воровство</i> (work stealing): свободный поток «ворует» задачу из чужой
 * очереди, если своя пуста. Это держит все ядра занятыми при неравномерной нагрузке.</p>
 *
 * <p><b>Как это работает:</b></p>
 * <ol>
 *   <li>{@link #main(String[])} создаёт пул, «заталкивает» в него корневую задачу сортировки
 *       через {@link ForkJoinPool#invoke(ForkJoinTask)} и печатает результат;</li>
 *   <li>{@link MergeSortTask} рекурсивно делит диапазон массива пополам, запускает две
 *       половины параллельно ({@link RecursiveTask#fork()}) и объединяет их результаты
 *       ({@link RecursiveTask#join()});</li>
 *   <li>как только подмассив стал достаточно мал, задача переключается на обычную
 *       последовательную сортировку вставками — это называется <i>порог</i> (threshold).</li>
 * </ol>
 *
 * <p>Подробные объяснения — в комментариях у каждого метода и внутри {@code main}.</p>
 */
@SuppressWarnings("JavaPrintToLogpoint")
public class ForkJoinPoolDemo {

    /**
     * Размер подмассива, начиная с которого «разделять» уже невыгодно.
     * Выделение подзадач и обмен между потоками стоят дороже, чем сортировка
     * маленького участка в одном потоке, поэтому для коротких кусков используем
     * сортировку вставками прямо здесь.
     */
    private static final int THRESHOLD = 10_000;

    /**
     * Точка входа демо. Не имеет аргументов — запускается как обычная Java-программа.
     */
    public static void main(String[] args) {
        System.out.println("=============== ForkJoinPoolDemo: параллельная сортировка слиянием ===============");

        // Генерируем "рыночный" массив цен акций в убывающем порядке (N, N-1, ..., 1).
        // Это наихудший случай для сортировки слиянием — задача наглядно дробится на части.
        int[] prices = descendingPrices(1_000_000);

        System.out.printf("Размер массива: %d%n", prices.length);
        System.out.printf("До сортировки : [%d, %d, %d, ... %d]%n",
                prices[0], prices[1], prices[2], prices[prices.length - 1]);

        // 1) Создаём общий пул Fork/Join. Вместо new можно было взять ForkJoinPool.commonPool(),
        //    но собственный пул позволяет явно указать количество потоков и наглядно показать,
        //    что создаётся отдельный экземпляр. Параллелизм = числу ядер процессора.
        int parallelism = Runtime.getRuntime().availableProcessors();
        try (ForkJoinPool pool = new ForkJoinPool(parallelism)) {

            System.out.printf("Пул: parallelism=%d, activeThreadCount=%d%n",
                    pool.getParallelism(), pool.getActiveThreadCount());

            long start = System.nanoTime();

            // 2) Запускаем корневую задачу. pool.invoke(...) сам рекурсивно порождает подзадачи,
            //    выполняет их на доступных потоках и возвращает итоговый результат.
            int[] sorted = pool.invoke(new MergeSortTask(prices, 0, prices.length - 1));

            long elapsed = System.nanoTime() - start;

            System.out.printf("После сортировки: [%d, %d, %d, ... %d]%n",
                    sorted[0], sorted[1], sorted[2], sorted[sorted.length - 1]);
            System.out.printf("Отсортирован корректно: %b%n", isSortedAscending(sorted));
            System.out.printf("Время сортировки: %.3f мс (в %d потоков)%n",
                    elapsed / 1_000_000.0, pool.getParallelism());
        }
    }

    /**
     * Создаёт массив цен, упорядоченный по убыванию (от {@code size} до 1).
     * Такой вход специально выбран, чтобы слиянию приходилось таскать максимум данных —
     * видно, что задача реально дробится на подзадачи.
     *
     * @param size количество элементов
     * @return массив {@code [size, size-1, ..., 2, 1]}
     */
    private static int[] descendingPrices(int size) {
        return IntStream.iterate(size, i -> i - 1)
                .limit(size)
                .toArray();
    }

    /**
     * Проверяет, что массив отсортирован по возрастанию (неубывание).
     *
     * @param a проверяемый массив
     * @return {@code true}, если каждый следующий элемент не меньше предыдущего
     */
    private static boolean isSortedAscending(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[i - 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Рекурсивная задача сортировки слиянием на диапазоне {@code [from, to]} включительно.
     *
     * <p>Ключевой момент {code ForkJoin}: метод {@code compute()} рекурсивно вызывается самим
     * пулом на отдельном потоке. Если диапазон большой — задача создаёт две половинки,
     * одну запускает через {@link #fork()} (выполняется асинхронно в другом потоке), а вторую
     * выполняет сам через {@link #compute()}, затем ждёт результат форкнутой через
     * {@link #join()}. Если диапазон мал — сортирует вставками без параллелизма.</p>
     */
    private static final class MergeSortTask extends RecursiveTask<int[]> {

        private final int[] source; // исходный массив, который сортируем
        private final int from;     // левая граница диапазона (включительно)
        private final int to;       // правая граница диапазона (включительно)

        private MergeSortTask(int[] source, int from, int to) {
            this.source = source;
            this.from = from;
            this.to = to;
        }

        /**
         * Выполняет сортировку диапазона {@code [from, to]} и возвращает отсортированный
         * новый массив. Этот метод вызывает пул, а не программа напрямую.
         *
         * @return отсортированная копия подмассива {@code source[from..to]}
         */
        @Override
        protected int[] compute() {
            int length = to - from + 1;

            // База рекурсии: маленький диапазон сортируем вставками в текущем потоке.
            // Это избавляет от оверхеда на создание/объединение множества мелких задач.
            if (length <= THRESHOLD) {
                return insertionSortRange();
            }

            // Разделяем диапазон пополам. from..mid и mid+1..to — две независимые половины.
            int mid = (from + to) >>> 1; // беззнаковый сдвиг — надёжная формула середины без переполнения

            // Создаём две подзадачи для левой и правой половин.
            MergeSortTask left = new MergeSortTask(source, from, mid);
            MergeSortTask right = new MergeSortTask(source, mid + 1, to);

            // Запускаем правую половину асинхронно: пул может выполнить её на другом потоке.
            right.fork();

            // Левую половину выполняем синхронно в текущем потоке (это сам RecursiveTask.compute()).
            int[] sortedLeft = left.compute();

            // Ждём результат правой половины. join() блокирует поток, пока задача не завершится,
            // и возвращает её результат (в нашем случае отсортированный массив).
            int[] sortedRight = right.join();

            // Сливаем два отсортированных массива в один — классическое слияние.
            return merge(sortedLeft, sortedRight);
        }

        /**
         * Сортирует текущий диапазон {@code [from, to]} сортировкой вставками.
         * Применяется на малых подмассивах (ниже {@link #THRESHOLD}) — в одном потоке.
         *
         * @return отсортированная копия подмассива
         */
        private int[] insertionSortRange() {
            int[] result = Arrays.copyOfRange(source, from, to + 1);
            for (int i = 1; i < result.length; i++) {
                int key = result[i];
                int j = i - 1;
                while (j >= 0 && result[j] > key) {
                    result[j + 1] = result[j];
                    j--;
                }
                result[j + 1] = key;
            }
            return result;
        }
    }

    /**
     * Сливает два уже отсортированных массива {@code left} и {@code right} в один отсортированный.
     * Классический merge из merge sort: идём двумя указателями и каждый раз берём меньший элемент.
     *
     * @param left  отсортированный по возрастанию массив
     * @param right отсортированный по возрастанию массив
     * @return новый отсортированный массив, содержащий все элементы {@code left} и {@code right}
     */
    private static int[] merge(int[] left, int[] right) {
        int[] merged = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;

        // Пока есть элементы в обоих массивах — берём меньший.
        while (i < left.length && j < right.length) {
            if (left[i] <= right[j]) {
                merged[k++] = left[i++];
            } else {
                merged[k++] = right[j++];
            }
        }

        // Дописываем остаток левого массива (если правый закончился раньше).
        while (i < left.length) {
            merged[k++] = left[i++];
        }

        // Дописываем остаток правого массива (если левый закончился раньше).
        while (j < right.length) {
            merged[k++] = right[j++];
        }

        return merged;
    }
}