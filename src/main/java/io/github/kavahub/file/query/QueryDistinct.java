package io.github.kavahub.file.query;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 
 */
public final class QueryDistinct<T> extends Query<T> {
    private final Query<T> query;

    public QueryDistinct(Query<T> query) {
        this.query = query;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        final HashSet<T> mem = new HashSet<>();

        return query.subscribe(data -> {
            if(mem.add(data)) onNext.accept(data);
        }, onError);
    }
    
}
