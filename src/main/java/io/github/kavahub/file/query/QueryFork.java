package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class QueryFork<T> extends Query<T> {
    private final T[] data;

    public QueryFork(T[] data) {
        this.data = data;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return CompletableFuture.runAsync(() -> Stream.of(data).forEach(e -> onNext.accept(e)));
    }

    
}
