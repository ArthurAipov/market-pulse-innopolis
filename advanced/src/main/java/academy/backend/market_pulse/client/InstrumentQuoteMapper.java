package academy.backend.market_pulse.client;

import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;

/**
 * Общий маппинг {@link InstrumentQuoteDto} → {@link Quote} для обеих реализаций
 * {@link TInvestApiClient} (см. пакет {@code client.impl}) — парсинг JSON у них разный, а
 * восстановление домена из DTO одно и то же.
 */
public final class InstrumentQuoteMapper {

    private InstrumentQuoteMapper() {
    }

    public static Quote toQuote(InstrumentQuoteDto dto) {
        Instrument instrument = InstrumentFactories.create(
                dto.getType(), dto.getTicker(), dto.getName(), Currency.valueOf(dto.getCurrency()));
        return new Quote(instrument, dto.getPrice(), dto.getChangePercent());
    }

    /**
     * Обратное преобразование — используется persistence-слоем (см. «Требования к файловому
     * хранилищу.md», семинар 7) для сохранения {@link Quote} в том же DTO-формате, в котором их
     * отдаёт instruments-service.
     */
    public static InstrumentQuoteDto toDto(Quote quote) {
        Instrument instrument = quote.getInstrument();
        return new InstrumentQuoteDto(
                instrument.getTicker(),
                instrument.getName(),
                instrument.getType(),
                instrument.getCurrency().name(),
                quote.getPrice(),
                quote.getChangePercent());
    }
}
