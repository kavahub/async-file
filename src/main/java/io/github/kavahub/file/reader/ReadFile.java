package io.github.kavahub.file.reader;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 
 */
public final class ReadFile {
    /**
     * 
     */
    private Consumer<Throwable> whenReadError;

    /**
     * 
     */
    private Consumer<Throwable> whenHandleDataError;

    /**
     * 
     */
    private BiConsumer<byte[], Integer> whenRead;

    /**
     * 
     */
    private Consumer<Integer> whenFinish;

    /**
    * 
    */
    private Consumer<Integer> whenCancel;

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

    public ReadFile whenReadError(Consumer<Throwable> whenReadError) {
        this.whenReadError = whenReadError;
        return this;
    }

    public ReadFile whenHandleDataError(Consumer<Throwable> whenHandleDataError) {
        this.whenHandleDataError = whenHandleDataError;
        return this;
    }

    public ReadFile whenRead(BiConsumer<byte[], Integer> whenRead) {
        this.whenRead = whenRead;
        return this;
    }

    public ReadFile whenFinish(Consumer<Integer> whenFinish) {
        this.whenFinish = whenFinish;
        return this;
    }

    public ReadFile whenCancel(Consumer<Integer> whenCancel) {
        this.whenCancel = whenCancel;
        return this;
    }

    /**
     * 
     */
    public void cancel() {
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
                if (result > 0) {
                    position.addAndGet(result);
                    buffer.flip();

                    byte[] data = new byte[result];
                    buffer.get(data);

                    try {
                        whenRead.accept(data, position.get());
                    } catch (Exception e) {
                        whenHandleDataError.accept(e);
                    }

                    if (!isCancelled()) {
                        read0(channel, position.get(), this);
                    } else {
                        whenCancel.accept(position.get());
                    }
                } else {
                    whenFinish.accept(position.get());
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                whenReadError.accept(exc);
            }
        };
        read0(channel, position.get(), handler);
    }

    private void read0(AsynchronousFileChannel channel, int position, CompletionHandler<Integer, ByteBuffer> handler) {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        channel.read(buffer, position, buffer, handler);
    }
}
