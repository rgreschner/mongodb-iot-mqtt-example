package com.ragres.mongodb.iotexample.ui.activities;

import android.util.Log;

import com.ragres.mongodb.iotexample.misc.Logging;

import java.util.Queue;
import java.util.concurrent.Semaphore;

import dagger.internal.ArrayQueue;

/**
 * Pool for LogListItem instances.
 */
public class LogListItemPool {

    public static final int ITEM_INIT_SIZE = 10;
    public static final int ITEM_MAX_SIZE = 15;

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
            items.offer(createNew());
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
        if (items.size() < ITEM_MAX_SIZE) {
            item.clear();
            items.add(item);
        }
        //Log.d(Logging.TAG, "Pool count: " + items.size());
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

        LogListItem item = items.poll();
        if (null == item) {
            item = createNew();
        }

        itemsLock.release();
        //Log.d(Logging.TAG, "Pool count: " + items.size());
        return item;
    }

}
