package com.ecore.lock;

public interface IEntityLocker<T> {

    void lock(T ID, Runnable protectedCode) throws InterruptedException;
    void tryLock(T ID, int timeOut, Runnable protectedCode) throws InterruptedException;
}
