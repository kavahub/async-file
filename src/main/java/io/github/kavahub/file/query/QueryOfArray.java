package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class QueryOfArray<T> extends Query<T> {
    private final T[] data;

    public QueryOfArray(T[] data) {
        this.data = data;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super T, ? super Throwable> consumer) {
        Stream.of(data).forEach(e -> consumer.accept(e, null));
        return CompletableFuture.completedFuture(null);
    }
    
}
