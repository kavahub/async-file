package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class QueryOnError<T> extends Query<T> {
    private final Query<T> query;
    private final Consumer<? super Throwable> action;

    
    public QueryOnError(Query<T> query, Consumer<? super Throwable> action) {
        this.query = query;
        this.action = action;
    }


    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return query.subscribe(onNext, err -> {
            action.accept(err);
            onError.accept(err);
        });
    }

}
