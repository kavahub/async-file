package com.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class QueryFilter<T> extends Query<T> {
    private final Query<T> query;
    private final Predicate<? super T> p;

    
    public QueryFilter(Query<T> query, Predicate<? super T> p) {
        this.query = query;
        this.p = p;
    }


    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super T, ? super Throwable> consumer) {
        return query.subscribe((item, err) -> {
            if(err != null) {
                consumer.accept(null, err);
                return;
            }
            if(p.test(item)) consumer.accept(item, null);
        });
    }
    
}
