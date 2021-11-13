package com.github.kavahub.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.github.kavahub.file.reader.AIOFileReader;
import com.github.kavahub.file.writer.AIOFileWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AIOFileWriterTest {
    private static final String FILE_TO_WRITE = "fileToWrite.txt";
    private static final List<String> EXPECTED = Arrays.asList("super", "brave", "isel", "ole", "中文", "massive");

    @BeforeEach
    public void clearUp() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_TO_WRITE));
    }

    @Test
    public void whenWriteIterable_thenExcepted() throws IOException {
        List<String> data = EXPECTED.stream().map(line -> line + System.lineSeparator()).collect(Collectors.toList());
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), data)
                // 写入完成时
                .whenComplete((index, ex) -> {
                    if (ex != null)
                        fail(ex.getMessage());
                })
                // 等待写入完成
                .join();
        Iterator<String> actual = Files.lines(Paths.get(FILE_TO_WRITE)).iterator();
        if (actual.hasNext() == false)
            fail("File is empty!!!");
        EXPECTED.forEach(l -> {
            if (actual.hasNext())
                assertEquals(l, actual.next());
            else
                fail("File does not contain line: " + l);
        });

    }

    @Test
    public void whenWriteByte_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        final byte[] expected = Files.readAllBytes(FILE);
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), expected).join();

        assertArrayEquals(expected, Files.readAllBytes(Paths.get(FILE_TO_WRITE)));
    }

    @Test
    public void giveReadAndWriteOneLine_whenLine_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        AIOFileWriter
                .write(Paths.get(FILE_TO_WRITE), AIOFileReader.line(FILE).map(line -> line + System.lineSeparator()))
                .join();

        assertArrayEquals(Files.readAllBytes(FILE), Files.readAllBytes(Paths.get(FILE_TO_WRITE)));
    }

    @Test
    public void giveReadAndWriteOneLine_whenAllLine_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), AIOFileReader.allLines(FILE)).join();

        assertArrayEquals(Files.readAllBytes(FILE), Files.readAllBytes(Paths.get(FILE_TO_WRITE)));
    }

    @Test
    public void giveReadAndWriteOneLine_whenAllByte_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE),
                AIOFileReader.allBytes(FILE).map(bytes -> new String(bytes, StandardCharsets.UTF_8))).join();

        assertArrayEquals(Files.readAllBytes(FILE), Files.readAllBytes(Paths.get(FILE_TO_WRITE)));
    }
}
