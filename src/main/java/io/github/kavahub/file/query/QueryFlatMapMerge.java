package io.github.kavahub.file.query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class QueryFlatMapMerge<T, R> extends Query<R> {
    private final Query<T> query;
    private final Function<? super T, ? extends Query<? extends R>> mapper;


    public QueryFlatMapMerge(Query<T> query, Function<? super T, ? extends Query<? extends R>> mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super R, ? super Throwable> consumer) {
        List<CompletableFuture<Void>> cfs = new ArrayList<>();
        return query
            .subscribe((item, err) -> {
                if (err != null) {
                    consumer.accept(null, err);
                    return;
                }
                cfs.add(mapper
                    .apply(item)
                    .subscribe(consumer::accept));
            })
            .thenCompose(ignore -> CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])));
    }
    
}
