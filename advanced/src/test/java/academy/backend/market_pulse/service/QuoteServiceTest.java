package academy.backend.market_pulse.service;


import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Тест сервиса котировок с замоканным источником: пишется вживую человеком. Эталон —
 * {@code QuoteServiceReferenceTest}.
 */
@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    // NOTICE: QuoteService — единственный код с логикой кеша; источник котировок (QuoteSource) —
    // его внешняя зависимость, поэтому мокаем именно её: @ExtendWith(MockitoExtension.class),
    // @Mock QuoteSource, сервис создаём вручную через конструктор.
    // NOTICE: главное, что проверяем, — повторный запрос того же тикера НЕ ходит в источник снова:
    // verify(source, times(1)).fetch("SBER"). Это проверка взаимодействия, а не значения.
    // NOTICE: when(source.fetch("SBER")).thenReturn(Optional.of(quote)) — заглушка ответа; тикер
    // сервис нормализует к верхнему регистру, мок увидит уже "SBER".
    @Mock
    private QuoteSource source;

    @Test
    void repeatedRequest() {
        QuoteService quoteService = new QuoteService(source);

        Stock stock = new Stock("SBER", "Сбер",
                Currency.RUB, "Финансы", new BigDecimal("5.0"));
        Quote quote = new Quote(stock, new BigDecimal("250.0"), new BigDecimal("1.2"));

        when(source.fetch("SBER")).thenReturn(Optional.of(quote));

        quoteService.request("SBER");
        quoteService.request("SBER");
        quoteService.processAll();

        assertThat(quoteService.cached("SBER")).contains(quote);
        verify(source, times(1)).fetch("SBER");

    }

    @Test
    void unknownRequest() {
        QuoteService quoteService = new QuoteService(source);
        when(source.fetch("UNKNOWN")).thenReturn(Optional.empty());
        quoteService.request("UNKNOWN");
        quoteService.processAll();
        assertThat(quoteService.cached("UNKNOWN")).isEmpty();
        verify(source).fetch("UNKNOWN");
    }

}
