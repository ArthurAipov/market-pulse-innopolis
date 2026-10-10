package academy.backend.market_pulse.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserActionLogTest {

    @Test
    void recordsAppendToTodaysPartitionFile(@TempDir Path tempDir) throws IOException {
        UserActionLog log = new UserActionLog(tempDir);

        log.record(UserAction.INSTRUMENT_ADDED, "SBER");
        log.record(UserAction.WATCH_ADDED, "GAZP");

        Path expectedFile = tempDir.resolve("actions-" + LocalDate.now() + ".log");
        assertTrue(Files.exists(expectedFile));

        List<String> lines = Files.readAllLines(expectedFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("INSTRUMENT_ADDED;SBER"));
        assertTrue(lines.get(1).contains("WATCH_ADDED;GAZP"));
    }
}
