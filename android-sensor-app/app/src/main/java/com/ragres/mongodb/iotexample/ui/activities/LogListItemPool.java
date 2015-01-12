package com.ragres.mongodb.iotexample.ui.activities;

import java.util.Queue;
import java.util.concurrent.Semaphore;

import dagger.internal.ArrayQueue;

/**
 * Pool for LogListItem instances.
 */
public class LogListItemPool {

    public static final int ITEM_INIT_SIZE = 20;
    /**
     * Lock on pooled instances.
     */
    private Semaphore itemsLock = new Semaphore(1);

    /**
     * Queued instances.
     */
    private Queue<LogListItem> items = new ArrayQueue<>();

    /**
     * Public constructor.
     */
    public LogListItemPool() {
        for(int i = 0; i< ITEM_INIT_SIZE; ++i){
            items.add(createNew());
        }
    }

    /**
     * Create new instance for pool.
     * @return New instance.
     */
    private LogListItem createNew() {
        return new LogListItem();
    }

    /**
     * Add instance to pool.
     * @param item Instance to pool.
     */
    public void add(LogListItem item){
        if (null == item)
        {
            return ;
        }
        try {
            itemsLock.acquire();
        } catch (InterruptedException e) {

        }
        item.clear();
        items.add(item);
        itemsLock.release();
    }

    /**
     * Get item from pool.
     * @return Pooled instance.
     */
    public LogListItem get() {

        try {
            itemsLock.acquire();
        } catch (InterruptedException e) {

        }

        LogListItem item = null;
        if (items.size() < 1){
            item = createNew();
        } else {
            item = items.remove();
        }

        itemsLock.release();

        return item;
    }

}
