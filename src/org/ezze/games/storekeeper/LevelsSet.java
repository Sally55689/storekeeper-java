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
 * @version 0.0.4
 */
public class LevelsSet {
    
    /**
     * Shows whether levels' set is initialized.
     * 
     * Set is initialized if at least one level has been loaded
     * (no matter if this level is playable or not).
     */
    protected boolean isInitialized = false;
    
    /**
     * Set's name.
     */
    protected String name = "";
    
    /**
     * A list of set's levels.
     */
    protected ArrayList<Level> levels = new ArrayList<Level>();
    
    /**
     * An index of set's currently selected level.
     * 
     * Default value {@code -1} means that no level is selected.
     */
    protected int currentLevelIndex = -1;
    
    /**
     * Constructs empty levels' set.
     */ 
    public LevelsSet() {
        
        this(null);
    }
    
    /**
     * Constructs levels' set from specified source file or DOM document.
     * 
     * @param source 
     *      Set's source file's name.
     */
    public LevelsSet(Object source) {
        
        load(source);
    }
    
    boolean isInitialized() {
        
        return isInitialized;
    }
    
    /**
     * Loads levels' set from source file pointed by a name.
     * 
     * @param source
     *      Set's source file's name.
     * @return
     *      Set's load result.
     */
    public final boolean load(Object source) {
        
        isInitialized = false;
        
        if (source == null)
            return false;
        
        if (source instanceof String) {
            
            File levelsSetFile = new File((String)source);
            if (!levelsSetFile.exists() || !levelsSetFile.isFile())
                return false;

            // Analyzing source's extension
            if (levelsSetFile.getAbsolutePath().endsWith(".xml")) {

                // XML source
                Document xmlLevelsSetDocument = XMLHelper.readXMLDocument((String)source);
                loadFromDOM(xmlLevelsSetDocument);
            }
            else if (levelsSetFile.getAbsolutePath().endsWith(".sok")) {

                // SOK source
                loadFromSOKFile((String)source);
            }
        }
        else if (source instanceof Document) {
            
            loadFromDOM((Document)source);
        }
        
        // Determining maximal possible size of set's level
        LevelSize maximalLevelSize = getMaximalLevelSize();
        
        // Initializing levels
        int levelIndex = 0;
        while (levelIndex < getLevelsCount()) {
            
            Level level = levels.get(levelIndex);
            level.initialize(maximalLevelSize);
            levelIndex++;
        }
        
        isInitialized = getLevelsCount() > 0;
        return isInitialized;
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

                Level level = createLevelFromLines(levelLines, levelName);
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
                        Level level = createLevelFromLines(levelLines, lastNonLevelLine);
                        addLevel(level);
                        levelLines = null;
                        lastNonLevelLine = null;
                    }
                    
                    // Remembering last non-level line - it's will be used as the name of next level
                    if (!fileLine.isEmpty())
                        lastNonLevelLine = fileLine;
                }
            }
            
            if (levelLines != null) {
                
                Level level = createLevelFromLines(levelLines, lastNonLevelLine);
                addLevel(level);
                levelLines = null;
                lastNonLevelLine = null;
            }
            
            bufferedReader.close();
            fileInputStream.close();
        }
        catch (FileNotFoundException ex) {
            
        }
        catch (IOException ex) {
            
        }
    }
    
    protected static Level createLevelFromLines(ArrayList<String> levelLines, String levelName) {
        
        if (levelLines == null || levelLines.isEmpty())
            return null;
        
        HashMap<String, Object> levelInfo = new HashMap<String, Object>();
        if (levelName != null && !levelName.isEmpty())
            levelInfo.put("name", levelName);
        Level level = new Level(levelLines, levelInfo);
        return level;
    }
    
    public void reinitialize() {
        
        reinitialize(null);
    }
    
    /**
     * Reinitializes all currently loaded levels.
     * 
     * This method must be used every time level's maximal size (width and height)
     * has been changed. New level's maximal size can be small enough to prevent
     * some of loaded levels to fit it.
     * 
     * @see Level#initialize()
     */
    public void reinitialize(LevelSize maximalLevelSize) {
        
        int gameLevelIndex = 0;
        while (gameLevelIndex < getLevelsCount()) {
            
            // Retrieving a reference to current level
            Level gameLevel = getLevelByIndex(gameLevelIndex);
            
            // Attempting to reinitialize the level
            gameLevel.initialize(maximalLevelSize == null ?
                    new LevelSize(Level.DEFAULT_LEVEL_WIDTH, Level.DEFAULT_LEVEL_HEIGHT) : maximalLevelSize);
            gameLevelIndex++;
        }
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
    
    public LevelSize getMaximalLevelSize() {
        
        int maximalWidth = Level.MINIMAL_LEVEL_WIDTH;
        int maximalHeight = Level.MINIMAL_LEVEL_HEIGHT;
        
        int levelIndex = 0;
        while (levelIndex < levels.size()) {
            
            Level level = levels.get(levelIndex);
            LevelSize levelSize = level.getSize();
            if (levelSize.getWidth() > maximalWidth)
                maximalWidth = levelSize.getWidth();
            if (levelSize.getHeight() > maximalHeight)
                maximalHeight = levelSize.getHeight();
            levelIndex++;
        }
        
        return new LevelSize(maximalWidth, maximalHeight);
    }
}