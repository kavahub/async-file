package io.github.kavahub.file.reader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import io.github.kavahub.file.Consts;
import io.github.kavahub.file.query.Query;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AIOFileReader {
    
    public Query<String> line(Path file) {
        return line(file, Consts.BUFFER_SIZE);
    }

    public Query<String> line(Path file, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize");
        }

        return new QueryLine(new FileByteReaderQuery(file, bufferSize));
    }

    public Query<String> allLines(Path file) {
        return allLines(file, Consts.BUFFER_SIZE);
    }

    public Query<String> allLines(Path file, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize");
        }

        return new QueryAllBytes(new FileByteReaderQuery(file, bufferSize))
            .map(bytes -> new String(bytes, StandardCharsets.UTF_8));
    }

    public Query<byte[]> bytes(Path file) {
        return new FileByteReaderQuery(file, Consts.BUFFER_SIZE);
    }

    public Query<byte[]> bytes(Path file, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize");
        }
        return new FileByteReaderQuery(file, bufferSize);
    }

    public Query<byte[]> allBytes(Path file) {
        return allBytes(file, Consts.BUFFER_SIZE);
    }

    public Query<byte[]> allBytes(Path file, int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize");
        }

        return new QueryAllBytes(new FileByteReaderQuery(file, bufferSize));
    }
}
