package org.ezze.games.storekeeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.InputStream;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import org.ezze.games.storekeeper.Level.MoveInformation;
import org.ezze.games.storekeeper.Level.MoveType;
import org.ezze.utils.io.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is the main part of the game implementing graphics,
 * animation and user actions' handling.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.4
 */
public class Game extends JPanel implements Runnable {

    /**
     * Level methods' result enumeration.
     * 
     * Used as a result of methods loading and reinitializng levels' sets.
     * 
     * @see #loadDefaultLevelsSet()
     * @see #loadLevelsSet(java.lang.String)
     * @see #reinitializeLevels()
     */
    public enum LevelResult {
        
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
        ERROR
    }
    
    /**
     * Game's state, can be equal to one of the following values:
     * <ul>
     * <li>{@link #INTRODUCTION}</li>
     * <li>{@link #PLAY}</li>
     * <li>{@link #STOP}</li>
     * <li>{@link #COMPLETED}</li>
     * </ul>
     */
    public enum GameState {

        /**
         * Means that introduction screen is to be displayed, the game is not running.
         */
        INTRODUCTION,
        
        /**
         * Means that game is running and a player can take an action.
         */
        PLAY,
        
        /**
         * Similar to {@code INTRODUCTION} excepting empty screen is to be displayed.
         * 
         * This game state is used to stop the game and between levels' changes.
         */
        STOP,
        
        /**
         * Means that current level has been successfully completed.
         * 
         * This game state is actual while provided implementation of
         * {@link LevelCompletionListener#levelCompleted(org.ezze.games.storekeeper.Level)}
         * is being invoked.
         */
        COMPLETED
    }
    
    /**
     * Specifies worker's look direction.
     */
    private enum WorkerDirection {

        /**
         * Worker looks to the left.
         */
        LEFT,
        
        /**
         * Worker looks to the right.
         */
        RIGHT
    }
    
    private Configuration gameConfiguration = null;
    
    /**
     * A reference to an interface implementing game graphics,
     * particularly providing references to sprites' images.
     * 
     * This one must be passed as instance constructor's argument.
     * 
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics)
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics, org.ezze.games.storekeeper.LevelCompletionListener)
     */
    private GameGraphics gameGraphics = null;
    
    /**
     * An instance of interface providing actions to do after level will have been completed.
     * 
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics, org.ezze.games.storekeeper.LevelCompletionListener)
     */
    private LevelCompletionListener gameLevelCompletionListener = null;
    
    /**
     * Keeps game's current state.
     * 
     * @see GameState
     */
    private GameState gameState = null;
    
    /**
     * Shows whether default levels set is loaded.
     */
    private boolean isDefaultLevelsSetLoaded = false;
    
    /**
     * Keeps a set of currently loaded levels.
     */
    private LevelsSet levelsSet = null;
    
    /**
     * Measures current level's play time in seconds.
     */
    private int levelTime = 0;
    
    /**
     * Represents game's loop, primarily used for {@link GameState#PLAY} state.
     */
    private Thread gameLoopThread = null;
    
    /**
     * Stores worker's current glance direction.
     */
    private WorkerDirection workerDirection = WorkerDirection.LEFT;
    
    /**
     * Stores worker's desired horizontal shift forced by user.
     * 
     * Can be equal to -1, 0, 1.
     */
    private int workerDeltaX = 0;
    
    /**
     * Stores worker's desired vertical shift forced by user.
     * 
     * Can be equal to -1, 0, 1.
     */
    private int workerDeltaY = 0;
    
    /**
     * Shows whether worker is idle right now.
     */
    private boolean isWorkerIdle = true;
    
    /**
     * Shows whether animation is in progress.
     */
    private boolean isAnimationInProgress = false;
    
    /**
     * Worker's animation phase's index.
     * 
     * This index must no less than 0 and less than
     * {@link GameGraphics#getActionSpritesCount()} of {@link #gameGraphics}.
     * Animation phase with index 0 corresponds to idle worker
     * while another ones represent worker's motion.
     */
    private int workerAnimPhase = 0;
    
    /**
     * Keeps worker's destination X coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This variable is a local copy of the current {@link Level#workerX}.
     * Default value -1 means that worker is idle and no animation is in progress.
     */
    private int workerAnimDestX = -1;
    
    /**
     * Keeps worker's destination Y coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This variable is a local copy of the current {@link Level#workerY}.
     * Default value -1 means that worker is idle and no animation is in progress.
     */
    private int workerAnimDestY = -1;
    
    /**
     * Keeps worker's current X animation coordinate of {@link Level} field.
     * 
     * Please note that this variable is used for animation purporses only
     * while X coordinate {@link Level#workerX} of worker's real position
     * is always integer.
     */
    private double workerAnimCurrX = 0.0;
    
    /** 
     * Keeps worker's current Y animation coordinate of {@link Level} field.
     * 
     * Please note that this variable is used for animation purporses only
     * while Y coordinate {@link Level#workerY} of worker's real position
     * is always integer.
     */
    private double workerAnimCurrY = 0.0;
    
    /**
     * Worker's X animation shift to be performed during current loop execution.
     * 
     * This one is determined as {@link GameGraphics#getAnimationStepShift()} of
     * {@link #gameGraphics} for each horizontal worker's animation.
     * 
     * Animation continues until {@link #workerAnimCurrX} reaches {@link #workerAnimDestX}
     * and {@link #workerAnimCurrY} reaches {@link #workerAnimDestY}.
     */
    private double workerAnimDeltaX = 0.0;
    
