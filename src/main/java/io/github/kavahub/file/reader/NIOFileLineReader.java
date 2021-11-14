package io.github.kavahub.file.reader;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;

import io.github.kavahub.file.query.Query;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
public class NIOFileLineReader {
    private static final Executor ASYNC_POOL = ForkJoinPool.commonPool();

    private NIOFileLineReader() {
    }

    /**
     * 
     * @param file
     * @return
     */
    public static Query<String> read(Path file) {
        return new FileReaderQuery(file);
    }

    /**
     * 
     */
    private static class FileReaderQuery extends Query<String> {
        private final Path file;

        public FileReaderQuery(Path file) {
            this.file = file;
        }

        @Override
        public CompletableFuture<Void> subscribe(BiConsumer<? super String, ? super Throwable> consumer) {
            CompletableFuture<Void> future = new CompletableFuture<>();

            FileReader reader = new FileReader(file, consumer, future);
            ASYNC_POOL.execute(reader);
            return future;
        }
    }

    /**
     * 
     */
    private static class FileReader implements Runnable {
        private final Path file;
        private final BiConsumer<? super String, ? super Throwable> consumer;
        private final CompletableFuture<Void> future;

        private boolean isCancel = false;

        /**
         * 
         */
        public void cancel() {
            if (log.isDebugEnabled()) {
                log.debug("Cancel signal received");
            }
            this.isCancel = true;
        }

        /**
         * 
         * @param file
         * @param consumer
         * @param future
         */
        public FileReader(Path file, BiConsumer<? super String, ? super Throwable> consumer, CompletableFuture<Void> future) {
            this.file = file;
            this.consumer = consumer;
            this.future = future;

            this.future.whenComplete((data, error) -> {
                this.cancel();
            });
        }

        @Override
        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("Begin to read file: {}", file.toString());
            }
            
            int totalLine = 0;
            try (BufferedReader br = Files.newBufferedReader(file)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (isCancel) {
                        if (log.isDebugEnabled()) {
                            log.debug("Cancel file reading. [{} lines] has been readed", totalLine);
                            break;
                        }
                    }
                    totalLine++;
                    consumer.accept(line, null);
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Failed while reading", e);
                }
                consumer.accept(null, e);
            }

            if (log.isDebugEnabled() && !isCancel) {
                log.debug("File read complete. [{} lines] has been readed", totalLine);
            }
            future.complete(null);
        }

    }
}
