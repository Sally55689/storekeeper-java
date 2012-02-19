package org.ezze.games.storekeeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ezze.games.storekeeper.Level.LevelSize;
import org.ezze.games.storekeeper.Level.LevelState;
import org.ezze.utils.io.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a set of loaded levels.
 * 
 * It can securely access both playable and corrupted levels.
 * Look at {@link LevelState} for possible level's states.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.3
 */
public class LevelsSet {
    
    /**
     * Level methods' result enumeration.
     * 
     * Used as a result of methods loading and reinitializng levels' sets.
     * 
     * @see #load(java.lang.String)
     * @see #reinitialize()
     */
    public enum LoadState {
        
        /**
         * Shows that levels' set has been successfully loaded or reinitialized.
         */
        SUCCESS,
        
        /**
         * Shows that at least one level of a set is not valid and
         * therefore is not loaded or reinitialized.
         */
        WARNING,
        
        /**
         * Shows that levels' set couldn't be loaded or reinitialized.
         */
        ERROR,
        
        /**
         * Shows that levels' set is not loaded from any source.
         */
        NOT_LOADED,
    }
    
    /**
     * Set's current load state.
     */
    LoadState loadState = LoadState.NOT_LOADED;
    
    /**
     * A reference to game's configuration.
     */
    Configuration configuration = null;
    
    /**
     * Set's source file's name.
     */
    String source = null;

    /**
     * Set's name.
     */
    String name = "";
    
    /**
     * A list of set's levels.
     */
    ArrayList<Level> levels = new ArrayList<Level>();
    
    /**
     * An index of set's currently selected level.
     * 
     * Default value {@code -1} means that no level is selected.
     */
    int currentLevelIndex = -1;
    
    /**
     * Constructs empty levels' set.
     * 
     * @param configuration
     *      A reference to game's configuration.
     * @see #LevelsSet(org.ezze.games.storekeeper.Configuration, java.lang.String)
     */
    public LevelsSet(Configuration configuration) {
        
        this(configuration, null);
    }
    
    /**
     * Constructs levels' set from specified source file.
     * 
     * @param configuration
     *      A reference to game's configuration.
     * @param source 
     *      Set's source file's name.
     * @see #LevelsSet(org.ezze.games.storekeeper.Configuration)
     */
    public LevelsSet(Configuration configuration, String source) {
        
        this.configuration = configuration;
        this.source = source;
        if (this.source != null)
            load(this.source);
    }
    
    /**
     * Retrieves set's current load state.
     * 
     * @return 
     *      Set's current load state.
     */
    public LoadState getLoadState() {
        
        return loadState;
    }
    
    /**
     * Loads levels' set from source file pointed by a name.
     * 
     * @param source
     *      Set's source file's name.
     * @return
     *      Set's load result.
     */
    public final LoadState load(String source) {
        
        if (configuration == null || source == null) {
         
            loadState = LoadState.ERROR;
            return loadState;
        }
        
        File levelsSetFile = new File(source);
        if (!levelsSetFile.exists() || !levelsSetFile.isFile()) {
         
            loadState = LoadState.ERROR;
            return loadState;
        }
        
        // Analyzing source's extension
        if (levelsSetFile.getAbsolutePath().endsWith(".xml")) {
            
            // XML source
            Document xmlLevelsSetDocument = XMLHelper.readXMLDocument(source);
            loadFromDOM(xmlLevelsSetDocument);
        }
        else if (levelsSetFile.getAbsolutePath().endsWith(".sok")) {
            
            // SOK source
            loadFromSOKFile(source);
        }
        
        // Checking whether at least one playable level has been successfully retrieved
        loadState = LoadState.ERROR;
        int playableLevelsCount = getPlayableLevelsCount();
        int levelsCount = getLevelsCount();
        if (playableLevelsCount == levelsCount)
            loadState = LoadState.SUCCESS;
        else if (playableLevelsCount > 0 && playableLevelsCount < levelsCount)
            loadState = LoadState.WARNING;
        
        return loadState;
    }
    
    /**
     * Reads levels from provided XML document.
     * 
     * @param xmlLevelsSetDocument
     *      Instance of XML document.
     * @see #load(java.lang.String)
     */
    public void loadFromDOM(Document xmlLevelsSetDocument) {
        
        // Retrieving levels set XML file's root element
        Element xmlLevelsSetElement = XMLHelper.getDocumentElement(xmlLevelsSetDocument);
        if (xmlLevelsSetElement == null)
            return;
        
        // Retrieving levels count from XML
        int levelsCount = XMLHelper.getChildrenCount(xmlLevelsSetElement, "level");
        if (levelsCount == 0)
            return;
        
        // Determining level's maximal width and height
        int maximalLevelWidth = (Integer)configuration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
        int maximalLevelHeight = (Integer)configuration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        
        // Retrieving set's name
        setName(XMLHelper.getElementText(XMLHelper.getChildElement(xmlLevelsSetElement, "name"), null));               
        
        int levelIndex = 0;
        while (levelIndex < levelsCount) {
            
            // Retrieving XML element of the current level
            Element xmlLevelElement = XMLHelper.getChildElement(xmlLevelsSetElement, "level", levelIndex);
            
            String levelName = XMLHelper.getElementText(XMLHelper.getChildElement(xmlLevelElement, "name"), "");
            int levelLinesCount = XMLHelper.getChildrenCount(xmlLevelElement, "l");
            if (levelLinesCount > 0) {

                ArrayList<String> levelLines = new ArrayList<String>();
                int levelLineIndex = 0;
                while (levelLineIndex < levelLinesCount) {

                    Element xmlLevelLineElement = XMLHelper.getChildElement(xmlLevelElement, "l", levelLineIndex);
                    String levelLine = XMLHelper.getElementText(xmlLevelLineElement);
                    levelLines.add(levelLine);
                    levelLineIndex++;
                }

                Level level = createLevelFromLines(levelLines, levelName,
                        new LevelSize(maximalLevelWidth, maximalLevelHeight));
                addLevel(level);
            }
            
            levelIndex++;
        }
    }
    
