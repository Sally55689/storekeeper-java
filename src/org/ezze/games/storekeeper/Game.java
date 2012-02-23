package org.ezze.games.storekeeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.text.AttributedString;
import javax.swing.JPanel;
import org.ezze.games.storekeeper.Level.LevelSize;
import org.ezze.games.storekeeper.Level.MoveInformation;
import org.ezze.games.storekeeper.Level.MoveType;
import org.ezze.games.storekeeper.Level.WorkerDirection;
import org.ezze.utils.io.XMLHelper;
import org.w3c.dom.Document;

/**
 * This class is the main part of the game implementing graphics,
 * animation and user actions' handling.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.7
 */
public class Game extends JPanel implements Runnable {
    
    public static final String LEVELS_SET = "levels_set";
    public static final String LEVEL_INDEX = "level_index";
    public static final String GAME_STATE = "game_state";
    public static final String MOVES_COUNT = "moves_count";
    public static final String TIME = "time";
    
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
    
    protected Configuration gameConfiguration = null;
    
    /**
     * A reference to an interface implementing game graphics,
     * particularly providing references to sprites' images.
     * 
     * This one must be passed as instance constructor's argument.
     * 
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics)
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics, org.ezze.games.storekeeper.LevelCompletionListener)
     */
    protected GameGraphics gameGraphics = null;
    
    /**
     * An instance of interface providing actions to do after level will have been completed.
     * 
     * @see #Game(org.ezze.games.storekeeper.Configuration, org.ezze.games.storekeeper.GameGraphics, org.ezze.games.storekeeper.LevelCompletionListener)
     */
    protected LevelCompletionListener gameLevelCompletionListener = null;
    
    /**
     * Keeps game's current state.
     * 
     * @see GameState
     */
    protected GameState gameState = null;
    
    /**
     * Shows whether default levels set is loaded.
     */
    protected boolean isDefaultLevelsSetLoaded = false;
    
    /**
     * Keeps a set of currently loaded levels.
     */
    protected LevelsSet levelsSet = null;
    
    /**
     * Measures current level's play time in seconds.
     */
    protected int levelTime = 0;
    
    /**
     * Represents game's loop, primarily used for {@link GameState#PLAY} state.
     */
    protected Thread gameLoopThread = null;
    
    /**
     * Stores worker's desired horizontal shift forced by user.
     * 
     * Can be equal to -1, 0, 1.
     */
    protected int workerDeltaX = 0;
    
    /**
     * Stores worker's desired vertical shift forced by user.
     * 
     * Can be equal to -1, 0, 1.
     */
    protected int workerDeltaY = 0;
    
    /**
     * Shows whether worker is idle right now.
     */
    protected boolean isWorkerIdle = true;
    
    /**
     * Shows whether animation is in progress.
     */
    protected boolean isAnimationInProgress = false;
    
    /**
     * Worker's animation phase's index.
     * 
     * This index must no less than 0 and less than
     * {@link GameGraphics#getActionSpritesCount(org.ezze.games.storekeeper.Level.WorkerDirection)}
     * of {@link #gameGraphics}. Animation phase with index 0 corresponds to idle worker
     * while another ones represent worker's motion.
     */
    protected int workerAnimPhase = 0;
    
    /**
     * Keeps worker's destination X coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This variable is a local copy of the current {@link Level#workerX}.
     * Default value -1 means that worker is idle and no animation is in progress.
     */
    protected int workerAnimDestX = -1;
    
    /**
     * Keeps worker's destination Y coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This variable is a local copy of the current {@link Level#workerY}.
     * Default value -1 means that worker is idle and no animation is in progress.
     */
    protected int workerAnimDestY = -1;
    
    /**
     * Keeps worker's current X animation coordinate of {@link Level} field.
     * 
     * Please note that this variable is used for animation purporses only
     * while X coordinate {@link Level#workerX} of worker's real position
     * is always integer.
     */
    protected double workerAnimCurrX = 0.0;
    
