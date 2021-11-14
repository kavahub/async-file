package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class QueryOnNext<T> extends Query<T> {
    private final Query<T> query;
    private final BiConsumer<? super T, ? super Throwable> action;

    
    public QueryOnNext(Query<T> query, BiConsumer<? super T, ? super Throwable> action) {
        this.query = query;
        this.action = action;
    }


    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super T, ? super Throwable> consumer) {
        return query.subscribe((item, err) -> {
            action.accept(item, err);
            consumer.accept(item, err);
        });
    }
}
