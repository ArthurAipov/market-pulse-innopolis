package academy.backend.market_pulse.client.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.client.InstrumentQuoteMapper;
import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Опорная реализация {@link TInvestApiClient} — пишется семинаристом на этапе 2 (см. «План
 * семинара.md», семинар 7). Синхронный {@link HttpClient#send} намеренно: он же на этапе 3
 * демонстрирует проблему, которую решает Reactor — поток вызывающего кода блокируется на всё время
 * ожидания ответа. {@link HttpClient} создаётся один раз в конструкторе и переиспользуется.
 */
public class HttpClientTInvestApiClient implements TInvestApiClient {

    private final HttpClient httpClient;
    private final TInvestApiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpClientTInvestApiClient(TInvestApiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
    }

    @Override
    public List<Quote> fetchQuotes() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(properties.getBaseUrl() + "/v1/instruments"))
                .timeout(properties.getRequestTimeout())
                .header("Accept", "application/json")
                .GET();
        if (properties.getMockScenario() != null) {
            requestBuilder.header(TInvestApiProperties.MOCK_SCENARIO_HEADER, properties.getMockScenario());
        }
        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new TInvestApiException("Не удалось выполнить запрос к instruments-service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TInvestApiException("Запрос к instruments-service прерван", e);
        }

        if (response.statusCode() != 200) {
            throw new TInvestApiException(
                    "instruments-service вернул неожиданный статус: " + response.statusCode());
        }

        List<InstrumentQuoteDto> dtos;
        try {
            dtos = objectMapper.readValue(response.body(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, InstrumentQuoteDto.class));
        } catch (IOException e) {
            throw new TInvestApiException("Не удалось разобрать ответ instruments-service", e);
        }

        return dtos.stream().map(InstrumentQuoteMapper::toQuote).toList();
    }
}
