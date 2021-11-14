package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class QuerySkip<T> extends Query<T> {
    private final Query<T> query;
    private final int skip;
    private int count = 0;

    
    public QuerySkip(Query<T> query, int skip) {
        this.query = query;
        this.skip = skip;
    }


    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super T, ? super Throwable> consumer) {
        return query.subscribe((item, err) -> {
            if(err != null) {
                consumer.accept(null, err);
                return;
            }
            if(count >= skip) consumer.accept(item, err);
            else count++;
        });
    }
}
