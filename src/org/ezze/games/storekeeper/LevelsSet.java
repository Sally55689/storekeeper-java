package org.ezze.games.storekeeper;

import java.util.ArrayList;
import org.ezze.games.storekeeper.Level.LevelState;

/**
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public class LevelsSet {

    String name = "";
    ArrayList<Level> levels = null;
    int currentLevelIndex = -1;
    
    public LevelsSet() {
        
        this("");
    }
    
    public LevelsSet(String name) {
        
        this.name = name;
        levels = new ArrayList<Level>();
        currentLevelIndex = -1;
    }
    
    public String getName() {
        
        return name;
    }
    
    public boolean isEmpty() {
    
        return getLevelsCount() == 0;
    }
    
    public int getLevelsCount() {
        
        return levels == null ? 0 : levels.size();
    }
    
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
    
    public int getPlayableLevelsCount() {
        
        return getLevelsCountByState(LevelState.PLAYABLE);
    }
    
    public int getCurrentLevelIndex() {
        
        return currentLevelIndex;
    }
    
    public boolean setCurrentLevelByIndex(int levelIndex) {
        
        return setCurrentLevelByIndex(levelIndex, false);
    }
    
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
    
    public boolean goToPreviousLevel() {
        
        return goToPreviousLevel(false);
    }
    
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
    
    public boolean goToNextLevel() {
        
        return goToNextLevel(false);
    }
    
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
    
    public Level getCurrentLevel() {
        
        return getLevelByIndex(currentLevelIndex);
    }
    
    public Level getLevelByIndex(int levelIndex) {
    
        if (levels == null || levelIndex < 0 || levelIndex >= levels.size())
            return null;
        
        return levels.get(levelIndex);
    }
    
    public void addLevel(Level level) {
        
        if (level == null)
            return;
        
        levels.add(level);
        if (currentLevelIndex < 0)
            currentLevelIndex = 0;
    }
}