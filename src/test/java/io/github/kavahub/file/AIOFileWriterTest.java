package io.github.kavahub.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kavahub.file.reader.AIOFileReader;
import io.github.kavahub.file.writer.AIOFileWriter;
import io.github.kavahub.file.writer.CompletableFileWriter;

public class AIOFileWriterTest {
    private static final Path FILE_TO_WRITE = Paths.get("target", "fileToWrite.txt");
    private static final List<String> EXPECTED = Arrays.asList("super", "brave", "isel", "ole", "中文", "massive");

    @BeforeEach
    public void clearUp() throws IOException {
        Files.deleteIfExists(FILE_TO_WRITE);
    }

    @Test
    public void whenWriteIterable_thenExcepted() throws IOException {
        try (CompletableFileWriter writer = AIOFileWriter.of(FILE_TO_WRITE)) {
            EXPECTED.stream().map(line -> line + System.lineSeparator()).forEach(line -> {
                writer.write(line);
            });

            // 等待写入完成
            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                System.out.println("总共写入字节数：" + size);
            }).join();

            Iterator<String> actual = Files.lines(FILE_TO_WRITE).iterator();
            if (actual.hasNext() == false)
                fail("File is empty!!!");

            EXPECTED.forEach(l -> {
                if (actual.hasNext())
                    assertEquals(l, actual.next());
                else
                    fail("File does not contain line: " + l);
            });
        }

    }

    @Test
    public void whenWriteByte_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        final byte[] expected = Files.readAllBytes(FILE);
        AIOFileWriter.write(FILE_TO_WRITE, expected).join();

        assertArrayEquals(expected, Files.readAllBytes(FILE_TO_WRITE));
    }

    @Test
    public void giveReadLine_whenWrite_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        try (CompletableFileWriter writer = AIOFileWriter.of(FILE_TO_WRITE)) {

            AIOFileReader.line(FILE).subscribe(data -> {
                if (data != null) {
                    writer.write(data + System.lineSeparator());
                }
            }, err -> err.printStackTrace()).join();

            // 等待写入完成
            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                System.out.println("总共写入字节数：" + size);
            }).join();

            assertArrayEquals(Files.readAllBytes(FILE), Files.readAllBytes(FILE_TO_WRITE));
        }
    }

    @Test
    public void giveReadBytes_whenWrite_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        try (CompletableFileWriter writer = AIOFileWriter.of(FILE_TO_WRITE)) {

            AIOFileReader.bytes(FILE).subscribe(data -> {
                writer.write(data);
            }, err -> err.printStackTrace()).join();

            // 等待写入完成
            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                System.out.println("总共写入字节数：" + size);
            }).join();
            assertArrayEquals(Files.readAllBytes(FILE), Files.readAllBytes(FILE_TO_WRITE));
        }
    }

    @Test
    public void giveReadAllLines_whenWrite_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        try (CompletableFileWriter writer = AIOFileWriter.of(FILE_TO_WRITE)) {

            AIOFileReader.allLines(FILE).subscribe(data -> {
                writer.write(data);
            }, err -> err.printStackTrace()).join();

            // 等待写入完成
            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                System.out.println("总共写入字节数：" + size);
            }).join();

            assertArrayEquals(Files.readAllBytes(FILE), Files.readAllBytes(FILE_TO_WRITE));
        }
    }

    @Test
    public void giveReadAllBytes_whenWrite_thenExcepted() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");
        try (CompletableFileWriter writer = AIOFileWriter.of(FILE_TO_WRITE)) {

            AIOFileReader.allBytes(FILE).subscribe(data -> {
                writer.write(data);
            }, err -> err.printStackTrace()).join();

            // 等待写入完成
            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                System.out.println("总共写入字节数：" + size);
            }).join();

            assertArrayEquals(Files.readAllBytes(FILE), Files.readAllBytes(FILE_TO_WRITE));
        }
    }
}