    /**
     * Worker's Y animation shift to be performed during current loop execution.
     * 
     * This one is determined as {@link GameGraphics#getAnimationStepShift()} of
     * {@link #gameGraphics} for each vertical worker's animation.
     * 
     * Animation continues until {@link #workerAnimCurrX} reaches {@link #workerAnimDestX}
     * and {@link #workerAnimCurrY} reaches {@link #workerAnimDestY}.
     */
    private double workerAnimDeltaY = 0.0;
    
    /**
     * Keeps moving box' destination X coordinate of {@link Level} field
     * and is used for animation purposes only.
     * 
     * This one is similar to {@link #workerAnimDestX} but relative to moving box.
     */
    private int boxAnimDestX = -1;
    
    /**
     * Keeps moving box' destination Y coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This one is similar to {@link #workerAnimDestY} but relative to moving box.
     */
    private int boxAnimDestY = -1;
    
    /**
     * Keeps moving box' current X animation coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This one is similar to {@link #workerAnimCurrX} but relative to moving box.
     */
    private double boxAnimCurrX = 0.0;
    
    /**
     * Keeps moving box' current Y animation coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This one is similar to {@link #workerAnimCurrY} but relative to moving box.
     */
    private double boxAnimCurrY = 0.0;
    
    /**
     * Moving box' X shift to be performed during current loop execution.
     * 
     * This one is equal to zero or to {@link #workerAnimDeltaX} due to box' shift
     * can be caused only by similar worker's movement.
     */
    private double boxAnimDeltaX = 0.0;
    
    /**
     * Moving box' Y shift to be performed during current loop execution.
     * 
     * This one is equal to zero or to {@link #workerAnimDeltaY} due to box' shift
     * can be caused only by similar worker's movement.
     */
    private double boxAnimDeltaY = 0.0;
    
    /**
     * Game's simple constructor.
     * 
     * @param gameConfiguration
     *      A reference to game configuration.
     * @param gameGraphics 
     *      A reference to game graphics implementation.
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics, org.ezze.games.storekeeper.LevelCompletionListener)
     * @see GameGraphics
     */
    public Game(Configuration gameConfiguration, GameGraphics gameGraphics) {
        
        this(gameConfiguration, gameGraphics, null);
    }
    
    /**
     * Game's advanced constructor.
     * 
     * @param gameConfiguration
     *      A reference to game configuration.
     * @param gameGraphics
     *      A reference to game graphics implementation.
     * @param gameLevelCompletionListener 
     *      A reference to game level's completion listener.
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics)
     * @see GameGraphics
     * @see LevelCompletionListener
     */
    public Game(Configuration gameConfiguration, GameGraphics gameGraphics,
            LevelCompletionListener gameLevelCompletionListener) {
        
        super();
        
        // Storing a reference to game configuration instance
        this.gameConfiguration = gameConfiguration;
        
        // Storing a reference to graphics implementation
        this.gameGraphics = gameGraphics;
        
        // Storing a reference to game level's completion listener
        this.gameLevelCompletionListener = gameLevelCompletionListener;
        
        // Defining game's initial state
        gameState = GameState.INTRODUCTION;
        
        // No levels are loaded by default
        levelsSet = null;
        
        // Making game's field double buffered
        setDoubleBuffered(true);
        
        // Setting field's background color
        setBackground(gameGraphics.getBackground());
    }
   
    /**
     * Retrieves a reference to game configuration instance.
     * 
     * @return
     *      Game configuration instance
     * @see #gameConfiguration
     */
    public Configuration getGameConfiguration() {
        
        return gameConfiguration;
    }
    
    /**
     * Retrieves a reference to game graphics implementation.
     * 
     * @return 
     *      Class implementing {@link GameGraphics} interface
     * @see #gameGraphics
     */
    public GameGraphics getGameGraphics() {
        
        return gameGraphics;
    }
    
    /**
     * Retrieves game's current state.
     * 
     * @return 
     *      Game's state
     * @see GameState
     */
    public GameState getGameState() {
        
        return gameState;
    }
    
    /**
     * Checks whether default levels' set is loaded right now.
     * 
     * @return 
     *      {@code true} if default levels' set is loaded, {@code false} otherwise
     */
    public boolean isDefaultLevelsSetLoaded() {
        
        return isDefaultLevelsSetLoaded;
    }
    
    /**
     * Checks whether any levels' set is loaded.
     * 
     * @return 
     *      {@code true} if levels' set is loaded, {@code false} otherwise
     */
    public boolean isLevelsSetLoaded() {
        
        return levelsSet != null && levelsSet.getLevelsCount() > 0;
    }
    
    /**
     * Loads default levels set.
     * 
     * @return 
     *      Level's load result {@link Result}
     * @see #loadLevelsSet(java.lang.String) 
     * @see #readLevelsSetFromXML(org.w3c.dom.Document, boolean) 
     */
    public LevelResult loadDefaultLevelsSet() {
        
        String resourcePathToLevelsSet = String.format("/%s/resources/levels.xml",
                Game.class.getPackage().getName().replace('.', '/'));
        InputStream levelsSetInputStream = Game.class.getResourceAsStream(resourcePathToLevelsSet);
        if (levelsSetInputStream == null)
            return LevelResult.ERROR;
        
        Document xmlLevelsSetDocument = XMLParser.readXMLDocument(levelsSetInputStream);
        LevelResult loadResult = readLevelsSetFromXML(xmlLevelsSetDocument, true);
        if (loadResult != LevelResult.ERROR)
            isDefaultLevelsSetLoaded = true;
        return loadResult;
    }
    
