package io.github.kavahub.file.query;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class QueryOfIterator<T> extends Query<T> {
    private final Iterator<T> iter;

    public QueryOfIterator(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        try {
            while(iter.hasNext()) {
                onNext.accept(iter.next());
            }
        } catch (Exception e) {
            onError.accept(e);
        }

        return CompletableFuture.completedFuture(null);
    }

    
}
