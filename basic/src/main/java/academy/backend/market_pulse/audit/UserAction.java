package academy.backend.market_pulse.audit;

/**
 * Типы пользовательских действий, попадающих в лог ({@link UserActionLog}). {@code BOUGHT}/
 * {@code SOLD} пока не используются ни одной командой — операций покупки/продажи в CLI ещё нет,
 * но формат лога уже рассчитан на них, чтобы не versioning'овать его задним числом, когда команда
 * появится.
 */
public enum UserAction {
    INSTRUMENT_ADDED,
    WATCH_ADDED,
    QUOTES_UPDATED,
    BOUGHT,
    SOLD
}
