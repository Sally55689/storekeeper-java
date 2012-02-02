package org.ezze.games.storekeeper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class stores an inner representation of storekeeper's level.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.3
 */
public class Level {

    /**
     * Default maximal count of level items per line.
     */
    public static final int DEFAULT_LEVEL_WIDTH = 20;
    
    /**
     * Default maximal count of level items per column.
     */
    public static final int DEFAULT_LEVEL_HEIGHT = 20;
    
    /**
     * Minimal value of maximal count of level items per line.
     */
    public static final int MINIMAL_LEVEL_WIDTH = 20;
    
    /**
     * Minimal value of maximal count of level items per column.
     */
    public static final int MINIMAL_LEVEL_HEIGHT = 20;
    
    /**
     * Maximal value of maximal count of level items per line.
     */
    public static final int MAXIMAL_LEVEL_WIDTH = 40;
    
    /**
     * Maximal value of maximal count of level items per column.
     */
    public static final int MAXIMAL_LEVEL_HEIGHT = 35;
    
    /**
     * Character used to represent worker's item.
     */
    public static final Character LEVEL_ITEM_WORKER = '@';
    
    /**
     * Character used to represent staying in cell worker's item.
     */
    public static final Character LEVEL_ITEM_WORKER_IN_CELL = '+';
    
    /**
     * Character used to represent brick's item.
     */
    public static final Character LEVEL_ITEM_BRICK = '#';
    
    /**
     * Character used to represent cell's item.
     */
    public static final Character LEVEL_ITEM_CELL = '.';
    
    /**
     * Character used to represent box' item.
     */
    public static final Character LEVEL_ITEM_BOX = '$';
    
    /**
     * Character used to represent staying in cell box' item.
     */
    public static final Character LEVEL_ITEM_BOX_IN_CELL = '*';
    
    /**
     * Character used to represent empty space's item.
     */
    public static final Character LEVEL_ITEM_SPACE = ' ';
    
    /**
     * List of allowed level items.
     */
    public static final ArrayList<Character> allowedLevelItems = new ArrayList<Character>();
    static {

        allowedLevelItems.add(LEVEL_ITEM_WORKER);
        allowedLevelItems.add(LEVEL_ITEM_WORKER_IN_CELL);
        allowedLevelItems.add(LEVEL_ITEM_BRICK);
        allowedLevelItems.add(LEVEL_ITEM_CELL);
        allowedLevelItems.add(LEVEL_ITEM_BOX);
        allowedLevelItems.add(LEVEL_ITEM_BOX_IN_CELL);
        allowedLevelItems.add(LEVEL_ITEM_SPACE);
    }
    
    public static enum LevelState {
        
        EMPTY,        
        OUT_OF_BOUNDS,
        CORRUPTED,
        PLAYABLE
    }
    
    /**
     * Represents a type of a move attempted by {@link #move(int, int)} method.
     */
    public static enum MoveType {

        /**
         * Nothing has been changed after the attempt to move.
         * 
         * This value is linked with {@link MoveDirection#NONE}.
         */
        NOTHING,
        
        /**
         * Worker has moved.
         */
        WORKER,
        
        /**
         * Worker has moved with a box.
         */
        WORKER_AND_BOX
    };
    
    /**
     * Represents a direction of a move attempted by {@link #move(int, int)} method.
     */
    public static enum MoveDirection {
        
        /**
         * Nothing has been changed after the attempt to move.
         * 
         * This value is linked with {@link MoveType#NOTHING}.
         */
        NONE,
        
        /**
         * Worker moved up.
         */
        UP,
        
        /**
         * Worker moved right.
         */
        RIGHT,
        
        /**
         * Worker moved down.
         */
        DOWN,
        
        /**
         * Worker moved left.
         */
        LEFT
    }
    
    /**
     * This class accumulates the information about a move
     * performed by the worker.
     */
    public class MoveInformation {
        
        /**
         * Describes performed move's type.
         */
        private MoveType moveType = MoveType.NOTHING;
        
        /**
         * Stores performed move's direction.
         */
        private MoveDirection moveDirection = MoveDirection.NONE;
        
        /**
         * Creates information of an empty move meaning that the worker didn't move.
         */
        public MoveInformation() {
            
        }
        
