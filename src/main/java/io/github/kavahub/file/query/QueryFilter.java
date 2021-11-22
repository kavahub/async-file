package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class QueryFilter<T> extends Query<T> {
    private final Query<T> query;
    private final Predicate<? super T> p;

    
    public QueryFilter(Query<T> query, Predicate<? super T> p) {
        this.query = query;
        this.p = p;
    }


    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return query.subscribe(data-> {
            if(p.test(data)) onNext.accept(data);
        }, onError);
    }
    
}
