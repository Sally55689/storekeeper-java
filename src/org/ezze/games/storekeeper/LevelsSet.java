package org.ezze.games.storekeeper;

import java.util.ArrayList;
import org.ezze.games.storekeeper.Level.LevelState;

/**
 * This class represents a set of loaded levels.
 * 
 * It can securely access both playable and corrupted levels.
 * Look at {@link Level#LevelState} for possible level's states.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public class LevelsSet {

    /**
     * Set's name.
     */
    String name = "";
    
    /**
     * A list of set's levels.
     */
    ArrayList<Level> levels = null;
    
    /**
     * An index of set's currently selected level.
     * 
     * Default value {@code -1} means that no level is selected.
     */
    int currentLevelIndex = -1;
    
    /**
     * Constructs empty levels' set without name.
     * 
     * @see #LevelsSet(java.lang.String)
     */
    public LevelsSet() {
        
        this("");
    }
    
    /**
     * Constructs empty levels' set with specified name.
     * 
     * @param name
     *      Set's name.
     */
    public LevelsSet(String name) {
        
        this.name = name;
        levels = new ArrayList<Level>();
        currentLevelIndex = -1;
    }
    
    /**
     * Retrieves set's name.
     * 
     * @return 
     *      Set's name.
     */
    public String getName() {
        
        return name;
    }
    
    /**
     * Checks whether set has no levels.
     * 
     * @return 
     *      {@code true} if set is empty, {@code false} otherwise.
     * @see #getLevelsCount()
     */
    public boolean isEmpty() {
    
        return getLevelsCount() == 0;
    }
    
    /**
     * Retrieves set levels' count.
     * 
     * @return
     *      Count of levels in the set.
     * @see #isEmpty()
     * @see #getLevelsCountByState(org.ezze.games.storekeeper.Level.LevelState)
     * @see #getPlayableLevelsCount()
     */
    public int getLevelsCount() {
        
        return levels == null ? 0 : levels.size();
    }
    
    /**
     * Retrieves set levels' count with specified state.
     * 
     * @param levelState
     *      Level's state.
     * @return
     *      Count of levels with specified state in the set.
     * @see #getLevelsCount()
     * @see #getPlayableLevelsCount()
     */
    public int getLevelsCountByState(LevelState levelState) {
        
        if (levels == null || levels.isEmpty() || levelState == null)
            return 0;
        
        int levelsCount = 0;
        for (Level level : levels) {
            
            if (level.getState() == levelState)
                levelsCount++;
        }
        
        return levelsCount;
    }
    
    /**
     * Retrieves set playable levels' count.
     * 
     * @return 
     *      Count of playable levels in the set.
     * @see #getLevelsCount()
     * @see #getLevelsCountByState(org.ezze.games.storekeeper.Level.LevelState)
     */
    public int getPlayableLevelsCount() {
        
        return getLevelsCountByState(LevelState.PLAYABLE);
    }
    
    /**
     * Retrieves an index of currently selected level.
     * 
     * @return 
     *      Index of currently selected level.
     * @see #getCurrentLevel()
     */
    public int getCurrentLevelIndex() {
        
        return currentLevelIndex;
    }
    
    /**
     * Selects a level by specified index.
     * 
     * @param levelIndex
     *      An index of level to select.
     * @return
     *      {@code true} if level has been selected, {@code false} otherwise.
     */
    public boolean setCurrentLevelByIndex(int levelIndex) {
        
        return setCurrentLevelByIndex(levelIndex, false);
    }

    /**
     * Selects a level by specified index.
     * 
     * @param levelIndex
     *      An index of level to select.
     * @param playable
     *      If it's set to {@code true} then only playable level can be selected.
     * @return
     *      {@code true} if level has been selected, {@code false} otherwise.
     */
    public boolean setCurrentLevelByIndex(int levelIndex, boolean playable) {
        
        if (levels == null || levelIndex < 0 || levelIndex >= levels.size()) {
            
            currentLevelIndex = -1;
            return false;
        }
        
        if (playable && getPlayableLevelsCount() == 0) {
            
            currentLevelIndex = -1;
            return false;
        }
        
        currentLevelIndex = levelIndex;
        return true;
    }
    
    /**
     * Selects first playable level in the set.
     * 
     * @return 
     *      {@code true} if playable level is found and selected, {@code false} otherwise.
     */
    public boolean setCurrentLevelByFirstPlayable() {
        
        if (levels == null || levels.isEmpty()) {
            
            currentLevelIndex = -1;
            return false;
        }
        
        int levelIndex = 0;
        while (levelIndex < levels.size()) {
            
            Level level = levels.get(levelIndex);
            if (level.isPlayable()) {
                
                currentLevelIndex = levelIndex;
                return true;
            }
            
            levelIndex++;
        }
        
        currentLevelIndex = -1;
        return false;
    }
    
    /**
     * Selects previous level of the set.
     * 
     * @return 
     *      {@code true} if level has been selected, {@code false} otherwise.
     */
    public boolean goToPreviousLevel() {
        
        return goToPreviousLevel(false);
    }
    
    /**
     * Selects previous level of the set.
     * 
     * @param playable
     *      If it's set to {@code true} then only previous playable level will be selected.
     * @return
     *      {@code true} if level has been selected, {@code false} otherwise.
     */
    public boolean goToPreviousLevel(boolean playable) {
        
        if (levels == null || currentLevelIndex < 0 || currentLevelIndex >= levels.size()) {
            
            currentLevelIndex = -1;
            return false;
        }
        
        if (playable && getPlayableLevelsCount() == 0) {
            
            currentLevelIndex = -1;
            return false;
        }
        
        do {
            
            currentLevelIndex--;
            if (currentLevelIndex < 0)
                currentLevelIndex = levels.size() - 1;
        }
        while (playable && !getCurrentLevel().isPlayable());
        
        return true;
    }
    
    /**
     * Selects next level of the set.
     * 
     * @return 
     *      {@code true} if level has been selected, {@code false} otherwise.
     */
    public boolean goToNextLevel() {
        
        return goToNextLevel(false);
    }
    
    /**
     * Selects next level of the set.
     * 
     * @param playable
     *      If it's set to {@code true} then only next playable level will be selected.
     * @return
     *      {@code true} if level has been selected, {@code false} otherwise.
     */
    public boolean goToNextLevel(boolean playable) {
        
        if (levels == null || currentLevelIndex < 0 || currentLevelIndex >= levels.size()) {
            
            currentLevelIndex = -1;
            return false;
        }
        
        if (playable && getPlayableLevelsCount() == 0) {
            
            currentLevelIndex = -1;
            return false;
        }
        
        do {
            
            currentLevelIndex++;
            if (currentLevelIndex >= levels.size())
                currentLevelIndex = 0;
        }
        while (playable && !getCurrentLevel().isPlayable());
            
        return true;
    }
    
    /**
     * Retrieves a reference to currently selected level's instance.
     * 
     * @return 
     *      Level's instance or {@code null} if no level is selected.
     * @see #getLevelByIndex(int)
     * @see #getCurrentLevelIndex()
     */
    public Level getCurrentLevel() {
        
        return getLevelByIndex(currentLevelIndex);
    }
    
    /**
     * Retrieves a reference to level specified by its index.
     * 
     * @param levelIndex
     *      Level's index.
     * @return
     *      Level's instance or {@code null} if {@code levelIndex} is invalid.
     * @see #getCurrentLevel()
     */
    public Level getLevelByIndex(int levelIndex) {
    
        if (levels == null || levelIndex < 0 || levelIndex >= levels.size())
            return null;
        
        return levels.get(levelIndex);
    }
    
    /**
     * Adds new level to the set.
     * 
     * @param level 
     *      New level's instance.
     */
    public void addLevel(Level level) {
        
        if (level == null)
            return;
        
        levels.add(level);
        if (currentLevelIndex < 0)
            currentLevelIndex = 0;
    }
}