        /**
         * Creates information of a move with specified type and direction.
         * 
         * @param moveType
         *      Performed move's type.
         * @param moveDirection
         *      Performed move's direction.
         */
        public MoveInformation(MoveType moveType, MoveDirection moveDirection) {
            
            if (moveType == null || moveType == MoveType.NOTHING ||
                    moveDirection == null || moveDirection == MoveDirection.NONE) {
                
                return;
            }
            
            this.moveType = moveType;
            this.moveDirection = moveDirection;
        }
        
        /**
         * Retrieves performed move's type.
         * 
         * @return 
         *      Move's type.
         */
        public MoveType getType() {
            
            return moveType;
        }
        
        /**
         * Retrieves performed move's direction.
         * 
         * @return 
         *      Move's direction.
         */
        public MoveDirection getDirection() {
            
            return moveDirection;
        }
    }
    
    /**
     * Shows whether the level is initialized.
     */
    //private boolean isInitialized = false;
    private LevelState levelState = LevelState.EMPTY;
    
    /**
     * Stores level's initial state characters.
     */
    private ArrayList<ArrayList<Character>> levelInitial = null;
    
    /**
     * Stores level's current state with empty lines and columns appended
     * to make level's size equal to {@link #maximalLevelWidth} and
     * {@link #maximalLevelHeight}.
     */
    private ArrayList<ArrayList<Character>> level = null;
    
    /**
     * Keeps level's information.
     */
    private HashMap<String, Object> levelInfo = null;
    
    /**
     * Restricts level's width (horizontal items' count).
     */
    private int maximalLevelWidth = DEFAULT_LEVEL_WIDTH;
    
    /**
     * Restricts level's height (vertical items' count).
     */
    private int maximalLevelHeight = DEFAULT_LEVEL_HEIGHT;
    
    /**
     * Keeps cells count of the level.
     */
    private int cellsCount = 0;
    
    /**
     * Keeps boxes count of the level.
     */
    private int boxesCount = 0;
    
    /**
     * Traces boxes count placed in the cells.
     */
    private int boxesInCellsCount = 0;
    
    /**
     * Keeps current worker position's horizontal index within the range [0; {@link #getMaximalLevelWidth()} - 1].
     */
    private int workerX = 0;
    
    /**
     * Keeps current worker position's vertical index within the range [0; {@link #getMaximalLevelHeight()} - 1].
     */
    private int workerY = 0;
    
    /**
     * Traces worker's moves count.
     */
    private int movesCount = 0;
    
    /**
     * Traces worker's pushes count.
     */
    private int pushesCount = 0;
    
    /**
     * Keeps an information about performed moves.
     */
    private ArrayList<MoveInformation> movesHistory = new ArrayList<MoveInformation>();
    
    /**
     * Level's default constructor.
     * 
     * @param levelLines
     *      Lines to initialize a level from.
     * @param levelInfo 
     *      Level's information.
     * @see #Level(java.util.ArrayList, java.util.HashMap, int, int)
     */
    public Level(ArrayList<String> levelLines, HashMap<String, Object> levelInfo) {
        
        this(levelLines, levelInfo, DEFAULT_LEVEL_WIDTH, DEFAULT_LEVEL_HEIGHT);
    }
    
    /**
     * Level's advanced constructor.
     * 
     * This one creates a level restricted by specified level's maximal size.
     * 
     * @param levelLines
     *      Lines to initialize a level from.
     * @param levelInfo
     *      Level's information.
     * @param maximalLevelWidth
     *      Level's maximal width in items.
     * @param maximalLevelHeight 
     *      Level's maximal height in items.
     * @see #Level(java.util.ArrayList, java.util.HashMap)
     */
    public Level(ArrayList<String> levelLines, HashMap<String, Object> levelInfo, int maximalLevelWidth, int maximalLevelHeight) {
        
        // Checking whether level lines are specified
        if (levelLines == null || levelLines.size() < 1)
            return;
        
        // Trying to set level's maximal size
        if (!setMaximalLevelSize(maximalLevelWidth, maximalLevelHeight))
            return;
        
        levelInitial = new ArrayList<ArrayList<Character>>();
        int levelLineIndex = 0;
        while (levelLineIndex < levelLines.size()) {
            
            String levelLine = levelLines.get(levelLineIndex);
            ArrayList<Character> levelRow = new ArrayList<Character>();
            for (int levelLineCharacterIndex = 0; levelLineCharacterIndex < levelLine.length(); levelLineCharacterIndex++) {
                
                Character levelLineCharacter = levelLine.charAt(levelLineCharacterIndex);
                if (allowedLevelItems.contains(levelLineCharacter))
                    levelRow.add(levelLineCharacter);
                else
                    levelRow.add(LEVEL_ITEM_SPACE);
            }
            levelInitial.add(levelRow);
            
            levelLineIndex++;
        }
        
        this.levelInfo = levelInfo;
        
        initialize();
    }
    
