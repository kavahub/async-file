package io.github.kavahub.file.query;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 
 */
public final class QueryDistinct<T> extends Query<T> {
    private final Query<T> query;

    public QueryDistinct(Query<T> query) {
        this.query = query;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super T, ? super Throwable> cons) {
        final HashSet<T> mem = new HashSet<>();
        return query.subscribe((item, err) -> {
            if(err != null) {
                cons.accept(null, err);
                return;
            }
            if(mem.add(item)) cons.accept(item, null);
        });
    }
    
}
