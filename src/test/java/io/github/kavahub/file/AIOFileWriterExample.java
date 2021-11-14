package io.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.kavahub.file.query.Query;
import io.github.kavahub.file.reader.AIOFileReader;
import io.github.kavahub.file.writer.AIOFileWriter;

public class AIOFileWriterExample {
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
    public void whenWriteWithQueryFlatMapMerge() throws IOException {
        Query<String>  data = Query.of("This is file content：你好")
            .flatMapMerge(line -> Query.of(line.split(" ")))
            .map((line) -> line + System.lineSeparator());
        AIOFileWriter.write(FILE_TO_WRITE, data).join();
    }

    @Test
    public void whenWriteWithQueryFilter() throws IOException {
        final Path FILE = Paths.get("src", "test", "resources", "fileWithmanyOfLine.txt");

        Query<String> reader = AIOFileReader.line(FILE)
            // 忽略前2行
            .skip(2)
            // 过滤掉空行
            .filter(line -> !line.isBlank())
            // 转换成大写
            .map(String::toUpperCase)
            // 加入换行符
            .map((line) -> line + System.lineSeparator());
        AIOFileWriter.write(FILE_TO_WRITE, reader).join();
    }
}
