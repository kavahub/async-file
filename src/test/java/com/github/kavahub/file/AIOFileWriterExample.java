package com.github.kavahub.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.kavahub.file.query.Query;
import com.github.kavahub.file.reader.AIOFileReader;
import com.github.kavahub.file.writer.AIOFileWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AIOFileWriterExample {
    private static final String FILE_TO_WRITE = "fileToWrite.txt";

    @BeforeEach
    public void clearUp() throws IOException {
        Files.deleteIfExists(Paths.get(FILE_TO_WRITE));
    }

    @Test
    public void whenWriteWithString() throws IOException {
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), "This is file content：你好").join();
    }

    @Test
    public void whenWriteWithStringSplit() throws IOException {
        final String content = "This is file content：你好";

        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), String.join(System.lineSeparator(), content.split(" "))).join();
    }

    @Test
    public void whenWriteWithQueryFlatMapMerge() throws IOException {
        Query<String>  data = Query.of("This is file content：你好")
            .flatMapMerge(line -> Query.of(line.split(" ")))
            .map((line) -> line + System.lineSeparator());
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), data).join();
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
        AIOFileWriter.write(Paths.get(FILE_TO_WRITE), reader).join();
    }
}
