package fr.ensibs.robots.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Generic object pool for reusing objects to reduce GC pressure.
 * 
 * <p>MISSION 5.1: Object Pooling
 * Prevents Java Garbage Collector lags during intense firefights
 * by reusing objects instead of creating new ones.
 * 
 * <p>Usage:
 * <pre>
 * ObjectPool&lt;Particle&gt; pool = new ObjectPool&lt;&gt;(() -&gt; new Particle(), 100);
 * Particle p = pool.acquire();
 * // ... use particle ...
 * pool.release(p);
 * </pre>
 * 
 * @param <T> the type of object to pool
 * @author Robot Wars Team
 */
public class ObjectPool<T>
{
    private final List<T> available;
    private final List<T> inUse;
    private final Supplier<T> factory;
    private final int maxSize;
    
    /**
     * Constructor
     * 
     * @param factory factory function to create new objects when pool is empty
     * @param initialSize initial pool size
     * @param maxSize maximum pool size (0 = unlimited)
     */
    public ObjectPool(Supplier<T> factory, int initialSize, int maxSize)
    {
        this.factory = factory;
        this.maxSize = maxSize;
        this.available = new ArrayList<>();
        this.inUse = new ArrayList<>();
        
        // Pre-populate pool
        for (int i = 0; i < initialSize; i++) {
            available.add(factory.get());
        }
    }
    
    /**
     * Constructor with unlimited pool size.
     * 
     * @param factory factory function to create new objects
     * @param initialSize initial pool size
     */
    public ObjectPool(Supplier<T> factory, int initialSize)
    {
        this(factory, initialSize, 0);
    }
    
    /**
     * Acquire an object from the pool.
     * 
     * @return an object from the pool (or newly created if pool is empty)
     */
    public T acquire()
    {
        T obj;
        if (available.isEmpty()) {
            // Pool is empty, create new object
            obj = factory.get();
        } else {
            // Reuse object from pool
            obj = available.remove(available.size() - 1);
        }
        
        inUse.add(obj);
        return obj;
    }
    
    /**
     * Release an object back to the pool.
     * 
     * @param obj the object to release
     */
    public void release(T obj)
    {
        if (obj == null) {
            return;
        }
        
        if (inUse.remove(obj)) {
            if (maxSize == 0 || available.size() < maxSize) {
                available.add(obj);
            }
            // If pool is full, object is discarded (will be GC'd)
        }
    }
    
    /**
     * Release all objects currently in use back to the pool.
     */
    public void releaseAll()
    {
        List<T> toRelease = new ArrayList<>(inUse);
        for (T obj : toRelease) {
            release(obj);
        }
    }
    
    /**
     * Get the number of available objects in the pool.
     * 
     * @return available count
     */
    public int getAvailableCount()
    {
        return available.size();
    }
    
    /**
     * Get the number of objects currently in use.
     * 
     * @return in-use count
     */
    public int getInUseCount()
    {
        return inUse.size();
    }
    
    /**
     * Clear the pool (release all objects).
     */
    public void clear()
    {
        available.clear();
        inUse.clear();
    }
}