    /** 
     * Keeps worker's current Y animation coordinate of {@link Level} field.
     * 
     * Please note that this variable is used for animation purporses only
     * while Y coordinate {@link Level#workerY} of worker's real position
     * is always integer.
     */
    protected double workerAnimCurrY = 0.0;
    
    /**
     * Worker's X animation shift to be performed during current loop execution.
     * 
     * This one is determined as {@link GameGraphics#getAnimationStepShift()} of
     * {@link #gameGraphics} for each horizontal worker's animation.
     * 
     * Animation continues until {@link #workerAnimCurrX} reaches {@link #workerAnimDestX}
     * and {@link #workerAnimCurrY} reaches {@link #workerAnimDestY}.
     */
    protected double workerAnimDeltaX = 0.0;
    
    /**
     * Worker's Y animation shift to be performed during current loop execution.
     * 
     * This one is determined as {@link GameGraphics#getAnimationStepShift()} of
     * {@link #gameGraphics} for each vertical worker's animation.
     * 
     * Animation continues until {@link #workerAnimCurrX} reaches {@link #workerAnimDestX}
     * and {@link #workerAnimCurrY} reaches {@link #workerAnimDestY}.
     */
    protected double workerAnimDeltaY = 0.0;
    
    /**
     * Keeps moving box' destination X coordinate of {@link Level} field
     * and is used for animation purposes only.
     * 
     * This one is similar to {@link #workerAnimDestX} but relative to moving box.
     */
    protected int boxAnimDestX = -1;
    
    /**
     * Keeps moving box' destination Y coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This one is similar to {@link #workerAnimDestY} but relative to moving box.
     */
    protected int boxAnimDestY = -1;
    
    /**
     * Keeps moving box' current X animation coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This one is similar to {@link #workerAnimCurrX} but relative to moving box.
     */
    protected double boxAnimCurrX = 0.0;
    
    /**
     * Keeps moving box' current Y animation coordinate of {@link Level} field
     * and is used for animation purporses only.
     * 
     * This one is similar to {@link #workerAnimCurrY} but relative to moving box.
     */
    protected double boxAnimCurrY = 0.0;
    
    /**
     * Moving box' X shift to be performed during current loop execution.
     * 
     * This one is equal to zero or to {@link #workerAnimDeltaX} due to box' shift
     * can be caused only by similar worker's movement.
     */
    protected double boxAnimDeltaX = 0.0;
    
    /**
     * Moving box' Y shift to be performed during current loop execution.
     * 
     * This one is equal to zero or to {@link #workerAnimDeltaY} due to box' shift
     * can be caused only by similar worker's movement.
     */
    protected double boxAnimDeltaY = 0.0;
    
    /**
     * Knows whether level's information is to be shown on game's field.
     */
    protected boolean displayLevelInfo = true;
    
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
        setGameState(GameState.INTRODUCTION);
        
        // No levels are loaded by default
        levelsSet = new LevelsSet(this.gameConfiguration);
        
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
    
