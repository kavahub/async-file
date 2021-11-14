package io.github.kavahub.file.writer;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WriteFile {
    /**
     * 
     */
    private Consumer<Throwable> whenError;

    /**
     * 
     */
    private Consumer<Integer> whenComplete;

    private final AsynchronousFileChannel channel;

    private WriteFile(AsynchronousFileChannel channel) {
        this.channel = channel;
    }

    public static WriteFile of(AsynchronousFileChannel channel) {
        return new WriteFile(channel);
    }

    public WriteFile whenError(Consumer<Throwable> whenError) {
        this.whenError = whenError;
        return this;
    }

    public WriteFile whenComplete(Consumer<Integer> whenComplete) {
        this.whenComplete = whenComplete;
        return this;
    }

    public void write(ByteBuffer data, int position) {
        channel.write(data, position, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                whenComplete.accept(result);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                if (log.isErrorEnabled()) {
                    log.error("Failed while writing", exc);
                }
                whenError.accept(exc);
            }
        });
    }
}
