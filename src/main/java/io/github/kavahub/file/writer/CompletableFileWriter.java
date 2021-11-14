package io.github.kavahub.file.writer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import io.github.kavahub.file.ChannelHelper;

public class CompletableFileWriter implements AutoCloseable {
    private final AsynchronousFileChannel channel;
    private CompletableFuture<Integer> currentPosition;

    private CompletableFileWriter(AsynchronousFileChannel channel) {
        this.channel = channel;
        currentPosition = CompletableFuture.completedFuture(0);
    }

    public static CompletableFileWriter of(Path file, StandardOpenOption... options) throws IOException {
        return new CompletableFileWriter(AsynchronousFileChannel.open(file, options));
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
            return size.thenApply(length -> length + index);
        });

        return this.currentPosition;
    }

    protected CompletableFuture<Integer> write(ByteBuffer data, int position) {
        CompletableFuture<Integer> promise = new CompletableFuture<>();

        final WriteFile writer = WriteFile.of(channel)
            .whenError(throwable -> promise.completeExceptionally(throwable))
            .whenComplete(size -> promise.complete(size));

        // 开始写入
        writer.write(data, position);
        return promise;
    }
    
    @Override
    public void close() throws Exception {
        if(channel != null) {
            currentPosition.whenComplete((res, ex) ->
                ChannelHelper.close(channel)
            );
        }
    }
    
}
