package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class QueryOfArray<T> extends Query<T> {
    private final T[] data;

    public QueryOfArray(T[] data) {
        this.data = data;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        try {
            Stream.of(data).forEach(e -> onNext.accept(e));
        } catch (Exception e) {
            onError.accept(e);
        }
        return CompletableFuture.completedFuture(null);
    }

}