    /**
     * Sets level's maximal width in items.
     * 
     * Desired value must be no less than {@link #MINIMAL_LEVEL_WIDTH} and
     * no more than {@link #MAXIMAL_LEVEL_WIDTH}.
     * 
     * @param maximalLevelWidth
     *      Level's desired maximal width in items.
     * @return 
     *      {@code true} if desired value has been set, {@code false} otherwise.
     * @see #setMaximalLevelHeight(int)
     * @see #setMaximalLevelSize(int, int)
     * @see #getMaximalLevelWidth()
     */
    public final boolean setMaximalLevelWidth(int maximalLevelWidth) {
        
        if (maximalLevelWidth < MINIMAL_LEVEL_WIDTH || maximalLevelWidth > MAXIMAL_LEVEL_WIDTH)
            return false;
        this.maximalLevelWidth = maximalLevelWidth;
        return true;
    }
    
    /**
     * Sets level's maximal height in items.
     * 
     * Desired value must be no less than {@link #MINIMAL_LEVEL_HEIGHT} and
     * no more than {@link #MAXIMAL_LEVEL_HEIGHT}.
     * 
     * @param maximalLevelHeight
     *      Level's desired maximal height in items.
     * @return 
     *      {@code true} if desired value has been set, {@code false} otherwise.
     * @see #setMaximalLevelWidth(int)
     * @see #setMaximalLevelSize(int, int)
     * @see #getMaximalLevelHeight()
     */
    public final boolean setMaximalLevelHeight(int maximalLevelHeight) {
        
        if (maximalLevelHeight < MINIMAL_LEVEL_HEIGHT || maximalLevelHeight > MAXIMAL_LEVEL_HEIGHT)
            return false;
        this.maximalLevelHeight = maximalLevelHeight;
        return true;
    }
    
    /**
     * Sets level's maximal size in items.
     * 
     * @param maximalLevelWidth
     *      Level's desired maximal width in items.
     * @param maximalLevelHeight
     *      Level's desired maximal height in items.
     * @return 
     *      {@code true} if desired width and height have been set, {@code false} otherwise.
     * @see #setMaximalLevelWidth(int)
     * @see #setMaximalLevelHeight(int)
     * @see #getMaximalLevelWidth()
     * @see #getMaximalLevelHeight()
     */
    public final boolean setMaximalLevelSize(int maximalLevelWidth, int maximalLevelHeight) {
        
        int currentMaximalLevelWidth = this.maximalLevelWidth;
        if (!setMaximalLevelWidth(maximalLevelWidth)) {
            
            return false;
        }
        
        if (!setMaximalLevelHeight(maximalLevelHeight)) {
        
            this.maximalLevelWidth = currentMaximalLevelWidth;
            return false;
        }
        
        return true;
    }
    
    /**
     * Retrieves currently set level's maximal width in items.
     * 
     * @return
     *      Level's maximal width in items.
     * @see #setMaximalLevelWidth(int)
     * @see #setMaximalLevelSize(int, int)
     */
    public int getMaximalLevelWidth() {
        
        return maximalLevelWidth;
    }
    
    /**
     * Retrieves currently set level's maximal height in items.
     * 
     * @return 
     *      Level's maximal height in items.
     * @see #setMaximalLevelHeight(int)
     * @see #setMaximalLevelSize(int, int)
     */
    public int getMaximalLevelHeight() {
        
        return maximalLevelHeight;
    }
    