    protected final void setGameState(GameState gameState) {
        
        if (gameState == null)
            return;

        GameState oldGameState = this.gameState;
        this.gameState = gameState;
        firePropertyChange(GAME_STATE, oldGameState, this.gameState);
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
     * Loads default levels' set.
     * 
     * @return 
     *      {@code true} if levels' set has been loaded, {@code false} otherwise.
     */
    public boolean loadDefaultLevelsSet() {
        
        String resourcePathToLevelsSet = String.format("/%s/resources/levels.xml",
                Game.class.getPackage().getName().replace('.', '/'));
        InputStream levelsSetInputStream = Game.class.getResourceAsStream(resourcePathToLevelsSet);
        if (levelsSetInputStream == null)
            return false;
        
        Document xmlLevelsSetDocument = XMLHelper.readXMLDocument(levelsSetInputStream);
        return loadLevelsSet(xmlLevelsSetDocument, true);
    }
    
    public boolean loadLevelsSet(Object source) {
        
        return loadLevelsSet(source, false);
    }
    
    /**
     * Loads levels' set from specified source.
     * 
     * @param source
     *      Source file's name or DOM document.
     * @return 
     *      {@code true} if levels' set has been loaded, {@code false} otherwise.
     */
    public boolean loadLevelsSet(Object source, boolean isDefaultLevelsSet) {
        
        // Reading levels from the source
        LevelsSet loadedLevelsSet = new LevelsSet(source);
        
        // We suppose levels' set is loaded when at least one level has been retrieved.
        boolean isLoaded = loadedLevelsSet.isInitialized();
        if (isLoaded) {
            
            // Stopping the game if it's running
            stop(true);
            
            // Setting a reference to loaded levels' set
            levelsSet = loadedLevelsSet;
            isDefaultLevelsSetLoaded = isDefaultLevelsSet;
        }
    
        if (isLoaded)
            firePropertyChange(LEVELS_SET, null, null);
        
        return isLoaded;
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
        if (!gameLevel.initialize(gameLevel.getMaximalSize())) {
         
            repaint();
            return false;
        }
        
        if (!levelsSet.setCurrentLevelByIndex(gameLevelIndex, true)) {
            
            repaint();
            return false;
        }
        
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
        setGameState(GameState.PLAY);
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
        
        setGameState(switchToIntroduction ? GameState.INTRODUCTION : GameState.STOP);
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
        int oldLevelIndex = levelsSet.getCurrentLevelIndex();
        if (levelsSet.goToPreviousLevel(true)) {
            
            int newLevelIndex = levelsSet.getCurrentLevelIndex();
            firePropertyChange(LEVEL_INDEX, oldLevelIndex, newLevelIndex);
        }
        
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
        int oldLevelIndex = levelsSet.getCurrentLevelIndex();
        if (levelsSet.goToNextLevel(true)) {
            
            int newLevelIndex = levelsSet.getCurrentLevelIndex();
            firePropertyChange(LEVEL_INDEX, oldLevelIndex, newLevelIndex);
        }
        
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
        
        int oldMovesCount = getLevelsSet().getCurrentLevel().getMovesCount();
        int newMovesCount = gameLevel.takeBack(takeBackMovesCount);
        if (oldMovesCount != newMovesCount)
            firePropertyChange(MOVES_COUNT, oldMovesCount, newMovesCount);
        return newMovesCount;
    }
    
    public int repeatMove() {
        
        return repeatMoves(1);
    }
    
    public int repeatMoves(int repeatMovesCount) {
        
        // Checking whether game is in play state
        if (!gameState.equals(GameState.PLAY))
            return -1;
        
        // Retrieving a reference to current game level
        Level gameLevel = levelsSet.getCurrentLevel();
        if (gameLevel == null)
            return -1;
        
        // Preventing from move repeat when the worker is moving
        if (!isWorkerIdle)
            return -1;
        
        int oldMovesCount = getLevelsSet().getCurrentLevel().getMovesCount();
        int newMovesCount = gameLevel.repeatMoves(repeatMovesCount);
        if (oldMovesCount != newMovesCount)
            firePropertyChange(MOVES_COUNT, oldMovesCount, newMovesCount);
        return newMovesCount;
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
    
    public void setDisplayLevelInfo(boolean displayLevelInfo) {
        
        this.displayLevelInfo = displayLevelInfo;
    }
    
    public int getTimeInSeconds() {
        
        return levelTime;
    }
    
    public String getTimeString() {
        
        int fixedLevelTime = levelTime;
        int seconds = fixedLevelTime % 60;
        int minutes = ((fixedLevelTime - seconds) / 60) % 60;
        int hours = (fixedLevelTime - seconds - 60 * minutes) / (60 * 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
            LevelSize maximalLevelSize = gameLevel.getMaximalSize();
            
            // Retrieving sprites' dimension
            Dimension spriteDimension = gameGraphics.getSpriteDimension();
            
            // Drawing game level's current state
            for (int lineIndex = 0; lineIndex < maximalLevelSize.getHeight(); lineIndex++) {

                for (int columnIndex = 0; columnIndex < maximalLevelSize.getWidth(); columnIndex++) {

                    Character levelItem = gameLevel.getItemAt(lineIndex, columnIndex);
                    Image levelItemSprite = null;

                    if (levelItem.equals(Level.LEVEL_ITEM_GOAL)) {

                        levelItemSprite = gameGraphics.getGoalSprite();
                    }
                    else if (levelItem.equals(Level.LEVEL_ITEM_BOX)) {

                        if (!isWorkerIdle && boxAnimDestX == columnIndex && boxAnimDestY == lineIndex)
                            levelItemSprite = null;
                        else
                            levelItemSprite = gameGraphics.getBoxSprite();
                    }
                    else if (levelItem.equals(Level.LEVEL_ITEM_BOX_ON_GOAL)) {

                        if (!isWorkerIdle && boxAnimDestX == columnIndex && boxAnimDestY == lineIndex)
                            levelItemSprite = gameGraphics.getGoalSprite();
                        else
                            levelItemSprite = gameGraphics.getBoxOnGoalSprite();
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
                Image workerSprite = gameGraphics.getActionSprite(gameLevel.getWorkerDirection(), 0);
                if (workerSprite != null) {
                    
                    g2d.drawImage(workerSprite, workerLocation.x * spriteDimension.width,
                            workerLocation.y * spriteDimension.height, this);
                }
            }
            else {

                // Moving worker
                WorkerDirection workerDirection = gameLevel.getWorkerDirection();
                Image workerSprite = gameGraphics.getActionSprite(workerDirection,
                        gameGraphics.getActionSpritesCount(workerDirection) > 1 ? workerAnimPhase : 0);
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
        
        if (displayLevelInfo && (gameState == GameState.INTRODUCTION || gameState == GameState.PLAY ||
                gameState == GameState.COMPLETED) && gameLevel != null) {
            
            // Defining information lines' offsets
            int infoLineHorizontalOffset = fontMetrics.stringWidth(" ");
            int topInfoLineOffset = gameGraphics.getSpriteDimension().height -
                    (gameGraphics.getSpriteDimension().height - gameFont.getSize()) / 2 - 1;
            int bottomInfoLineOffset = topInfoLineOffset +
                    gameGraphics.getSpriteDimension().height * (gameLevel.getMaximalHeight() - 1);
            
            // Printing level information
            String levelNameTitle = "Level: ";
            String gameLevelName = gameLevel.getName();
            String levelNameText = String.format("%03d", levelsSet.getCurrentLevelIndex() + 1);
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
                int maximalLevelWidth = gameLevel.getMaximalWidth();
                
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

                    setGameState(GameState.COMPLETED);
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

                        // Firing level position property change
                        int movesCount = gameLevel.getMovesCount();
                        firePropertyChange(MOVES_COUNT, movesCount - 1, movesCount);
                        
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
                if (workerAnimPhase >= gameGraphics.getActionSpritesCount(gameLevel.getWorkerDirection()))
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
            Dimension spriteSize = getGameGraphics().getSpriteDimension();
            int repaintX = spriteSize.width * (gameLevel.getWorkerX() - 1);
            int repaintY = spriteSize.height * (gameLevel.getWorkerY() - 1);
            Rectangle rectangle = new Rectangle(repaintX, repaintY, spriteSize.width * 3, spriteSize.height * 3);
            repaint(rectangle);
            
            // Repainting level information
            if (displayLevelInfo) {
                
                Rectangle topRectangle = new Rectangle(0, 0,
                        spriteSize.width * gameLevel.getMaximalWidth(), spriteSize.height);
                repaint(topRectangle);
                Rectangle bottomRectangle = new Rectangle(0, spriteSize.height * (gameLevel.getSize().getHeight() - 1),
                        spriteSize.width * gameLevel.getMaximalWidth(), spriteSize.height);
                repaint(bottomRectangle);
            }
                    
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

            // Updating time
            long levelTimeInMilliseconds = System.currentTimeMillis() - levelStartTime;
            int oldLevelTime = levelTime;
            levelTime = (int)(levelTimeInMilliseconds / 1000);
            if (levelTime > oldLevelTime)
                firePropertyChange(TIME, oldLevelTime, levelTime);
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