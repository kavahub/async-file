package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class QuerySkip<T> extends Query<T> {
    private final Query<T> query;
    private final int skip;
    private int count = 0;

    
    public QuerySkip(Query<T> query, int skip) {
        this.query = query;
        this.skip = skip;
    }


    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return query.subscribe(data -> {
            if(count >= skip) onNext.accept(data);
            else count++;
        }, onError);
    }
}