    /**
     * Completes level's initialization.
     * 
     * This method checks whether level's initial source {@link #levelInitial}
     * is valid, consists of only one worker and equal count of cells and boxes.
     * After that it adds additional empty horizontal and vertical lines to
     * center the level in a box of ({@link #getMaximalLevelWidth()},
     * {@link #getMaximalLevelHeight()}) size.
     * 
     * @return
     *      {@code true} if level has been successfully initialized, {@code false otherwise}.
     */
    public final boolean initialize() {

        levelState = LevelState.EMPTY;
        
        cellsCount = 0;
        boxesCount = 0;
        boxesInCellsCount = 0;
        int workersCount = 0;
        workerX = 0;
        workerY = 0;
        movesCount = 0;
        pushesCount = 0;
        movesHistory = new ArrayList<MoveInformation>();
        
        if (levelInitial == null || levelInitial.isEmpty())
            return false;
        
        if (levelInitial.size() > maximalLevelHeight) {
        
            levelState = LevelState.OUT_OF_BOUNDS;
            return false;
        }
        
        int maxLineWidth = 0;
        int lineIndex = 0;
        while (lineIndex < levelInitial.size()) {

            ArrayList<Character> line = levelInitial.get(lineIndex);
            if (line.size() > maxLineWidth)
                maxLineWidth = line.size();
            for (int lineCharacterIndex = 0; lineCharacterIndex < line.size(); lineCharacterIndex++) {

                Character lineCharacter = line.get(lineCharacterIndex);
                if (lineCharacter.equals(LEVEL_ITEM_WORKER)) {

                    workersCount++;
                    workerX = lineCharacterIndex;
                    workerY = lineIndex;
                }
                else if (lineCharacter.equals(LEVEL_ITEM_WORKER_IN_CELL)) {

                    workersCount++;
                    cellsCount++;
                    workerX = lineCharacterIndex;
                    workerY = lineIndex;
                    levelInitial.get(lineIndex).set(lineCharacterIndex, LEVEL_ITEM_CELL);
                }
                else if (lineCharacter.equals(LEVEL_ITEM_CELL)) {

                    cellsCount++;
                }
                else if(lineCharacter.equals(LEVEL_ITEM_BOX)) {

                    boxesCount++;
                }
                else if (lineCharacter.equals(LEVEL_ITEM_BOX_IN_CELL)) {

                    boxesCount++;
                    cellsCount++;
                    boxesInCellsCount++;
                }
            }

            lineIndex++;
        }

        // Checking whether level parameters are valid
        if (maxLineWidth > maximalLevelWidth) {
            
            levelState = LevelState.OUT_OF_BOUNDS;
            return false;
        }
        
        if (boxesCount != cellsCount || workersCount != 1) {
         
            levelState = LevelState.CORRUPTED;
            return false;
        }
        
        // Cloning level's instance for playing
        level = new ArrayList<ArrayList<Character>>();
        for (ArrayList<Character> levelInitialRow : levelInitial) {
            
            ArrayList<Character> levelRow = new ArrayList<Character>();
            for (Character levelInitialRowCharacter : levelInitialRow)
                levelRow.add(new Character(levelInitialRowCharacter));
            level.add(levelRow);
        }

        // Level's vertical normalization
        int leadingEmptyLinesCount = (int)(Math.floor((double)maximalLevelHeight - (double)level.size()) / 2);
        int trailingEmptyLinesCount = maximalLevelHeight - level.size() - leadingEmptyLinesCount;

        // Shifting worker's vertical location
        workerY += leadingEmptyLinesCount;

        // Adding leading empty rows
        int leadingEmptyLineIndex = 0;
        while (leadingEmptyLineIndex < leadingEmptyLinesCount) {

            ArrayList<Character> emptyLine = new ArrayList<Character>();
            for (int emptyLineCharacterIndex = 0; emptyLineCharacterIndex < maximalLevelWidth; emptyLineCharacterIndex++)
                emptyLine.add(LEVEL_ITEM_SPACE);
            level.add(0, emptyLine);
            leadingEmptyLineIndex++;
        }

        // Adding trailing empty rows
        int trailingEmptyLineIndex = 0;
        while (trailingEmptyLineIndex < trailingEmptyLinesCount) {

            ArrayList<Character> emptyLine = new ArrayList<Character>();
            for (int emptyLineCharacterIndex = 0; emptyLineCharacterIndex < maximalLevelWidth; emptyLineCharacterIndex++)
                emptyLine.add(LEVEL_ITEM_SPACE);
            level.add(emptyLine);
            trailingEmptyLineIndex++;
        }

        // Level's horizontal normalization
        int leadingEmptyCharactersCount = (int)(Math.floor((double)maximalLevelWidth - (double)maxLineWidth) / 2);

        // Shifting worker's horizontal location
        workerX += leadingEmptyCharactersCount;

        lineIndex = leadingEmptyLinesCount;
        while (lineIndex < maximalLevelHeight - trailingEmptyLinesCount) {

            ArrayList<Character> line = level.get(lineIndex);
            int emptyCharacterIndex = 0;
            while (emptyCharacterIndex < leadingEmptyCharactersCount) {

                line.add(0, LEVEL_ITEM_SPACE);
                emptyCharacterIndex++;
            }

            while (line.size() < maximalLevelWidth)
                line.add(LEVEL_ITEM_SPACE);

            lineIndex++;
        }
        
        levelState = LevelState.PLAYABLE;
        return true;
    }
    
