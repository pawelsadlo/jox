package com.softwaremill.jox.otel.context;

import com.softwaremill.jox.structured.ForkDecorator;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import java.util.concurrent.Callable;

/**
 * A {@link ForkDecorator} which propagates the OpenTelemetry {@link Context} to forks: the context
 * which is current when a fork is started becomes current in the fork's body.
 *
 * <p>Should be set as the decorator used by concurrency scopes, through the {@code
 * com.softwaremill.jox.structured.Scope#FORK_DECORATOR} scoped value:
 *
 * <pre>{@code
 * ScopedValue.where(Scope.FORK_DECORATOR, new PropagatingForkDecorator())
 *     .call(() -> supervised(scope -> { ... }));
 * }</pre>
 */
public class PropagatingForkDecorator implements ForkDecorator {
    @Override
    public <T> Callable<T> decorate(Callable<T> f) {
        // captured on the thread which starts the fork, where the context is still current
        Context forkingContext = Context.current();
        return () -> {
            try (Scope ignored = forkingContext.makeCurrent()) {
                return f.call();
            }
        };
    }
}
