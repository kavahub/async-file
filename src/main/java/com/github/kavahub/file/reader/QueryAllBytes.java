package com.github.kavahub.file.reader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.github.kavahub.file.query.Query;

public class QueryAllBytes extends Query<byte[]> {
    private final Query<byte[]> query;


    public QueryAllBytes(Query<byte[]> query) {
        this.query = query;
    }


    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super byte[], ? super Throwable> consumer) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CompletableFuture<Void> future = query.subscribe((data, error) -> {
                if (error != null) {
                    consumer.accept(null, error);
                }

                if (data != null) {
                    try {
                        out.write(data);
                    } catch (IOException e) {
                        consumer.accept(null, e);
                    }
                }
            });

            return future.whenComplete((data, error) -> {
                if (error!= null) {
                    consumer.accept(null, error);
                }

                consumer.accept(out.toByteArray(), null);
            });
        } catch (IOException e) {
            consumer.accept(null, e);
        }

        return CompletableFuture.completedFuture(null);
    }


}