    /**
     * Retrieves level's state.
     * 
     * @return
     *      Level's state
     */
    public LevelState getState() {

        return levelState;
    }
    
    /**
     * Checks whether level is playable.
     * 
     * @return 
     *      {@code true} is level is playable, {@code false} otherwise.
     */
    public boolean isPlayable() {
        
        return levelState == LevelState.PLAYABLE;
    }
    
    /**
     * Retrieves level's name.
     * 
     * @return 
     *      Level's name or empty string if it's not determined.
     */
    public String getName() {
        
        try {
            
            return (String)levelInfo.get("name");
        }
        catch (NullPointerException ex) {
            
        }
        catch (ClassCastException ex) {
            
        }
        
        return "";
    }
    
    /**
     * Retrieves current item character at specified position.
     * 
     * Look at {@link #allowedLevelItems} for possible character values.
     * 
     * @param line
     *      Level's line index within the range [0; {@link #getMaximalLevelHeight()} - 1].
     * @param column
     *      Level's column index within the range [0; {@link #getMaximalLevelWidth()} - 1].
     * @return 
     *      Character of the item.
     * @see #setItemAt(java.lang.Character, int, int)
     * @see #allowedLevelItems
     */
    public Character getItemAt(int line, int column) {

        if (levelState == LevelState.EMPTY || levelState == LevelState.OUT_OF_BOUNDS)
            return null;

        if (line < 0 || line >= level.size())
            return LEVEL_ITEM_BRICK;
        
        ArrayList<Character> levelLine = level.get(line);
        if (column < 0 || column >= levelLine.size())
            return LEVEL_ITEM_BRICK;
        
        return levelLine.get(column);
    }
    
    /**
     * Sets specified item character at specified position.
     * 
     * Look at {@link #allowedLevelItems} for possible character values.
     * 
     * @param levelItem
     *      Item character to set.
     * @param line
     *      Level's line index within the range [0; {@link #getMaximalLevelHeight()} - 1].
     * @param column
     *      Level's column index within the range [0; {@link #getMaximalLevelWidth()} - 1].
     * @return
     *      {@code true} if character has been set, {@code false} otherwise.
     * @see #getItemAt(int, int)
     * @see #allowedLevelItems
     */
    public boolean setItemAt(Character levelItem, int line, int column) {

        if (levelState == LevelState.EMPTY || levelState == LevelState.OUT_OF_BOUNDS)
            return false;
        
        if (!allowedLevelItems.contains(levelItem))
            return false;

        level.get(line).set(column, levelItem);
        return true;
    }

    /**
     * Retrieves worker's current horizontal position.
     *
     * @return
     *      Worker's horizontal position
     * @see #getWorkerY()
     * @see #getWorkerLocation()
     */
    public int getWorkerX() {

        return workerX;
    }

    /**
     * Retrieves worker's current vertical position.
     *
     * @return
     *      Worker's vertical position
     * @see #getWorkerX()
     * @see #getWorkerLocation()
     */
    public int getWorkerY() {

        return workerY;
    }

    /**
     * Retrieves worker's current location
     *
     * @return
     *      Worker's location
     * @see #getWorkerX()
     * @see #getWorkerY()
     */
    public Point getWorkerLocation() {

        return new Point(workerX, workerY);
    }
    
    /**
     * Retrieves moves count performed by the worker.
     * 
     * @return 
     *      Moves count.
     */
    public int getMovesCount() {

        return movesCount;
    }
    
    /**
     * Retrieves pushes count performed by the worker.
     * 
     * @return
     *      Pushes count.
     */
    public int getPushesCount() {
        
        return pushesCount;
    }
    
    /**
     * Retrieves count of moves stored in history.
     * 
     * @return 
     *      Count of history moves.
     */
    public int getMovesHistoryCount() {
        
        synchronized(SyncLock.getInstance().getLock(SyncLock.MOVES_HISTORY)) {
            
            return movesHistory.size();
        }
    }
    
