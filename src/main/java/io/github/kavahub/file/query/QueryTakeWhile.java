package io.github.kavahub.file.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class QueryTakeWhile<T> extends Query<T> {
    private final Query<T> query;
    private final Predicate<? super T> p;
    private CompletableFuture<Void> subscription;

    /**
     * After cancellation of upstream subscription we may still receive updates on
     * consumer. To avoid propagation we must check if we have already cancelled the
     * subscription. But we need a different flag from the CF subscription because
     * this field may not be initialized yet on first check of the subscribe
     * callback.
     */
    private boolean finished = false;

    public QueryTakeWhile(Query<T> query, Predicate<? super T> p) {
        this.query = query;
        this.p = p;
    }

    @Override
    public CompletableFuture<Void> subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        subscription = query.subscribe(data -> {
            /**
             * After cancellation of upstream subscription we may still receive updates on
             * consumer. To avoid propagation we must check if we have already cancelled the
             * subscription.
             */
            if (finished) {
                if (subscription != null && !subscription.isDone())
                    subscription.complete(null);
                return;
            }

            if (p.test(data)) {
                onNext.accept(data);
            } else {
                if (!finished) {
                    finished = true;
                    // We need this guard because we could meet conditions
                    // to finish processing, yet the outer subscribe() invocation
                    // has not returned and the subscription is still null.
                    if (subscription != null)
                        subscription.complete(null);
                }
            }
        }, onError);
        return subscription;
    }
}