    /**
     * Loads levels' set from XML file pointed by a name.
     * 
     * @param levelsSetFileName
     *      Set's XML file's name
     * @return
     *      Level's load result {@link Result}
     * @see #loadDefaultLevelsSet() 
     * @see #readLevelsSetFromXML(org.w3c.dom.Document, boolean) 
     */
    public LevelResult loadLevelsSet(String levelsSetFileName) {
        
        if (levelsSetFileName == null)
            return LevelResult.ERROR;
        
        File levelsSetFile = new File(levelsSetFileName);
        if (!levelsSetFile.exists() || !levelsSetFile.isFile())
            return LevelResult.ERROR;
        
        Document xmlLevelsSetDocument = XMLParser.readXMLDocument(levelsSetFileName);
        LevelResult loadResult = readLevelsSetFromXML(xmlLevelsSetDocument, false);
        if (loadResult != LevelResult.ERROR)
            isDefaultLevelsSetLoaded = false;
        return loadResult;
    }
    
    /**
     * Reads levels from provided XML document.
     * 
     * In the case of {@code isDefault} set to {@code false} this method
     * will try to load default levels' set on failure.
     * 
     * This method is used by {@link #loadDefaultLevelsSet()} and
     * {@link #loadLevelsSet(java.lang.String)} methods.
     * 
     * @param xmlLevelsSetDocument
     *      Instance of XML document
     * @param isDefault
     *      Shows whether XML document is of default levels' set
     * @return 
     *      Level's load result {@link Result}
     * @see #loadDefaultLevelsSet()
     * @see #loadLevelsSet(java.lang.String)
     */
    private LevelResult readLevelsSetFromXML(Document xmlLevelsSetDocument, boolean isDefault) {
        
        // Retrieving levels set XML file's root element
        Element xmlLevelsSetElement = XMLParser.getDocumentElement(xmlLevelsSetDocument);
        if (xmlLevelsSetElement == null)
            return LevelResult.ERROR;
        
        // Retrieving levels count from XML
        int levelsCount = XMLParser.getChildrenCount(xmlLevelsSetElement, "level");
        if (levelsCount == 0)
            return LevelResult.ERROR;
        
        // Stopping the game
        stop(true);
        
        // Retrieving set's name
        String levelsSetName = XMLParser.getElementText(XMLParser.getChildElement(xmlLevelsSetElement, "name"), null);
        
        // Creating new levels set
        levelsSet = new LevelsSet(levelsSetName);
        
        // Defining level's maximal width and height
        int maximalLevelWidth = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
        int maximalLevelHeight = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        
        int levelIndex = 0;
        while (levelIndex < levelsCount) {
            
            // Retrieving XML element of the current level
            Element xmlLevelElement = XMLParser.getChildElement(xmlLevelsSetElement, "level", levelIndex);
            
            // Retrieving level's ID
            int levelID = XMLParser.getElementAttributeInteger(xmlLevelElement, "id", 0);
            
            // Checking whether level's ID is valid
            if (levelID > 0) {

                String levelName = XMLParser.getElementText(XMLParser.getChildElement(xmlLevelElement, "name"), "");
                int levelLinesCount = XMLParser.getChildrenCount(xmlLevelElement, "l");
                if (levelLinesCount > 0) {

                    ArrayList<String> levelLines = new ArrayList<String>();
                    int levelLineIndex = 0;
                    while (levelLineIndex < levelLinesCount) {

                        Element xmlLevelLineElement = XMLParser.getChildElement(xmlLevelElement, "l", levelLineIndex);
                        String levelLine = XMLParser.getElementText(xmlLevelLineElement);
                        levelLines.add(levelLine);
                        levelLineIndex++;
                    }

                    HashMap<String, Object> levelInfo = new HashMap<String, Object>();
                    levelInfo.put("id", new Integer(levelID));
                    levelInfo.put("name", levelName);
                    Level gameLevel = new Level(levelLines, levelInfo, maximalLevelWidth, maximalLevelHeight);
                    levelsSet.addLevel(gameLevel);
                }
            }
            
            levelIndex++;
        }
        
        // Checking whether at least one playable level has been successfully retrieved
        LevelResult readResult = levelsSet.getPlayableLevelsCount() == 0 ? LevelResult.ERROR :
                (levelsSet.getPlayableLevelsCount() < levelsSet.getLevelsCount() ? LevelResult.WARNING : LevelResult.SUCCESS);
        
        if (readResult == LevelResult.ERROR && !isDefault) {
            
            // Loading default levels set on failure
            loadDefaultLevelsSet();
        }
        
        return readResult;
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
     *      <li>{@link LevelResult#SUCCESS} - all levels have been successfully reinitialized;</li>
     *      <li>{@link LevelResult#WARNING} - at least one of currently loaded levels was unable
     *          to be reinitialized;</li>
     *      <li>{@link LevelResult#ERROR} - all currently loaded levels were unable to be reinitialized.</li>
     *      </ul>
     * @see Level#initialize()
     */
    public LevelResult reinitializeLevels() {
        
        // Stopping the game if it's required
        stop(true);
        
        if (levelsSet == null || levelsSet.isEmpty())
            return LevelResult.ERROR;

        // Retrieving level's configured width and height
        int levelWidth = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
        int levelHeight = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        
        int gameLevelIndex = 0;
        while (gameLevelIndex < levelsSet.getLevelsCount()) {
            
            // Retrieving a reference to current level
            Level gameLevel = levelsSet.getLevelByIndex(gameLevelIndex);
            
            // Changing level's maximal size before reinitialization
            gameLevel.setMaximalLevelSize(levelWidth, levelHeight);
            
            // Attempting to reinitialize the level
            if (!gameLevel.initialize()) {
                
                // Changing currently selected level's index if it's required
                if (gameLevelIndex < levelsSet.getCurrentLevelIndex() && levelsSet.getCurrentLevelIndex() > 0)
                    levelsSet.goToPreviousLevel(true);
            }
            
            gameLevelIndex++;
        }
        
        LevelResult reinitializationResult = LevelResult.SUCCESS;
        if (levelsSet.getPlayableLevelsCount() == 0)
            reinitializationResult = LevelResult.ERROR;
        else if (levelsSet.getPlayableLevelsCount() < levelsSet.getLevelsCount())
            reinitializationResult = LevelResult.WARNING;
        
        return reinitializationResult;
    }
    
    /**
     * Retrieves a reference to levels' set.
     * 
     * @return 
     *      Levels' set.
     */
    public LevelsSet getLevelsSet() {
        
        return levelsSet;
    }
    
    /**
     * Starts game's level with specified index.
     * 
     * This method interrupts currenly running game in any
     * and start new game loop thread {@link #gameLoopThread} setting
     * game's state {@link #gameState} to {@link GameState#PLAY}.
     * 
     * Level index {@code gameLevelIndex} must be in the
     * range [0; {@link LevelsSet#getLevelsCount()} - 1].
     * 
     * @param gameLevelIndex
     *      Level's index
     * @return 
     *      {@code true} on success, {@code false} otherwise
     * @see #restartLevel()
     * @see #goToPreviousLevel()
     * @see #goToNextLevel()
     * @see #stop() 
     * @see #stop(boolean)
     */
    public boolean startLevel(int gameLevelIndex) {
        
        if (levelsSet == null || gameLevelIndex < 0 || gameLevelIndex >= levelsSet.getLevelsCount())
            return false;
        
        // Interrupting previous game run if any
        stop();
        
        // Retrieving a reference to desired game level
        Level gameLevel = levelsSet.getLevelByIndex(gameLevelIndex);
        if (!gameLevel.initialize()) {
         
            repaint();
            return false;
        }
        
        if (!levelsSet.setCurrentLevelByIndex(gameLevelIndex, true)) {
            
            repaint();
            return false;
        }
        
        workerDirection = WorkerDirection.LEFT;
        workerDeltaX = 0;
        workerDeltaY = 0;
        isWorkerIdle = true;
        isAnimationInProgress = false;
        workerAnimPhase = 0;
        workerAnimDestX = -1;
        workerAnimDestY = -1;
        workerAnimCurrX = 0.0;
        workerAnimCurrY = 0.0;
        workerAnimDeltaX = 0.0;
        workerAnimDeltaY = 0.0;
        boxAnimDestX = -1;
        boxAnimDestY = -1;
        boxAnimCurrX = 0.0;
        boxAnimCurrY = 0.0;
        boxAnimDeltaX = 0.0;
        boxAnimDeltaY = 0.0;
        
        // Starting game loop thread
        gameState = GameState.PLAY;
        gameLoopThread = new Thread(this);
        gameLoopThread.start();
        
        return true;
    }
    
    /**
     * Forces current game level to restart.
     * 
     * @return 
     *      {@code true} on success, {@code false} otherwise
     * @see #startLevel(int)
     * @see #goToPreviousLevel() 
     * @see #goToNextLevel()
     * @see #stop() 
     * @see #stop(boolean) 
     */
    public boolean restartLevel() {
        
        if (gameState == GameState.PLAY)
            return startLevel(levelsSet.getCurrentLevelIndex());
        return false;
    }
    
    /**
     * Stops the game if it's running.
     * 
     * This method interrupts game loop thread {@link #gameLoopThread}
     * and changes game's state {@link #gameState} to {@link GameState#STOP}.
     * 
     * @see #stop(boolean)
     * @see #startLevel(int)
     */
    public void stop() {
        
        stop(false);
    }
    
    /**
     * Stops the game if it's running and switches
     * to introduction screen in the case of {@code switchToIntroduction}
     * is set to {@code true}.
     * 
     * This method interrupts game loop thread {@link #gameLoopThread}
     * and changes game's state {@link #gameState} to {@link GameState#STOP}
     * or {@link GameState#INTRODUCTION}.
     * 
     * @param switchToIntroduction 
     *      Application will be switched to introduction screen
     *      if this flag is set to {@code true}
     * @see #stop() 
     * @see #startLevel(int) 
     */
    public void stop(boolean switchToIntroduction) {
        
        gameState = (switchToIntroduction ? GameState.INTRODUCTION : GameState.STOP);
        if (gameLoopThread != null) {

            gameLoopThread.interrupt();
            try {

                gameLoopThread.join();
            }
            catch (InterruptedException ex) {

            }
            
            gameLoopThread = null;
        }
        
        repaint();
    }
    
    /**
     * Switches to the previous level of current levels' set.
     * 
     * If the game was in {@link GameState#PLAY} state
     * then new level will be started automatically.
     * 
     * @return 
     *      {@code true} on success, {@code false} otherwise
     * @see #goToNextLevel()
     * @see #restartLevel()
     */
    public boolean goToPreviousLevel() {
        
        if (levelsSet == null || levelsSet.isEmpty())
            return false;
        
        boolean startNewLevel = gameState == GameState.PLAY;
        
        // Stopping current game run
        stop(gameState == GameState.INTRODUCTION);
        
        // Jumping to the previous game level
        levelsSet.goToPreviousLevel(true);
        
        if (startNewLevel)
            return startLevel(levelsSet.getCurrentLevelIndex());
        
        return true;
    }
    
    /**
     * Switches to the next level of current levels' set.
     * 
     * If the game was in {@link GameState#PLAY} state
     * then new level will be started automatically.
     * 
     * @return 
     *      {@code true} on success, {@code false} otherwise
     * @see #goToPreviousLevel()
     * @see #restartLevel()
     */
    public boolean goToNextLevel() {
        
        if (levelsSet == null || levelsSet.isEmpty())
            return false;
        
        boolean startNewLevel = gameState == GameState.PLAY;
        
        // Stopping current game run
        stop(gameState == GameState.INTRODUCTION);
        
        // Jumping to the next game level
        levelsSet.goToNextLevel(true);
        
        if (startNewLevel)
            return startLevel(levelsSet.getCurrentLevelIndex());
        
        return true;
    }
    
    /**
     * Takes current game level's position back by one move.
     * 
     * @return 
     *      A number of performed moves after the take-back or {@code -1}
     *      if the take-back cannot be performed for some reasons.
     * @see #takeBack(int)
     * @see Level#takeBack()
     * @see Level#takeBack(int)
     */
    public int takeBack() {
        
        return takeBack(1);
    }
    
    /**
     * Takes current game level's position back by specified moves' count.
     * 
     * @param takeBackMovesCount
     *      Moves' count to take current level's position back by.
     * @return
     *      A number of performed moves after the take-back or {@code -1}
     *      if the take-back cannot be performed for some reasons.
     * @see #takeBack()
     * @see Level#takeBack()
     * @see Level#takeBack(int)
     */
    public int takeBack(int takeBackMovesCount) {
        
        // Checking whether game is in play state
        if (!gameState.equals(GameState.PLAY))
            return -1;
        
        // Retrieving a reference to current game level
        Level gameLevel = levelsSet.getCurrentLevel();
        if (gameLevel == null)
            return -1;
        
        // Preventing from taking back when the worker is moving
        if (!isWorkerIdle)
            return -1;
        
        return gameLevel.takeBack(takeBackMovesCount);
    }
    
    /**
     * Sets worker's horizontal shift to the left.
     * 
     * A real worker's movement can be performed only
     * by {@link Level#move(int, int)} method with compliant
     * conditions satisfied.
     * 
     * @see #forceWorkerToMoveRight()
     * @see #forceWorkerToMoveUp()
     * @see #forceWorkerToMoveDown()
     * @see #forceWorkerToStop()
     * @see #run() 
     */
    public void forceWorkerToMoveLeft() {
        
        workerDirection = WorkerDirection.LEFT;
        workerDeltaX = -1;
        workerDeltaY = 0;
    }
    
    /**
     * Sets worker's horizontal shift to the right.
     * 
     * A real worker's movement can be performed only
     * by {@link Level#move(int, int)} method with compliant
     * conditions satisfied.
     * 
     * @see #forceWorkerToMoveLeft() 
     * @see #forceWorkerToMoveUp() 
     * @see #forceWorkerToMoveDown()
     * @see #forceWorkerToStop()
     * @see #run() 
     */
    public void forceWorkerToMoveRight() {
        
        workerDirection = WorkerDirection.RIGHT;
        workerDeltaX = 1;
        workerDeltaY = 0;
    }
    
    /**
     * Sets worker's vertical shift to the up.
     * 
     * A real worker's movement can be performed only
     * by {@link Level#move(int, int)} method with compliant
     * conditions satisfied.
     * 
     * @see #forceWorkerToMoveLeft()
     * @see #forceWorkerToMoveRight() 
     * @see #forceWorkerToMoveDown() 
     * @see #forceWorkerToStop() 
     * @see #run() 
     */
    public void forceWorkerToMoveUp() {
        
        workerDeltaX = 0;
        workerDeltaY = -1;
    }
    
    /**
     * Sets worker's vertical shift to the down.
     * 
     * A real worker's movement can be performed only
     * by {@link Level#move(int, int)} method with compliant
     * conditions satisfied.
     * 
     * @see #forceWorkerToMoveLeft() 
     * @see #forceWorkerToMoveRight() 
     * @see #forceWorkerToMoveUp() 
     * @see #forceWorkerToStop()
     * @see #run() 
     */
    public void forceWorkerToMoveDown() {
        
        workerDeltaX = 0;
        workerDeltaY = 1;
    }
    
    /**
     * Sets worker's horizontal movement to zero.
     * 
     * @see #forceWorkerToStopVerticalMovement() 
     * @see #forceWorkerToStop() 
     */
    public void forceWorkerToStopHorizontalMovement() {
        
        workerDeltaX = 0;
    }
    
    /**
     * Sets worker's vertical movement to zero.
     * 
     * @see #forceWorkerToStopHorizontalMovement() 
     * @see #forceWorkerToStop() 
     */
    public void forceWorkerToStopVerticalMovement() {
        
        workerDeltaY = 0;
    }
    
    /**
     * Sets worker's horizontal and vertical movements to zero.
     * 
     * @see #forceWorkerToStopHorizontalMovement() 
     * @see #forceWorkerToStopVerticalMovement() 
     * @see #forceWorkerToMoveLeft() 
     * @see #forceWorkerToMoveRight()
     * @see #forceWorkerToMoveUp()
     * @see #forceWorkerToMoveDown() 
     */
    public void forceWorkerToStop() {
        
        forceWorkerToStopHorizontalMovement();
        forceWorkerToStopVerticalMovement();
    }
    
    /**
     * {@inheritDoc}
     * 
     * In the case of {@link Game} instance this method paints
     * game's play field and outputs current level specific information
     * (level's id and name, completed moves count and elapsed time).
     * 
     * @param g
     *      Graphics instance
     */
    @Override
    public void paint(Graphics g) {

        super.paint(g);

        Graphics2D g2d = (Graphics2D)g;
        
        int fontSize = (int)((double)gameGraphics.getSpriteDimension().width / 2) + 4;
        Font gameFont = new Font("Monospaced", Font.BOLD, fontSize);
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fontMetrics = g2d.getFontMetrics(gameFont);
        
        // Retrieving a reference to current game level
        Level gameLevel = levelsSet == null ? null : levelsSet.getCurrentLevel();
        
        if (gameState == GameState.INTRODUCTION) {

            // Displaying introduction image
            setBackground(gameGraphics.getBackground());
            Image introductionImage = gameGraphics.getIntroductionImage();
            int introWidth = introductionImage.getWidth(null);
            int introHeight = introductionImage.getHeight(null);
            int introX = (getSize().width - introWidth) / 2;
            int introY = (getSize().height - introHeight) / 2;
            g2d.drawImage(introductionImage, introX, introY, this);
        }
        else if (gameState == GameState.STOP) {
            
            setBackground(gameGraphics.getBackground());
        }
        else if ((gameState == GameState.PLAY || gameState == GameState.COMPLETED) && gameLevel != null) {
            
            // Retrieving level's maximal size
            int maximalLevelWidth = gameLevel.getMaximalLevelWidth();
            int maximalLevelHeight = gameLevel.getMaximalLevelHeight();
            
            // Retrieving sprites' dimension
            Dimension spriteDimension = gameGraphics.getSpriteDimension();
            
            // Drawing game level's current state
            for (int lineIndex = 0; lineIndex < maximalLevelHeight; lineIndex++) {

                for (int columnIndex = 0; columnIndex < maximalLevelWidth; columnIndex++) {

                    Character levelItem = gameLevel.getItemAt(lineIndex, columnIndex);
                    Image levelItemSprite = null;

                    if (levelItem.equals(Level.LEVEL_ITEM_CELL)) {

                        levelItemSprite = gameGraphics.getCellSprite();
                    }
                    else if (levelItem.equals(Level.LEVEL_ITEM_BOX)) {

                        if (!isWorkerIdle && boxAnimDestX == columnIndex && boxAnimDestY == lineIndex)
                            levelItemSprite = null;
                        else
                            levelItemSprite = gameGraphics.getBoxSprite();
                    }
                    else if (levelItem.equals(Level.LEVEL_ITEM_BOX_IN_CELL)) {

                        if (!isWorkerIdle && boxAnimDestX == columnIndex && boxAnimDestY == lineIndex)
                            levelItemSprite = gameGraphics.getCellSprite();
                        else
                            levelItemSprite = gameGraphics.getBoxInCellSprite();
                    }
                    else if(levelItem.equals(Level.LEVEL_ITEM_BRICK)) {

                        levelItemSprite = gameGraphics.getBrickSprite();
                    }

                    if (levelItemSprite != null) {

                        g2d.drawImage(levelItemSprite, columnIndex * spriteDimension.width, lineIndex * spriteDimension.height, this);
                    }
                }
            }

            // Drawing the worker
            if (isWorkerIdle) {

                // Idle worker
                Point workerLocation = gameLevel.getWorkerLocation();
                Image workerSprite = null;
                if (workerDirection == WorkerDirection.LEFT)
                    workerSprite = gameGraphics.getLeftActionSprite(0);
                else if (workerDirection == WorkerDirection.RIGHT)
                    workerSprite = gameGraphics.getRightActionSprite(0);
                if (workerSprite != null) {

                    g2d.drawImage(workerSprite, workerLocation.x * spriteDimension.width, workerLocation.y * spriteDimension.height, this);
                }
            }
            else {

                // Moving worker
                Image workerSprite = null;
                if (workerDirection == WorkerDirection.LEFT) 
                    workerSprite = gameGraphics.getLeftActionSprite(gameGraphics.getActionSpritesCount() > 1 ? workerAnimPhase : 0);
                else if (workerDirection == WorkerDirection.RIGHT)
                    workerSprite = gameGraphics.getRightActionSprite(gameGraphics.getActionSpritesCount() > 1 ? workerAnimPhase : 0);
                if (workerSprite != null) {

                    g2d.drawImage(workerSprite, (int)(workerAnimCurrX * spriteDimension.width),
                            (int)(workerAnimCurrY * spriteDimension.height), this);
                }

                // Moving box (if it's required)
                if (boxAnimDestX >= 0 && boxAnimDestY >= 0) {

                    g2d.drawImage(gameGraphics.getBoxSprite(), (int)(boxAnimCurrX * spriteDimension.width),
                            (int)(boxAnimCurrY * spriteDimension.height), this);
                }
            }
        }
        
        if (gameState == GameState.INTRODUCTION || gameState == GameState.PLAY || gameState == GameState.COMPLETED) {
            
            // Defining information lines' offsets
            int infoLineHorizontalOffset = fontMetrics.stringWidth(" ");
            int topInfoLineOffset = gameGraphics.getSpriteDimension().height -
                    (gameGraphics.getSpriteDimension().height - gameFont.getSize()) / 2;
            int bottomInfoLineOffset = gameGraphics.getSpriteDimension().height * (gameLevel.getMaximalLevelHeight() - 1)
                    + topInfoLineOffset - 2;
            
            // Printing level information
            String levelNameTitle = "Level: ";
            String gameLevelName = gameLevel.getName();
            String levelNameText = String.format("%03d", gameLevel.getID());
            if (levelsSet != null && !levelsSet.getName().isEmpty()) {

                if (gameLevelName != null && !gameLevelName.isEmpty())
                    levelNameText += String.format(" (\"%s\" of %s)", gameLevelName, levelsSet.getName());
                else
                    levelNameText += String.format(" (%s)", levelsSet.getName());
            }
            else if (gameLevelName != null && !gameLevelName.isEmpty())
                levelNameText += String.format(" (\"%s\")", gameLevelName);
            String levelNameLabel = String.format(" %s%s ", levelNameTitle, levelNameText);
            AttributedString levelNameString = new AttributedString(levelNameLabel);
            levelNameString.addAttribute(TextAttribute.FONT, gameFont);
            levelNameString.addAttribute(TextAttribute.FOREGROUND, new Color(240, 240, 240));
            levelNameString.addAttribute(TextAttribute.FOREGROUND, new Color(255, 220, 0), 0, levelNameTitle.length());
            levelNameString.addAttribute(TextAttribute.BACKGROUND, gameGraphics.getBackground());
            g2d.drawString(levelNameString.getIterator(), infoLineHorizontalOffset, bottomInfoLineOffset);
            
            if ((gameState == GameState.PLAY || gameState == GameState.COMPLETED) && gameLevel != null) {
                
                // Retrieving level's maximal width
                int maximalLevelWidth = gameLevel.getMaximalLevelWidth();
                
                // Printing worker's moves count and pushes count
                String movesCountTitle = "Moves:";
                String pushesCountTitle = "Pushes:";
                String movesCountText = String.format("%05d", gameLevel.getMovesCount());
                String pushesCountText = String.format("%05d", gameLevel.getPushesCount());
                String countLabel = String.format(" %s %s %s %s",
                        movesCountTitle, movesCountText, pushesCountTitle, pushesCountText);
                AttributedString countString = new AttributedString(countLabel);
                countString.addAttribute(TextAttribute.FONT, gameFont);
                countString.addAttribute(TextAttribute.FOREGROUND, new Color(240, 240, 240));
                countString.addAttribute(TextAttribute.FOREGROUND, new Color(220, 190, 0),
                        countLabel.indexOf(movesCountTitle), countLabel.indexOf(movesCountTitle) + movesCountTitle.length());
                countString.addAttribute(TextAttribute.FOREGROUND, new Color(220, 190, 0),
                        countLabel.indexOf(pushesCountTitle), countLabel.indexOf(pushesCountTitle) + pushesCountTitle.length());
                countString.addAttribute(TextAttribute.BACKGROUND, gameGraphics.getBackground());
                g2d.drawString(countString.getIterator(), infoLineHorizontalOffset, topInfoLineOffset);
                
                // Printing level's elapsed time
                String levelTimeTitle = "Time: ";
                int levelTimeSeconds = levelTime % 60;
                int levelTimeMinutes = ((levelTime - levelTimeSeconds) / 60) % 60;
                int levelTimeHours = (levelTime - levelTimeSeconds - levelTimeMinutes * 60) / (60 * 60);
                String levelTimeText = String.format("%02d:%02d:%02d", levelTimeHours, levelTimeMinutes, levelTimeSeconds);
                String levelTimeLabel = String.format(" %s%s ", levelTimeTitle, levelTimeText);
                AttributedString levelTimeString = new AttributedString(levelTimeLabel);
                levelTimeString.addAttribute(TextAttribute.FONT, gameFont);
                levelTimeString.addAttribute(TextAttribute.FOREGROUND, new Color(240, 240, 240));
                levelTimeString.addAttribute(TextAttribute.FOREGROUND, new Color(50, 230, 0), 0, levelTimeTitle.length());
                levelTimeString.addAttribute(TextAttribute.BACKGROUND, gameGraphics.getBackground());
                int levelTimeStringLeftOffset = gameGraphics.getSpriteDimension().width * maximalLevelWidth -
                        fontMetrics.stringWidth(levelTimeLabel) - infoLineHorizontalOffset;
                g2d.drawString(levelTimeString.getIterator(), levelTimeStringLeftOffset, topInfoLineOffset);
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }
    
    /**
     * Implements game loop.
     * 
     * This method attempts to do worker's movement if any is forced
     * by the user with compliant methods:
     * 
     * <ul>
     * <li>{@link #forceWorkerToMoveLeft()}</li>
     * <li>{@link #forceWorkerToMoveRight()}</li>
     * <li>{@link #forceWorkerToMoveUp()}</li>
     * <li>{@link #forceWorkerToMoveDown()}</li>
     * </ul>
     * 
     * The time taken for one game loop's execution is measured in milliseconds.
     */
    @Override
    public void run() {
            
        long cycleStartTime = 0;
        long cycleUsefulTime = 0;
        long cycleSleepTime = 0;
        long levelStartTime = System.currentTimeMillis();
        levelTime = 0;               
        
        // Retrieving a reference to current gameLevel
        Level gameLevel = levelsSet == null ? null : levelsSet.getCurrentLevel();

        while (gameState == GameState.PLAY && !Thread.interrupted()) {
            
            // Retrieving game cycle time
            Integer gameCycleTime = (Integer)gameConfiguration.getOption(Configuration.OPTION_GAME_CYCLE_TIME,
                    Configuration.DEFAULT_OPTION_GAME_CYCLE_TIME);

            // Remembering a start time of current iteration
            cycleStartTime = System.currentTimeMillis();

            // Checking whether user has required worker to move
            if (!isAnimationInProgress) {

                if (gameLevel.isCompleted()) {

                    gameState = GameState.COMPLETED;
                }
                else {

                    // Retrieving worker's current position
                    int workerX = gameLevel.getWorkerX();
                    int workerY = gameLevel.getWorkerY();
                    
                    // Attempting to move the worker by desired shift
                    MoveInformation moveInformation = gameLevel.move(workerDeltaX, workerDeltaY);
                    
                    // Checking whether move attempt was successful
                    if (moveInformation.getType().equals(MoveType.WORKER) ||
                            moveInformation.getType().equals(MoveType.WORKER_AND_BOX)) {

                        // Setting animation state
                        isAnimationInProgress = true;
                        
                        // If the worker was idle before the move we will reset his animation phase
                        if (isWorkerIdle)
                            workerAnimPhase = 0;
                        
                        // Making worker busy
                        isWorkerIdle = false;
                        
                        // Specifing worker's initial position of animation
                        workerAnimCurrX = workerX;
                        workerAnimCurrY = workerY;
                        
                        // Retrieving worker's destination position of animation
                        workerAnimDestX = gameLevel.getWorkerX();
                        workerAnimDestY = gameLevel.getWorkerY();
                        
                        // Calculating worker's animation shift per game loop
                        workerAnimDeltaX = Math.signum(workerAnimDestX - workerAnimCurrX) * gameGraphics.getAnimationStepShift();
                        workerAnimDeltaY = Math.signum(workerAnimDestY - workerAnimCurrY) * gameGraphics.getAnimationStepShift();

                        // Checking whether a box is also to be animated
                        if (moveInformation.getType().equals(MoveType.WORKER_AND_BOX)) {

                            // Box' initial position is equal to worker's destination one
                            boxAnimCurrX = workerAnimDestX;
                            boxAnimCurrY = workerAnimDestY;
                            
                            // Calculating box' destination position
                            boxAnimDestX = (int)(workerAnimDestX + Math.signum(workerAnimDestX - workerAnimCurrX));
                            boxAnimDestY = (int)(workerAnimDestY + Math.signum(workerAnimDestY - workerAnimCurrY));
                            
                            // Box' animation shift is equal to worker's one
                            boxAnimDeltaX = workerAnimDeltaX;
                            boxAnimDeltaY = workerAnimDeltaY;
                        }
                        else {

                            // No box' animation here, resetting parameters
                            boxAnimCurrX = 0.0;
                            boxAnimCurrY = 0.0;
                            boxAnimDestX = -1;
                            boxAnimDestY = -1;
                            boxAnimDeltaX = 0.0;
                            boxAnimDeltaY = 0.0;
                        }
                    }
                    else {

                        // Worker didn't move, making him idle
                        isWorkerIdle = true;
                        
                        // Resetting worker's animation parameters
                        workerAnimDestX = -1;
                        workerAnimDestY = -1;
                        workerAnimCurrX = 0.0;
                        workerAnimCurrY = 0.0;
                        workerAnimDeltaX = 0.0;
                        workerAnimDeltaY = 0.0;
                        
                        // Resetting box' animation parameters
                        boxAnimCurrX = 0.0;
                        boxAnimCurrY = 0.0;
                        boxAnimDestX = -1;
                        boxAnimDestY = -1;
                        boxAnimDeltaX = 0.0;
                        boxAnimDeltaY = 0.0;
                    }
                }
            }

            if (isAnimationInProgress) {

                // Defining worker's new animation phase
                workerAnimPhase++;
                if (workerAnimPhase == gameGraphics.getActionSpritesCount())
                    workerAnimPhase = 1;

                // Calculating worker's new animation position
                workerAnimCurrX += workerAnimDeltaX;
                workerAnimCurrY += workerAnimDeltaY;

                // Calculating box' new animation position (if it's required)
                if (boxAnimDestX >= 0 && boxAnimDestY >= 0) {

                    boxAnimCurrX += boxAnimDeltaX;
                    boxAnimCurrY += boxAnimDeltaY;
                }
            }

            // Repainting the play field
            repaint();

            if (isAnimationInProgress) {

                if (workerAnimDeltaX != 0) {

                    if ((workerAnimDestX - (workerAnimCurrX + workerAnimDeltaX)) * Math.signum(workerAnimDeltaX) <= 0)
                        isAnimationInProgress = false;
                }
                else if (workerAnimDeltaY != 0) {

                    if ((workerAnimDestY - (workerAnimCurrY + workerAnimDeltaY)) * Math.signum(workerAnimDeltaY) <= 0)
                        isAnimationInProgress = false;
                }
            }
                
            // Determining a time required for step calculations
            cycleUsefulTime = System.currentTimeMillis() - cycleStartTime;

            // Determining a time left to complete iteration step
            cycleSleepTime = gameCycleTime - cycleUsefulTime;
            if (cycleSleepTime < 0)
                cycleSleepTime = 0;

            // Waiting 'till the end of iteration
            try {

                if (cycleSleepTime > 0)
                    Thread.sleep(cycleSleepTime);
            }
            catch (InterruptedException ex) {

            }

            // Updating moves count and time
            levelTime = (int)((System.currentTimeMillis() - levelStartTime) / 1000);
        }

        // Checking whether level is completed
        if (gameState == GameState.COMPLETED) {

            // Making worker idle and displaying the final position
            isWorkerIdle = true;
            repaint();

            if (gameLevelCompletionListener != null)
                gameLevelCompletionListener.levelCompleted(gameLevel);

            // Stopping the game
            stop();

            // Jumping to the next game level
            levelsSet.goToNextLevel(true);
            startLevel(levelsSet.getCurrentLevelIndex());

            // Finishing thread execution here
            return;
        }

        repaint();
    }
}