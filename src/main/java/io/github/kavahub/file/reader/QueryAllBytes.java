package io.github.kavahub.file.reader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.github.kavahub.file.query.Query;

public class QueryAllBytes extends Query<byte[]> {
    private final Query<byte[]> query;


    public QueryAllBytes(Query<byte[]> query) {
        this.query = query;
    }


    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super byte[]> onNext, Consumer<? super Throwable> onError) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CompletableFuture<Void> future = query.subscribe(data -> {
                    try {
                        out.write(data);
                    } catch (IOException e) {
                        onError.accept(e);;
                    }
            }, onError);

            return future.whenComplete((data, error) -> {
                if (error != null) {
                    onError.accept(error);
                }
                onNext.accept(out.toByteArray());
            });
        } catch (IOException e) {
            onError.accept( e);
        }

        return CompletableFuture.completedFuture(null);
    }
}
