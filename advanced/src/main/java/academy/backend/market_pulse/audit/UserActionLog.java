package academy.backend.market_pulse.audit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Аудит пользовательских действий (добавлен инструмент, изменён watchlist, обновлены котировки и
 * т.д. — см. {@link UserAction}) — партиционированный по дате лог: каждый день пишет в свой файл
 * {@code actions-YYYY-MM-DD.log}, а не в один бесконечно растущий файл. Запись — всегда в конец
 * ({@link StandardOpenOption#APPEND}), прошлые записи не перечитываются и не переписываются.
 */
public class UserActionLog {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Path directory;

    public UserActionLog(Path directory) {
        this.directory = directory;
    }

    public void record(UserAction action, String details) {
        Instant now = Instant.now();
        Path file = directory.resolve("actions-" + LocalDate.ofInstant(now, ZoneId.systemDefault())
                .format(FILE_DATE) + ".log");
        String line = now + ";" + action + ";" + details + System.lineSeparator();
        try {
            Files.createDirectories(directory);
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать событие аудита в " + file, e);
        }
    }
}
