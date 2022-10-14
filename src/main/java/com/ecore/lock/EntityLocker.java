package com.ecore.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reusable utility class that provides synchronization mechanism similar to row-level DB locking.
 * <p>
 * The class is supposed to be used by the components that are responsible for managing storage and
 * caching of different type of entities in the application. EntityLocker itself does not deal with
 * the entities, only with the IDs (primary keys) of the entities.
 */
public class EntityLocker<T extends Comparable<T>> implements IEntityLocker<T> {

    public static final int NON_TIMEOUT = -1;
    private ConcurrentMap<T, EntityLockerWrapper> locks = new ConcurrentHashMap<>();

    /**
     * Max locked ID (for deadlock prevention)
     */
    private ThreadLocal<T> maxLockedId = new ThreadLocal<>();

    @Override
    public void lock(T ID, Runnable protectedCode) throws InterruptedException {
        tryLock(ID, NON_TIMEOUT, protectedCode);
    }

    /**
     * @param ID            Entity ID
     * @param timeOut       Time out for lock, -1 - no timeout
     * @param protectedCode Protected code to run
     */
    @Override
    public void tryLock(T ID, int timeOut, Runnable protectedCode) throws InterruptedException {

        // Deadlock prevention
        T prevID = maxLockedId.get();
        if (prevID != null && ID.compareTo(prevID) < 0) {
            throw new InterruptedException("Deadlock prevented: " + prevID + " > " + ID);
        }

        // Create lock
        EntityLockerWrapper entityLockerWrapper = locks.computeIfAbsent(ID,
            k -> new EntityLockerWrapper());
        boolean locked;
        if (timeOut == NON_TIMEOUT) {
            entityLockerWrapper.lock.lock();
            locked = entityLockerWrapper.lock.isLocked();
        } else {
            locked = entityLockerWrapper.lock.tryLock(timeOut, TimeUnit.MILLISECONDS);
        }

        if (locked) {
            maxLockedId.set(ID);
            try {
                // Run protected code
                protectedCode.run();
            } finally {
                if (entityLockerWrapper.lock.getQueueLength() == 1) {
                    locks.remove(ID);
                }
                entityLockerWrapper.lock.unlock();
                maxLockedId.set(prevID);
            }
        }
    }

    private class EntityLockerWrapper {

        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

        private EntityLockerWrapper addThreadInQueue() {
            numberOfThreadsInQueue.incrementAndGet();
            return this;
        }

        private int removeThreadFromQueue() {
            return numberOfThreadsInQueue.decrementAndGet();
        }

    }


}
