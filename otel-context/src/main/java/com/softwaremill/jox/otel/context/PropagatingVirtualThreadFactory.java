package com.softwaremill.jox.otel.context;

import java.util.concurrent.ThreadFactory;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

/**
 * A virtual thread factory which propagates the OpenTelemetry {@link Context} when creating new
 * threads.
 *
 * <p>Should be set as the thread factory used by Jox's concurrency scopes, using the {@code
 * com.softwaremill.jox.structured.Scope#THREAD_FACTORY} scoped value:
 *
 * <pre>{@code
 * ScopedValue.where(Scope.THREAD_FACTORY, new PropagatingVirtualThreadFactory())
 *     .run(() -> supervised(scope -> { ... }));
 * }</pre>
 */
public class PropagatingVirtualThreadFactory implements ThreadFactory {
    private final ThreadFactory delegate = Thread.ofVirtual().factory();

    @Override
    public Thread newThread(Runnable r) {
        Context parentContext = Context.current();
        return delegate.newThread(
                () -> {
                    try (Scope ignored = parentContext.makeCurrent()) {
                        r.run();
                    }
                });
    }
}
