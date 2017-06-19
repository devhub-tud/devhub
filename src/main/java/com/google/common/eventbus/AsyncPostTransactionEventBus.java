package com.google.common.eventbus;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.UnitOfWork;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.BaseSessionEventListener;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Async EventBus that ensures the event is not propagated before the transaction active in the callee
 * of the post method has finished. It also ensures that the event handlers are invoked within a unit
 * of work.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@Singleton
public class AsyncPostTransactionEventBus extends EventBus {

    private final ConcurrentLinkedQueue<EventWithHandler> eventsToDispatch =
        new ConcurrentLinkedQueue<EventWithHandler>();
    private final Provider<EntityManager> entityManagerProvider;
    private final Provider<UnitOfWork> unitOfWorkProvider;
    private final Executor executor;

    public AsyncPostTransactionEventBus(Provider<EntityManager> entityManagerProvider, Provider<UnitOfWork> unitOfWorkProvider, ExecutorService executor) {
        this.entityManagerProvider = entityManagerProvider;
        this.unitOfWorkProvider = unitOfWorkProvider;
        this.executor = executor;
    }


    @Override
    public void post(Object event) {;
        entityManagerProvider.get().unwrap(Session.class)
            .addEventListeners(new BaseSessionEventListener() {
                @Override
                public void transactionCompletion(boolean successful) {
                    if (successful) {
                        AsyncPostTransactionEventBus.super.post(event);
                    }
                }
            });
    }

    @Override
    void enqueueEvent(Object event, EventHandler handler) {
        eventsToDispatch.offer(new EventWithHandler(event, handler));
    }

    /**
     * Dispatch {@code events} in the order they were posted, regardless of
     * the posting thread.
     */
    @Override
    @SuppressWarnings("deprecation") // only deprecated for external subclasses
    protected void dispatchQueuedEvents() {
        while (true) {
            EventWithHandler eventWithHandler = eventsToDispatch.poll();
            if (eventWithHandler == null) {
                break;
            }

            dispatch(eventWithHandler.event, eventWithHandler.handler);
        }
    }

    @Override
    void dispatch(final Object event, final EventHandler handler) {
        executor.execute(new Runnable() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void run() {
                UnitOfWork unitOfWork = unitOfWorkProvider.get();
                unitOfWork.begin();

                try {
                    AsyncPostTransactionEventBus.super.dispatch(event, handler);
                }
                finally {
                    unitOfWork.end();
                }
            }
        });
    }

}
