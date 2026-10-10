package academy.backend.market_pulse.cli;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.TInvestApiException;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.persistence.FileBackedQuoteRepository;
import picocli.CommandLine.Command;

/**
 * Обновляет на диске котировки отслеживаемых инструментов (см. «План семинара.md», семинар 7,
 * этап 4): запрашивает актуальные данные через {@link TInvestApiClient} и формирует новый файл с
 * обновлёнными значениями — то, что потом переживает перезапуск CLI, в отличие от
 * {@code QuoteService}, который ничего не сохраняет между вызовами.
 */
@Command(name = "update", description = "Обновить на диске котировки отслеживаемых инструментов")
public class UpdateCommand implements Callable<Integer> {

    private final TInvestApiClient client;
    private final Watchlist watchlist;
    private final FileBackedQuoteRepository repository;

    public UpdateCommand(TInvestApiClient client, Watchlist watchlist, FileBackedQuoteRepository repository) {
        this.client = client;
        this.watchlist = watchlist;
        this.repository = repository;
    }

    @Override
    public Integer call() {
        Set<String> tracked = watchlist.tickers();
        if (tracked.isEmpty()) {
            System.out.println("Watchlist пуст — нечего обновлять. Добавьте тикеры командой watch.");
            return 1;
        }

        List<Quote> quotes;
        try {
            quotes = client.fetchQuotes();
        } catch (TInvestApiException e) {
            System.out.println("Не удалось получить данные от instruments-service: " + e.getMessage());
            return 1;
        }

        List<Quote> trackedQuotes = quotes.stream()
                .filter(quote -> tracked.contains(quote.getInstrument().getTicker()))
                .toList();

        repository.save(trackedQuotes);
        System.out.println("Обновлено котировок: " + trackedQuotes.size() + " -> " + repository.getPath());
        return 0;
    }
}
