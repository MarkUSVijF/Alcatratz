/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.technikumwien.bic4b18_01.common.assist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian
 * @param <T extends Serializable>
 */
public class LockAbleObject_privateCopy<T extends Serializable> {

    private T instance;
    private final ReentrantReadWriteLock lock;
    private static final Logger logger = Logger.getLogger(TraceHelper.getClassName());

    public LockAbleObject_privateCopy() throws ClassCastException, NullPointerException {
        this.instance = null;
        lock = new ReentrantReadWriteLock();
    }

    public void lock() {
        lock.writeLock().lock();
        lock.readLock().lock();
    }

    public void unlock() {
        if (lock.isWriteLockedByCurrentThread()) {
            lock.writeLock().unlock();
        }
        lock.readLock().unlock();
    }

    public void write(T instance) {
        if (lock.isWriteLockedByCurrentThread()) {
            directWrite(instance);
            return;
        }
        lock.writeLock().lock();
        try {
            directWrite(instance);
        } finally {
            lock.writeLock().unlock();
        }

    }

    private void directWrite(T instance) {
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(instance);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            this.instance = (T) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "{0} -> instance could not be written", Thread.currentThread().getName());
        } finally {
            try {
                oos.close();
            } catch (IOException | NullPointerException ex) {
            }
        }
    }

    public T read() {

        ObjectOutputStream oos = null;
        lock.readLock().lock();
        if (instance == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(instance);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "{0} -> instance could not be read", Thread.currentThread().getName());
            return null;
        } finally {
            try {
                oos.close();
            } catch (IOException | NullPointerException ex) {
            }
            lock.readLock().unlock();
        }
    }
}
