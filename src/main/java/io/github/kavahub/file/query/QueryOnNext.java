package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class QueryOnNext<T> extends Query<T> {
    private final Query<T> query;
    private final Consumer<? super T> action;

    
    public QueryOnNext(Query<T> query, Consumer<? super T> action) {
        this.query = query;
        this.action = action;
    }


    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return query.subscribe(data -> {
            action.accept(data);
            onNext.accept(data);
        }, onError);
    }

}
