package org.ezze.games.storekeeper;

/**
 * This interface has a method to implement to
 * be invoked after game's level will have been completed.
 * 
 * A class implementing this interface must be passed as an argument to
 * {@link Game#Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics, org.ezze.games.storekeeper.LevelCompletionListener)}
 * during game instance's creation.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.1
 * @see Game#Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics, org.ezze.games.storekeeper.LevelCompletionListener)
 */
public interface LevelCompletionListener {
    
    /**
     * Describes actions to do after level will have been completed.
     * 
     * @param gameLevel
     *      An instance of completed level {@link Level}.
     */
    public void levelCompleted(Level gameLevel);
}