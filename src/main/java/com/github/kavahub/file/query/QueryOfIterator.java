package com.github.kavahub.file.query;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class QueryOfIterator<T> extends Query<T> {
    private final Iterator<T> iter;

    public QueryOfIterator(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super T, ? super Throwable> consumer) {
        while(iter.hasNext()) {
            consumer.accept(iter.next(), null);
        }
        return CompletableFuture.completedFuture(null);
    }

    
}
