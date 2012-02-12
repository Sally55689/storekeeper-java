package org.ezze.games.storekeeper;

import java.util.HashMap;

/**
 * Singleton class required to synchronize blocks dedicated
 * to different purporses.
 * 
 * In order to lock some block of code from executing by
 * concurrent thread one should mark this block by
 * {@code synchronized(lockObject)} where {@code lockObject}
 * is a lock object that can be retrieved using {@link #getLock(java.lang.String)}
 * method of this class.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.2
 */
public class SyncLock {

    /**
     * Single instance of this class.
     */
    private static SyncLock instance = null;
    
    /**
     * This one stores lock objects used for synchronization and that
     * can be retrieved by {@link #getLock(java.lang.String)} method.
     */
    private HashMap<String, Object> locks = new HashMap<String, Object>();
    
    /**
     * Retrieves a reference to single instance of this class.
     * 
     * @return 
     *      Instance of {@link #SyncLock()}.
     */
    public static SyncLock getInstance() {
        
        if (instance == null)
            instance = new SyncLock();
        return instance;
    }
    
    /**
     * Retrieves a lock object for unique string identifier.
     * 
     * @param lockName
     *      Unique string identifier.
     * @return
     *      Lock object that can be used for synchronization purporses
     *      in the following manner:
     *      <p>{@code synchronized(lockObject) { // block's code... }}</p>
     */
    public Object getLock(String lockName) {
        
        if (lockName == null || lockName.isEmpty())
            return null;
        
        if (!locks.containsKey(lockName))
            locks.put(lockName, new Object());
        return locks.get(lockName);
    }
}