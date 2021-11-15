package io.github.kavahub.file.writer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import io.github.kavahub.file.query.Query;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
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
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Integer> write(Path file, String line) {
        return write(file, line, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public CompletableFuture<Integer> write(Path file, String line,
            StandardOpenOption... options) {
                if (log.isDebugEnabled()) {
                    log.debug("Begin to write file: {}", file.toString());
                }
        try (CompletableFileWriter writer = CompletableFileWriter.of(file, options)) {
            CompletableFuture<Integer> future = writer.write(line);
            future.whenComplete((size, error) -> {
                if (log.isDebugEnabled()) {
                    log.debug("Total [{} bytes] has been writed to file: {}", size, file.toString());
                }
            });
            return future;
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<Integer> write(Path file, Query<String> lines) {
        return write(file, lines, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public CompletableFuture<Integer> write(Path file, Query<String> lines, StandardOpenOption... options) {
        if (log.isDebugEnabled()) {
            log.debug("Begin to write file: {}", file.toString());
        }
        try (CompletableFileWriter writer = CompletableFileWriter.of(file, options)) {

            lines.subscribe((data, error) -> {
                writer.write(data);
            }).join();

            writer.getPosition().whenComplete((size, error) -> {
                if (log.isDebugEnabled()) {
                    log.debug("Total [{} bytes] has been writed to file: {}", size, file.toString());
                }
            });
            return writer.getPosition();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    public CompletableFuture<Integer> write(Path file, Iterable<String> lines) {
        return write(file, lines, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    public CompletableFuture<Integer> write(Path file, Iterable<String> lines,
            StandardOpenOption... options) {
        if (log.isDebugEnabled()) {
            log.debug("Begin to write file: {}", file.toString());
        }
        try (CompletableFileWriter writer = CompletableFileWriter.of(file, options)) {
            lines.forEach(writer::write);

            writer.getPosition().whenComplete((size, error) -> {
                if (log.isDebugEnabled()) {
                    log.debug("Total [{} bytes] has been writed to file: {}", size, file.toString());
                }
            });
            return writer.getPosition();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
