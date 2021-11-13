package com.github.kavahub.file.reader;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
public final class ReadFile {
    /**
     * 
     */
    private Consumer<Throwable> whenError;

    /**
     * 
     */
    private Consumer<byte[]> whenRead;

    /**
     * 
     */
    private Consumer<Integer> whenComplete;

    private final AsynchronousFileChannel channel;
    private final int bufferSize;
    private boolean cancelled = false;

    private ReadFile(AsynchronousFileChannel channel, int bufferSize) {
        this.channel = channel;
        this.bufferSize = bufferSize;
    }

    public static ReadFile of(AsynchronousFileChannel channel, int bufferSize) {
        return new ReadFile(channel, bufferSize);
    }

    public ReadFile whenError(Consumer<Throwable> whenError) {
        this.whenError = whenError;
        return this;
    }
    
    public ReadFile whenRead(Consumer<byte[]> whenRead) {
        this.whenRead = whenRead;
        return this;
    }

    public ReadFile whenComplete(Consumer<Integer> whenComplete) {
        this.whenComplete = whenComplete;
        return this;
    }

    /**
     * 
     */
    public void cancel() {
        if (log.isDebugEnabled()) {
            log.debug("Cancel signal received");
        }
        cancelled = true;
    }

    /**
     * 
     * @return
     */
    public boolean isCancelled() {
        return cancelled;
    }

    public void read() {
        final AtomicInteger position = new AtomicInteger(0);
        final CompletionHandler<Integer, ByteBuffer> handler = new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if(result > 0) {
                    position.addAndGet(result);
                    buffer.flip();

                    byte[] data = new byte[result];
                    buffer.get(data);
                    whenRead.accept(data);
                   
                    if (!isCancelled()) {
                        read0(channel, position.get(), this);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Cancel file reading. [{} bytes] has been readed", position.get());
                        }
                        whenComplete.accept(position.get());
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("File read complete. [{} bytes] has been readed", position.get());
                    }
                    whenComplete.accept(position.get());
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                if (log.isErrorEnabled()) {
                    log.error("Failed while reading", exc);
                }
                whenError.accept(exc);
            }
        };
        read0(channel, position.get(), handler);
    }

    private void read0(AsynchronousFileChannel channel, int position, CompletionHandler<Integer, ByteBuffer> handler) {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        channel.read(buffer, position, buffer, handler);
    }
}
