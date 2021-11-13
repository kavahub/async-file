package com.github.kavahub.file.reader;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.github.kavahub.file.ChannelHelper;
import com.github.kavahub.file.query.Query;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileByteReaderQuery extends Query<byte[]> {
    private final Path file;
    private final int bufferSize;

    public FileByteReaderQuery(Path file, int bufferSize) {
        this.file = file;
        this.bufferSize = bufferSize;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super byte[], ? super Throwable> consumer) {
        final CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Begin to read file: {}", file.toString());
            }

            AsynchronousFileChannel channel = AsynchronousFileChannel.open(file, StandardOpenOption.READ);
            ReadFile reader = ReadFile.of(channel, bufferSize)
                    // 读取文件异常时
                    .whenError(throwable -> consumer.accept(null, throwable))
                    // 读取文件完成时
                    .whenComplete(size -> {
                        if (!future.isDone()) {
                            future.complete(null);
                        }
                    })
                    // 读取到文件数据时
                    .whenRead(bytes -> consumer.accept(bytes, null));

            // 开始读
            reader.read();
            future.whenComplete((data, err) -> {
                if (data == null || err != null) {
                    // 取消读操作
                    reader.cancel();

                    // 关闭channel
                    ChannelHelper.close(channel);
                }
            });
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return future;
    }
}