    /**
     * Adds a move to history and increments {@link #movesCount} and {@link #pushesCount}
     * if it's necessary.
     * 
     * If one want to take level's position back he or she may use {@link #takeBack()}
     * or {@link #takeBack(int)} methods.
     * 
     * @param moveInformation
     *      An instance with information about recently performed move.
     * @see #takeBack()
     * @see #takeBack(int)
     */
    public void addMoveToHistory(MoveInformation moveInformation) {
        
        synchronized(SyncLock.getInstance().getLock(SyncLock.MOVES_HISTORY)) {
        
            if (moveInformation == null || moveInformation.getType().equals(MoveType.NOTHING) ||
                    moveInformation.getDirection().equals(MoveDirection.NONE)) {

                return;
            }

            while (movesHistory.size() > movesCount)
                movesHistory.remove(movesHistory.size() - 1);

            movesCount++;
            if (moveInformation.getType().equals(MoveType.WORKER_AND_BOX))
                pushesCount++;

            movesHistory.add(moveInformation);
        }
    }
    
    /**
     * Takes game level's position back by one move.
     * 
     * @return
     *      A number of performed moves after the take-back or {@code -1}
     *      if level is not initialized or moves' history is empty.
     * @see #takeBack(int)
     * @see #addMoveToHistory(org.ezze.games.storekeeper.Level.MoveInformation)
     */
    public int takeBack() {
        
        return takeBack(1);
    }
    
    /**
     * Takes game level's position back by specified moves' count.
     * 
     * @param takeBackMovesCount
     *      Moves' count to take game level's position back by.
     * @return
     *      A number of performed moves after the take-back or {@code -1}
     *      if level is not initialized or {@code takeBackMovesCount}
     *      is more than performed moves' count.
     * @see #takeBack()
     * @see #addMoveToHistory(org.ezze.games.storekeeper.Level.MoveInformation)
     */
    public int takeBack(int takeBackMovesCount) {
        
        synchronized(SyncLock.getInstance().getLock(SyncLock.MOVES_HISTORY)) {
        
            if (levelState != LevelState.PLAYABLE || takeBackMovesCount > getMovesCount())
                return -1;

            int lastRemovingMoveIndex = getMovesCount() - 1;
            int firstRemovingMoveIndex = lastRemovingMoveIndex - takeBackMovesCount + 1;
            int removingMoveIndex = lastRemovingMoveIndex;
            while (removingMoveIndex >= firstRemovingMoveIndex) {

                // Retrieving information of a move to be removed
                MoveInformation moveInformation = movesHistory.get(removingMoveIndex);
                if (!moveInformation.getType().equals(MoveType.NOTHING)) {

                    MoveDirection moveDirection = moveInformation.getDirection();                                

                    if (moveInformation.getType().equals(MoveType.WORKER_AND_BOX)) {

                        // Retrieving box' current coordinates
                        int boxX = workerX;
                        int boxY = workerY;
                        if (moveDirection == MoveDirection.LEFT)
                            boxX -= 1;
                        else if (moveDirection == MoveDirection.RIGHT)
                            boxX += 1;
                        else if (moveDirection == MoveDirection.UP)
                            boxY -= 1;
                        else if (moveDirection == MoveDirection.DOWN)
                            boxY += 1;

                        // Restoring previous item at box' current position
                        Character levelItem = getItemAt(boxY, boxX);
                        if (levelItem.equals(LEVEL_ITEM_BOX_IN_CELL))
                            setItemAt(LEVEL_ITEM_CELL, boxY, boxX);
                        else if (levelItem.equals(LEVEL_ITEM_BOX))
                            setItemAt(LEVEL_ITEM_SPACE, boxY, boxX);

                        // Retrieving box' previous coordinates (it's where the worker right now)
                        boxX = workerX;
                        boxY = workerY;

                        // Retrieving box' destination item
                        levelItem = getItemAt(boxY, boxX);
                        if (levelItem.equals(LEVEL_ITEM_CELL))
                            setItemAt(LEVEL_ITEM_BOX_IN_CELL, boxY, boxX);
                        else if (levelItem.equals(LEVEL_ITEM_SPACE))
                            setItemAt(LEVEL_ITEM_BOX, boxY, boxX);

                        // Decreasing pushes count
                        pushesCount--;
                    }

                    // Moving worker back
                    if (moveDirection == MoveDirection.LEFT)
                        workerX += 1;
                    else if (moveDirection == MoveDirection.RIGHT)
                        workerX -= 1;
                    else if (moveDirection == MoveDirection.UP)
                        workerY += 1;
                    else if (moveDirection == MoveDirection.DOWN)
                        workerY -= 1;
                    movesCount--;
                }

                removingMoveIndex--;
            }

            return movesCount;
        }
    }

