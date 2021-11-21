package io.github.kavahub.file.writer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import io.github.kavahub.file.ChannelHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompletableFileWriter implements AutoCloseable {
    private final AsynchronousFileChannel channel;
    private CompletableFuture<Integer> currentPosition;

    private CompletableFileWriter(AsynchronousFileChannel channel) {
        this.channel = channel;
        currentPosition = CompletableFuture.completedFuture(0);
    }

    public static CompletableFileWriter of(Path file, StandardOpenOption... options) {
        if (log.isDebugEnabled()) {
            log.debug("Begin to write file: {}", file.toString());
        }

        AsynchronousFileChannel channel;
        try {
            channel = AsynchronousFileChannel.open(file, options);
            return new CompletableFileWriter(channel);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    public CompletableFuture<Integer> getPosition() {
        return currentPosition;
    }

    public CompletableFuture<Integer> write(String str) {
        return write(str.getBytes(StandardCharsets.UTF_8));
    }

    public CompletableFuture<Integer> write(byte[] bytes) {
        return write(ByteBuffer.wrap(bytes));
    }

    public CompletableFuture<Integer> write(ByteBuffer data) {
        this.currentPosition = currentPosition.thenCompose(index -> {
            CompletableFuture<Integer> size = write(data, index);
            return size.thenApply(length -> {
                final int total = length + index;

                if (log.isDebugEnabled()) {
                    log.debug("[{} bytes] has been writed", total);
                }

                return total;
            });
        });

        return this.currentPosition;
    }

    protected CompletableFuture<Integer> write(ByteBuffer data, int position) {
        CompletableFuture<Integer> promise = new CompletableFuture<>();

        final WriteFile writer = WriteFile.of(channel).whenError(throwable -> {
            if (log.isErrorEnabled()) {
                log.error("Failed while writing", throwable);
            }
            
            promise.completeExceptionally(throwable);
        }).whenComplete(size -> {
            promise.complete(size);
        });

        // 开始写入
        writer.write(data, position);
        return promise;
    }

    @Override
    public void close() {
        if (channel != null) {
            currentPosition.whenComplete((res, ex) -> ChannelHelper.close(channel));
        }
    }

}
