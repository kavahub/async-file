package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class QueryMap<T, R> extends Query<R> {
    private final Query<T> query;
    private final Function<? super T, ? extends R> mapper;

    
    public QueryMap(Query<T> query, Function<? super T, ? extends R> mapper) {
        this.query = query;
        this.mapper = mapper;
    }
    
    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError) {
        return query.subscribe(data -> {
            onNext.accept(mapper.apply(data));
        }, onError);
    }
}