    /**
     * Checks whether level is completed.
     *
     * @return
     *      {@code true} if level is completed, {@code false} otherwise
     */
    public boolean isCompleted() {

        return boxesCount == boxesInCellsCount;
    }

    /**
     * Completes worker's move with specified shifts if it's possible
     * 
     * @param workerDeltaX
     *      Worker's horizontal shift
     * @param workerDeltaY
     *      Worker's vertical shift
     * @return
     *      Completed move's type as number of {@link MoveType} enumeration
     */
    public MoveInformation move(int workerDeltaX, int workerDeltaY) {

        if (workerDeltaX == 0 && workerDeltaY == 0)
            return new MoveInformation(MoveType.NOTHING, MoveDirection.NONE);

        // Calculating worker's destination location
        int workerDestinationX = workerX + workerDeltaX;
        int workerDestinationY = workerY + workerDeltaY;

        // Checking that worker's destination position is not a wall
        Character workerDestinationLevelItem = getItemAt(workerDestinationY, workerDestinationX);
        if (workerDestinationLevelItem.equals(LEVEL_ITEM_BRICK))
            return new MoveInformation(MoveType.NOTHING, MoveDirection.NONE);
        
        // Defining worker's move direction
        MoveDirection moveDirection = MoveDirection.NONE;
        if (workerDeltaX > 0)
            moveDirection = MoveDirection.RIGHT;
        else if (workerDeltaX < 0)
            moveDirection = MoveDirection.LEFT;
        else if (workerDeltaY > 0)
            moveDirection = MoveDirection.DOWN;
        else if (workerDeltaY < 0)
            moveDirection = MoveDirection.UP;

        // Checking whether worker's destination position is a box
        if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX) || workerDestinationLevelItem.equals(LEVEL_ITEM_BOX_IN_CELL)) {

            // Looking for possibility to move the box
            int boxDestinationX = workerDestinationX + workerDeltaX;
            int boxDestinationY = workerDestinationY + workerDeltaY;

            // Checking whether the box' destination position is not a wall or another box
            Character boxDestinationLevelItem = getItemAt(boxDestinationY, boxDestinationX);
            if (boxDestinationLevelItem.equals(LEVEL_ITEM_BRICK) || boxDestinationLevelItem.equals(LEVEL_ITEM_BOX)
                    || boxDestinationLevelItem.equals(LEVEL_ITEM_BOX_IN_CELL))
                return new MoveInformation(MoveType.NOTHING, MoveDirection.NONE);

            // Removing the box from old location
            if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX))
                setItemAt(LEVEL_ITEM_SPACE, workerDestinationY, workerDestinationX);
            else if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX_IN_CELL)) {

                setItemAt(LEVEL_ITEM_CELL, workerDestinationY, workerDestinationX);
                boxesInCellsCount--;
            }

            // Placing the box in new location
            if (boxDestinationLevelItem.equals(LEVEL_ITEM_SPACE))
                setItemAt(LEVEL_ITEM_BOX, boxDestinationY, boxDestinationX);
            else if (boxDestinationLevelItem.equals(LEVEL_ITEM_CELL)) {

                setItemAt(LEVEL_ITEM_BOX_IN_CELL, boxDestinationY, boxDestinationX);
                boxesInCellsCount++;
            }

            workerX = workerDestinationX;
            workerY = workerDestinationY;

            // Adding the move to moves' history
            MoveInformation moveInformation = new MoveInformation(MoveType.WORKER_AND_BOX, moveDirection);
            addMoveToHistory(moveInformation);

            return moveInformation;
        }

        workerX = workerDestinationX;
        workerY = workerDestinationY;

        // Adding the move to moves' history
        MoveInformation moveInformation = new MoveInformation(MoveType.WORKER, moveDirection);
        addMoveToHistory(moveInformation);
        
        return moveInformation;
    }
}