package io.github.kavahub.file.query;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 
 */
public abstract class Query<T> {
    public abstract CompletableFuture<Void> subscribe(Consumer<? super T> onNext,
            Consumer<? super Throwable> onError);

    public static <U> Query<U> of(U data) {
        return new QueryOf<>(data);
    }

    /**
     * Returns an asynchronous sequential ordered query whose elements are the
     * specified values in data parameter.
     * 
     * @param <U>
     * @param data
     * @return
     */
    public static <U> Query<U> of(U[] data) {
        return new QueryOfArray<>(data);
    }

    /**
     * Returns an asynchronous sequential ordered query whose elements are the
     * specified values in the Iterator parameter.
     * 
     * @param <U>
     * @param iter
     * @return
     */
    public static <U> Query<U> of(Iterator<U> iter) {
        return new QueryOfIterator<>(iter);
    }

    /**
     * Returns an asynchronous sequential ordered query whose elements are the
     * specified values in data parameter running on thread pool.
     * 
     * @param <U>
     * @param data
     * @return
     */
    public static <U> Query<U> fork(U[] data) {
        return new QueryFork<>(data);
    }

    /**
     * Returns a new asynchronous query emitting the same items of this query,
     * additionally performing the provided action on each element as elements are
     * consumed from the resulting query.
     * 
     * @param action
     * @return
     */
    public final Query<T> onNext(Consumer<? super T> action) {
        return new QueryOnNext<>(this, action);
    }

    public final Query<T> onError(Consumer<? super Throwable> action) {
        return new QueryOnError<>(this, action);
    }

    /**
     * Returns a new asynchronous query consisting of the remaining elements of this
     * query after discarding the first {@code n} elements of the query.
     * 
     * @param n
     * @return
     */
    public final Query<T> skip(int n) {
        return new QuerySkip<>(this, n);
    }

    /**
     * Returns an asynchronous query consisting of the elements of this query that
     * match the given predicate.
     * 
     * @param p
     * @return
     */
    public final Query<T> filter(Predicate<? super T> p) {
        return new QueryFilter<>(this, p);
    }

    /**
     * Returns an asynchronous query consisting of the results of applying the given
     * function to the elements of this query.
     * 
     * @param <R>
     * @param mapper
     * @return
     */
    public final <R> Query<R> map(Function<? super T, ? extends R> mapper) {
        return new QueryMap<>(this, mapper);
    }

    /**
     * Returns a query consisting of the distinct elements (according to
     * {@link Object#equals(Object)}) of this query.
     * 
     * @return
     */
    public final Query<T> distinct() {
        return new QueryDistinct<>(this);
    }

    /**
     * Returns a query consisting of the longest prefix of elements taken from this
     * query that match the given predicate.
     * 
     * @param predicate
     * @return
     */
    public final Query<T> takeWhile(Predicate<? super T> predicate) {
        return new QueryTakeWhile<>(this, predicate);
    }

    /**
     * Returns an asynchronous query consisting of the results of replacing each
     * element of this query with the contents of a mapped query produced by
     * applying the provided mapping function to each element. It waits for the
     * inner flow to complete before starting to collect the next one.
     * 
     * @param <R>
     * @param mapper
     * @return
     */
    public final <R> Query<R> flatMapConcat(Function<? super T, ? extends Query<? extends R>> mapper) {
        return new QueryFlatMapConcat<T, R>(this, mapper);
    }

    /**
     * 
     * @param <R>
     * @param mapper
     * @return
     */
    public final <R> Query<R> flatMapMerge(Function<? super T, ? extends Query<? extends R>> mapper) {
        return new QueryFlatMapMerge<T, R>(this, mapper);
    }

    /**
     * 
     */
    public final void blockingSubscribe() {
        this.subscribe(item -> {
        }, er -> {
        }).join(); // In both previous cases cf will raise an exception.
    }
}
