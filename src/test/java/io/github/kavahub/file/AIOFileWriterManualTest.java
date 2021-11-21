package io.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kavahub.file.reader.AIOFileReader;
import io.github.kavahub.file.writer.AIOFileWriter;
import io.github.kavahub.file.writer.CompletableFileWriter;

public class AIOFileWriterManualTest {
    private static final Path FILE_TO_WRITE = Paths.get("target", "fileToWrite.txt");

    @BeforeEach
    public void clearUp() throws IOException {
        Files.deleteIfExists(FILE_TO_WRITE);
    }

    @Test
    public void whenWriteWithString() throws IOException {
        AIOFileWriter.write(FILE_TO_WRITE, "This is file content：你好").join();
    }

    @Test
    public void whenWriteWithStringSplit() throws IOException {
        final String content = "This is file content：你好";

        AIOFileWriter.write(FILE_TO_WRITE, String.join(System.lineSeparator(), content.split(" "))).join();
    }

    @Test
    public void whenWriteWithQueryFilter() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        try (CompletableFileWriter writer = AIOFileWriter.of(FILE_TO_WRITE)) {

            AIOFileReader.line(FILE)
                    // 忽略前2行
                    .skip(2)
                    // 过滤掉空行
                    .filter(line -> !line.isBlank())
                    // 转换成大写
                    .map(String::toUpperCase)
                    // 加入换行符
                    .map(line -> line + System.lineSeparator())
                    .subscribe((data, err) -> {
                        if (err != null) {
                            err.printStackTrace();
                        }

                        if (data != null) {
                            writer.write(data);
                        }
                    }).join();

            // 等待写入完成
            writer.getPosition().whenComplete((size, error) -> {
                if (error != null) {
                    error.printStackTrace();
                }

                System.out.println("总共写入字节数：" + size);
            }).join();
        }
        ;
    }
}
