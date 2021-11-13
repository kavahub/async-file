package com.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class QueryMap<T, R> extends Query<R> {
    private final Query<T> query;
    private final Function<? super T, ? extends R> mapper;

    
    public QueryMap(Query<T> query, Function<? super T, ? extends R> mapper) {
        this.query = query;
        this.mapper = mapper;
    }
    
    @Override
    public CompletableFuture<Void> subscribe(BiConsumer<? super R, ? super Throwable> consumer) {
        return query.subscribe((item, err) -> {
            if(err != null) {
                consumer.accept(null, err);
                return;
            }
            consumer.accept(mapper.apply(item), null);
        });
    }}
