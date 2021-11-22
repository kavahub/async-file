package io.github.kavahub.file.query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class QueryFlatMapMerge<T, R> extends Query<R> {
    private final Query<T> query;
    private final Function<? super T, ? extends Query<? extends R>> mapper;


    public QueryFlatMapMerge(Query<T> query, Function<? super T, ? extends Query<? extends R>> mapper) {
        this.query = query;
        this.mapper = mapper;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super R> onNext, Consumer<? super Throwable> onError) {
        List<CompletableFuture<Void>> cfs = new ArrayList<>();
        return query
            .subscribe(data -> {
                cfs.add(mapper
                    .apply(data)
                    .subscribe(onNext::accept, onError));
            }, onError)
            .thenCompose(ignore -> CompletableFuture.allOf(cfs.toArray(new CompletableFuture[cfs.size()])));
    }
    
}
