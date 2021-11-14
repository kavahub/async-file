package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class QueryOf<T> extends Query<T> {
    private final T data;

    public QueryOf(T data) {
        this.data = data;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super T, ? super Throwable> consumer) {
        consumer.accept(data, null);
        return CompletableFuture.completedFuture(null);
    }
    
}
