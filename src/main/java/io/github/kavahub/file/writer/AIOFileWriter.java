package io.github.kavahub.file.writer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AIOFileWriter {
    public CompletableFileWriter of(Path file) throws IOException {
        return of(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public CompletableFileWriter of(Path file, StandardOpenOption... options) throws IOException {
        return CompletableFileWriter.of(file, options);
    }

    public CompletableFuture<Integer> write(Path file, byte[] bytes) {
        return write(file, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public CompletableFuture<Integer> write(Path file, byte[] bytes, StandardOpenOption... options) {
        try (CompletableFileWriter writer = CompletableFileWriter.of(file, options)) {
            return writer.write(bytes);
        }
    }

    public CompletableFuture<Integer> write(Path file, String line) {
        return write(file, line, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public CompletableFuture<Integer> write(Path file, String line,
            StandardOpenOption... options) {

        try (CompletableFileWriter writer = CompletableFileWriter.of(file, options)) {
            return writer.write(line);
        }
    }

}
