package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class QueryOf<T> extends Query<T> {
    private final T data;

    public QueryOf(T data) {
        this.data = data;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        try {
            onNext.accept(data);
        } catch (Exception e) {
            onError.accept(e);
        }

        return CompletableFuture.completedFuture(null);
    }
    
}