    /**
     * @param fileName
     *      SOK-file's name.
     */
    public void loadFromSOKFile(String fileName) {
        
        try {
            
            FileInputStream fileInputStream = new FileInputStream(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            
            // Determining level's maximal width and height
            int maximalLevelWidth = (Integer)configuration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                    Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
            int maximalLevelHeight = (Integer)configuration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                    Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        
            ArrayList<String> levelLines = null;
            String lastNonLevelLine = null;
            String fileLine = null;
            Pattern levelLinePattern = Pattern.compile(String.format("^[%s%s%s%s%s%s%s]+$",
                    Level.LEVEL_ITEM_WORKER_REG, Level.LEVEL_ITEM_WORKER_ON_GOAL_REG,
                    Level.LEVEL_ITEM_BRICK_REG, Level.LEVEL_ITEM_GOAL_REG,
                    Level.LEVEL_ITEM_BOX_REG, Level.LEVEL_ITEM_BOX_ON_GOAL_REG,
                    Level.LEVEL_ITEM_SPACE_REG));
            
            while ((fileLine = bufferedReader.readLine()) != null) {
                
                // Trimming the line from the right
                while (fileLine.endsWith(" "))
                    fileLine = fileLine.substring(0, fileLine.length() -1);
                               
                // Checking whether the line describes level's row
                Matcher levelLineMatcher = levelLinePattern.matcher(fileLine);
                if (levelLineMatcher.matches()) {
                    
                    // We have level's row here
                    if (levelLines == null)
                        levelLines = new ArrayList<String>();
                    levelLines.add(fileLine);
                }
                else {
                    
                    // We have non-level's line here
                    if (levelLines != null) {
                        
                        // Creating new level from previously read level's rows
                        Level level = createLevelFromLines(levelLines, lastNonLevelLine,
                                new LevelSize(maximalLevelWidth, maximalLevelHeight));
                        addLevel(level);
                        levelLines = null;
                    }
                    
                    // Remembering last non-level line - it's will be used as the name of next level
                    if (!fileLine.isEmpty())
                        lastNonLevelLine = fileLine;
                }
            }
            
            if (levelLines != null) {
                
                Level level = createLevelFromLines(levelLines, lastNonLevelLine,
                        new LevelSize(maximalLevelWidth, maximalLevelHeight));
                addLevel(level);
                levelLines = null;
            }
            
            bufferedReader.close();
            fileInputStream.close();
        }
        catch (FileNotFoundException ex) {
            
        }
        catch (IOException ex) {
            
        }
    }
    
    protected static Level createLevelFromLines(ArrayList<String> levelLines, String levelName, LevelSize maximalLevelSize) {
        
        if (levelLines == null || levelLines.isEmpty())
            return null;
        
        HashMap<String, Object> levelInfo = new HashMap<String, Object>();
        if (levelName != null && !levelName.isEmpty())
            levelInfo.put("name", levelName);
        return new Level(levelLines, levelInfo, maximalLevelSize);
    }
    
    /**
     * Reinitializes all currently loaded levels.
     * 
     * This method must be used every time level's maximal size (width and height)
     * has been changed. New level's maximal size can be small enough to prevent
     * some of loaded levels to fit it.
     * 
     * @return
     *      Reinitialization result, must be one of the following values:
     *      <ul>
     *      <li>{@link LoadState#SUCCESS} - all levels have been successfully reinitialized;</li>
     *      <li>{@link LoadState#WARNING} - at least one of currently loaded levels was unable
     *          to be reinitialized;</li>
     *      <li>{@link LoadState#ERROR} - all currently loaded levels were unable to be reinitialized.</li>
     *      </ul>
     * @see Level#initialize()
     */
    public LoadState reinitialize() {
        
        if (configuration == null || isEmpty()) {
            
            loadState = LoadState.ERROR;
            return loadState;
        }

        // Retrieving level's configured width and height
        int levelWidth = (Integer)configuration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
        int levelHeight = (Integer)configuration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        
        int gameLevelIndex = 0;
        while (gameLevelIndex < getLevelsCount()) {
            
            // Retrieving a reference to current level
            Level gameLevel = getLevelByIndex(gameLevelIndex);
            
            // Changing level's maximal size before reinitialization
            gameLevel.setMaximalSize(levelWidth, levelHeight);
            
            // Attempting to reinitialize the level
            gameLevel.initialize();
            gameLevelIndex++;
        }
        
        // Analyzing reinitialization results
        loadState = LoadState.SUCCESS;
        if (getPlayableLevelsCount() == 0)
            loadState = LoadState.ERROR;
        else if (getPlayableLevelsCount() < getLevelsCount())
            loadState = LoadState.WARNING;
        return loadState;
    }
    
    /**
     * Retrieves set's name.
     * 
     * @return 
     *      Set's name.
     * @see #setName(java.lang.String)
     */
    public String getName() {
        
        return name;
    }
    
    /**
     * Sets set's name.
     * 
     * @param name 
     *      Desired set's name.
     * @see #getName()
     */
    public void setName(String name) {
        
        this.name = name != null ? name : "";
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