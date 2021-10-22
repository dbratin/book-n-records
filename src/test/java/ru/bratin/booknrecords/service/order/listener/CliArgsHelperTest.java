package ru.bratin.booknrecords.service.order.listener;

import org.junit.jupiter.api.Test;
import ru.bratin.booknrecords.service.CliArgsHelper;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class CliArgsHelperTest {

    @Test
    public void exceptionalCasesTest() {
        // no args
        assertThrows(IllegalArgumentException.class, () -> CliArgsHelper.getFileArg(new String[]{}));

        // file not exists
        assertThrows(IllegalArgumentException.class, () -> CliArgsHelper.getFileArg(new String[]{"./sonething-that-not-exists"}));

        // dir instead of file provided
        assertThrows(IllegalArgumentException.class, () -> CliArgsHelper.getFileArg(new String[]{"./test-dir"}));

        // not readable file provided
        assertThrows(IllegalArgumentException.class, () -> CliArgsHelper.getFileArg(new String[]{"./need to think how to implement"}));
    }

    @Test
    public void regularCaseTest() throws IOException {
        var path = CliArgsHelper.getFileArg(new String[]{ "./target/test-classes/orders.xml" });

        try(var stream = Files.newInputStream(path)) {
            assertTrue(stream.read() != 0);
        }
    }
}
