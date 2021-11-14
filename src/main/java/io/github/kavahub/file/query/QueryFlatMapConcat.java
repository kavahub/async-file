package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class QueryFlatMapConcat<T, R> extends Query<R> {
    private final Query<T> upstream;
    private final Function<? super T, ? extends Query<? extends R>> mapper;


    public QueryFlatMapConcat(Query<T> upstream, Function<? super T, ? extends Query<? extends R>> mapper) {
        this.upstream = upstream;
        this.mapper = mapper;
    }

    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super R, ? super Throwable> consumer) {
        return upstream.subscribe((item, err) -> {
            if(err != null) {
                consumer.accept(null, err);
                return;
            }
            mapper
                .apply(item)
                .subscribe(consumer::accept)
                .join(); // !!!! Replace this by Continuation !!!
        });
    }
    
}
