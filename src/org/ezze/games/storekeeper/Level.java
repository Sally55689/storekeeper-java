package org.ezze.games.storekeeper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class stores an inner representation of storekeeper's level.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.6
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
    
    public static final String LEVEL_ITEM_WORKER_REG = "@";
    
    /**
     * Character used to represent staying on goal worker's item.
     */
    public static final Character LEVEL_ITEM_WORKER_ON_GOAL = '+';
    
    public static final String LEVEL_ITEM_WORKER_ON_GOAL_REG = "\\+";
    
    /**
     * Character used to represent brick's item.
     */
    public static final Character LEVEL_ITEM_BRICK = '#';
    
    public static final String LEVEL_ITEM_BRICK_REG = "#";
    
    /**
     * Character used to represent goal's item.
     */
    public static final Character LEVEL_ITEM_GOAL = '.';
    
    public static final String LEVEL_ITEM_GOAL_REG = "\\.";
    
    /**
     * Character used to represent box' item.
     */
    public static final Character LEVEL_ITEM_BOX = '$';
    
    public static final String LEVEL_ITEM_BOX_REG = "\\$";
    
    /**
     * Character used to represent staying on goal box' item.
     */
    public static final Character LEVEL_ITEM_BOX_ON_GOAL = '*';
    
    public static final String LEVEL_ITEM_BOX_ON_GOAL_REG = "\\*";
    
    /**
     * Character used to represent empty space's item.
     */
    public static final Character LEVEL_ITEM_SPACE = ' ';
    
    public static final String LEVEL_ITEM_SPACE_REG = " ";
    
    /**
     * List of allowed level items.
     */
    public static final ArrayList<Character> allowedLevelItems = new ArrayList<Character>();
    static {

        allowedLevelItems.add(LEVEL_ITEM_WORKER);
        allowedLevelItems.add(LEVEL_ITEM_WORKER_ON_GOAL);
        allowedLevelItems.add(LEVEL_ITEM_BRICK);
        allowedLevelItems.add(LEVEL_ITEM_GOAL);
        allowedLevelItems.add(LEVEL_ITEM_BOX);
        allowedLevelItems.add(LEVEL_ITEM_BOX_ON_GOAL);
        allowedLevelItems.add(LEVEL_ITEM_SPACE);
    }
    
    /**
     * Enumerates level's possible states.
     */
    public static enum LevelState {
        
        /**
         * Means that level is not created or initialized.
         * 
         * This value must be strongly avoided.
         */
        EMPTY,
        
        /**
         * Means that level is valid but cannot be played with specified
         * maximal level's size (see {@link #getMaximalWidth()}
         * and {@link #getMaximalHeight()}.
         */
        OUT_OF_BOUNDS,
        
        /**
         * Means that level is invalid and cannot be played in any case.
         */
        CORRUPTED,
        
        /**
         * Means that level is valid and can be played with current maximal
         * level's size.
         */
        PLAYABLE
    }
    
    /**
     * Represents a direction of a move attempted by {@link #move(int, int)} method.
     */
    public static enum Direction {
        
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
     * Represents a type of a move attempted by {@link #move(int, int)} method.
     */
    public static enum MoveType {

        /**
         * Nothing has been changed after the attempt to move.
         * 
         * This value is linked with {@link Direction#NONE}.
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
     * This class accumulates the information about a move
     * performed by the worker.
     */
    public static class MoveInformation {
        
        /**
         * Describes performed move's type.
         */
        protected MoveType moveType = MoveType.NOTHING;
        
        /**
         * Stores performed move's direction.
         */
        protected Direction moveDirection = Direction.NONE;
        
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
        public MoveInformation(MoveType moveType, Direction moveDirection) {
            
            if (moveType == null || moveType == MoveType.NOTHING ||
                    moveDirection == null || moveDirection == Direction.NONE) {
                
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
        public Direction getDirection() {
            
            return moveDirection;
        }
    }
    
    /**
     * This class represents worker's compound look direction.
     * 
     * It consists of two directions - horizontal and vertical, and can be used
     * for rendering purporses when one direction (e.g. vertical) is absent.
     */
    public static class WorkerDirection {
        
        /**
         * Describes worker's horizontal direction.
         */
        protected Direction horizontal = Direction.LEFT;
        
        /**
         * Describes worker's vertical direction.
         */
        protected Direction vertical = Direction.DOWN;
        
        /**
         * Shows whether vertical direction represents a real direction.
         */
        protected boolean isVerticalReal = false;
        
        /**
         * Worker's direction default constructor with horizontal direction equal
         * to {@link Direction#LEFT} and supposed as real one, and vertical
         * direction equal to {@link Direction#DOWN}.
         */
        public WorkerDirection() {
        }
        
        /**
         * Worker's direction advanced constructor.
         * 
         * @param horizontal
         *      Specifies horizontal direction.
         * @param vertical
         *      Specifies vertical direction.
         * @param isVerticalReal 
         *      If it's set to {@code true} then vertical direction represents a real direction,
         *      otherwise this one is represented by horizontal direction.
         */
        public WorkerDirection(Direction horizontal, Direction vertical, boolean isVerticalReal) {
            
            if (horizontal == Direction.LEFT || horizontal == Direction.RIGHT)
                this.horizontal = horizontal;
            if (vertical == Direction.UP || vertical == Direction.DOWN)
                this.vertical = vertical;
            this.isVerticalReal = isVerticalReal;
        }
        
        /**
         * Retrieves horizontal direction.
         * 
         * @return 
         *      Worker's horizontal direction.
         */
        public Direction getHorizontal() {
            
            return horizontal;
        }
        
        /**
         * Retrieves vertical direction.
         * 
         * @return 
         *      Worker's vertical direction.
         */
        public Direction getVertical() {
            
            return vertical;
        }
        
        /**
         * Show whether vertical direction represents a real direction.
         * 
         * @return 
         *      {@code true} if vertical direction represents a real direction,
         *      {@code false} if horizontal direction represents this one.
         */
        public boolean isVerticalReal() {
            
            return isVerticalReal;
        }
        
        /**
         * Retrieves a real direction.
         * 
         * @return 
         *      Real direction.
         */
        public Direction get() {
            
            return isVerticalReal ? vertical : horizontal;
        }
        
        /**
         * Updates instance by new value of real direction.
         * 
         * @param direction
         *      New value of real direction.
         */
        public void update(Direction direction) {
            
            if (direction == null)
                return;
            
            if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                
                horizontal = direction;
                isVerticalReal = false;
                return;
            }
            
            vertical = direction;
            isVerticalReal = true;
        }
    }
    
    public static class LevelSize {
        
        protected int width = 0;
        protected int height = 0;
        
        public LevelSize(int width, int height) {
            
            this.width = width;
            this.height = height;
        }
        
        public int getWidth() {
            
            return width;
        }
        
        public int getHeight() {
            
            return height;
        }
    }
    
    /**
     * Shows whether the level is initialized.
     */
    //protected boolean isInitialized = false;
    protected LevelState levelState = LevelState.EMPTY;
    
    /**
     * Stores level's initial state characters.
     */
    protected ArrayList<ArrayList<Character>> levelInitial = null;
    
    /**
     * Stores level's current state with empty lines and columns appended
     * to make level's size equal to {@link #maximalLevelWidth} and
     * {@link #maximalLevelHeight}.
     */
    protected ArrayList<ArrayList<Character>> level = null;
    
    /**
     * Keeps level's information.
     */
    protected HashMap<String, Object> levelInfo = null;
    
    /**
     * Keeps level's real size (width and height in level items).
     */
    protected LevelSize size = null;
    
    /**
     * Restricts level's width (horizontal items' count) and height (vertical items' count).
     */
    protected LevelSize maximalSize = new LevelSize(DEFAULT_LEVEL_WIDTH, DEFAULT_LEVEL_HEIGHT);
    
    /**
     * Keeps goals count of the level.
     */
    protected int goalsCount = 0;
    
    /**
     * Keeps boxes count of the level.
     */
    protected int boxesCount = 0;
    
    /**
     * Traces boxes count placed on the goals.
     */
    protected int boxesOnGoalsCount = 0;
    
    /**
     * Keeps current worker position's horizontal index within the range [0; {@link #getMaximalWidth()} - 1].
     */
    protected int workerX = 0;
    
    /**
     * Keeps current worker position's vertical index within the range [0; {@link #getMaximalHeight()} - 1].
     */
    protected int workerY = 0;
    
    /**
     * Represents worker's actual compound look direction.
     */
    protected WorkerDirection workerDirection = new WorkerDirection();
    
    /**
     * Traces worker's moves count.
     */
    protected int movesCount = 0;
    
    /**
     * Traces worker's pushes count.
     */
    protected int pushesCount = 0;
    
    /**
     * Keeps an information about performed moves.
     */
    protected ArrayList<MoveInformation> movesHistory = new ArrayList<MoveInformation>();
    
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
        
        // Checking whether level lines are specified
        if (levelLines == null || levelLines.size() < 1)
            return;
        
        levelInitial = new ArrayList<ArrayList<Character>>();
        int levelLineIndex = 0;
        
        int levelWidth = 0;
        int levelHeight = levelLines.size();
        
        while (levelLineIndex < levelLines.size()) {
            
            String levelLine = levelLines.get(levelLineIndex);
            
            if (levelWidth < levelLine.length())
                levelWidth = levelLine.length();
                
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
        
        // Defining level's size
        size = new LevelSize(levelWidth, levelHeight);
    }
    
    /**
     * Retrieves level's size.
     * 
     * @return
     *      Level's size.
     */
    public LevelSize getSize() {
        
        return new LevelSize(size.getWidth(), size.getHeight());
    }
    
    /**
     * Retrieves level's width.
     * 
     * @return
     *      Level's width.
     */
    public int getWidth() {
        
        return size.getWidth();
    }
    
    /**
     * Retrieves level's height.
     * 
     * @return
     *      Level's height.
     */
    public int getHeight() {
        
        return size.getHeight();
    }
    
    /**
     * Sets level's maximal size in items.
     * 
     * @param maximalWidth
     *      Level's desired maximal width in items.
     * @param maximalHeight
     *      Level's desired maximal height in items.
     * @return 
     *      {@code true} if desired width and height have been set, {@code false} otherwise.
     * @see #getMaximalWidth()
     * @see #getMaximalHeight()
     */
    public final boolean setMaximalSize(int maximalWidth, int maximalHeight) {
        
        if (maximalWidth < MINIMAL_LEVEL_WIDTH || maximalWidth > MAXIMAL_LEVEL_WIDTH ||
                maximalHeight < MINIMAL_LEVEL_HEIGHT || maximalHeight > MAXIMAL_LEVEL_HEIGHT) {
            
            return false;
        }
        
        maximalSize = new LevelSize(maximalWidth, maximalHeight);
        return true;
    }
    
    public LevelSize getMaximalSize() {
        
        return maximalSize;
    }
    
    /**
     * Retrieves currently set level's maximal width in items.
     * 
     * @return
     *      Level's maximal width in items.
     * @see #setMaximalSize(int, int) 
     */
    public int getMaximalWidth() {
        
        return maximalSize.getWidth();
    }
    
    /**
     * Retrieves currently set level's maximal height in items.
     * 
     * @return 
     *      Level's maximal height in items.
     * @see #setMaximalSize(int, int)
     */
    public int getMaximalHeight() {
        
        return maximalSize.getHeight();
    }
    
    public final boolean initialize() {
        
        return initialize(null);
    }
    
    /**
     * Completes level's initialization.
     * 
     * This method checks whether level's initial source {@link #levelInitial}
     * is valid, consists of only one worker and equal count of goals and boxes.
     * After that it adds additional empty horizontal and vertical lines to
     * center the level in a box of ({@link #getMaximalWidth()},
     * {@link #getMaximalHeight()}) size.
     * 
     * @return
     *      {@code true} if level has been successfully initialized, {@code false otherwise}.
     */
    synchronized public final boolean initialize(LevelSize maximalSize) {

        levelState = LevelState.EMPTY;
        
        goalsCount = 0;
        boxesCount = 0;
        boxesOnGoalsCount = 0;
        int workersCount = 0;
        workerX = 0;
        workerY = 0;
        movesCount = 0;
        pushesCount = 0;
        movesHistory = new ArrayList<MoveInformation>();
        
        this.maximalSize = maximalSize == null ? new LevelSize(DEFAULT_LEVEL_WIDTH, DEFAULT_LEVEL_HEIGHT) : maximalSize;
        
        if (levelInitial == null || levelInitial.isEmpty())
            return false;
        
        if (levelInitial.size() > this.maximalSize.getHeight()) {
        
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
                else if (lineCharacter.equals(LEVEL_ITEM_WORKER_ON_GOAL)) {

                    workersCount++;
                    goalsCount++;
                    workerX = lineCharacterIndex;
                    workerY = lineIndex;
                }
                else if (lineCharacter.equals(LEVEL_ITEM_GOAL)) {

                    goalsCount++;
                }
                else if(lineCharacter.equals(LEVEL_ITEM_BOX)) {

                    boxesCount++;
                }
                else if (lineCharacter.equals(LEVEL_ITEM_BOX_ON_GOAL)) {

                    boxesCount++;
                    goalsCount++;
                    boxesOnGoalsCount++;
                }
            }

            lineIndex++;
        }

        // Checking whether level parameters are valid
        if (maxLineWidth > this.maximalSize.getWidth()) {
            
            levelState = LevelState.OUT_OF_BOUNDS;
            return false;
        }
        
        if (boxesCount != goalsCount || workersCount != 1) {
         
            levelState = LevelState.CORRUPTED;
            return false;
        }
        
        // Cloning level's instance for playing
        level = new ArrayList<ArrayList<Character>>();
        for (ArrayList<Character> levelInitialRow : levelInitial) {
            
            ArrayList<Character> levelRow = new ArrayList<Character>();
            for (Character levelInitialRowCharacter : levelInitialRow) {
         
                if (levelInitialRowCharacter.equals(LEVEL_ITEM_WORKER_ON_GOAL))
                    levelRow.add(new Character(LEVEL_ITEM_GOAL));
                else
                    levelRow.add(new Character(levelInitialRowCharacter));
            }
            level.add(levelRow);
        }        

        // Level's vertical normalization
        int leadingEmptyLinesCount = (int)(Math.floor((double)this.maximalSize.getHeight() - (double)level.size()) / 2);
        int trailingEmptyLinesCount = this.maximalSize.getHeight() - level.size() - leadingEmptyLinesCount;

        // Shifting worker's vertical location
        workerY += leadingEmptyLinesCount;

        // Adding leading empty rows
        int leadingEmptyLineIndex = 0;
        while (leadingEmptyLineIndex < leadingEmptyLinesCount) {

            ArrayList<Character> emptyLine = new ArrayList<Character>();
            for (int emptyLineCharacterIndex = 0; emptyLineCharacterIndex < this.maximalSize.getWidth(); emptyLineCharacterIndex++)
                emptyLine.add(LEVEL_ITEM_SPACE);
            level.add(0, emptyLine);
            leadingEmptyLineIndex++;
        }

        // Adding trailing empty rows
        int trailingEmptyLineIndex = 0;
        while (trailingEmptyLineIndex < trailingEmptyLinesCount) {

            ArrayList<Character> emptyLine = new ArrayList<Character>();
            for (int emptyLineCharacterIndex = 0; emptyLineCharacterIndex < this.maximalSize.getWidth(); emptyLineCharacterIndex++)
                emptyLine.add(LEVEL_ITEM_SPACE);
            level.add(emptyLine);
            trailingEmptyLineIndex++;
        }

        // Level's horizontal normalization
        int leadingEmptyCharactersCount = (int)(Math.floor((double)this.maximalSize.getWidth() - (double)maxLineWidth) / 2);

        // Shifting worker's horizontal location
        workerX += leadingEmptyCharactersCount;

        lineIndex = leadingEmptyLinesCount;
        while (lineIndex < this.maximalSize.getHeight() - trailingEmptyLinesCount) {

            ArrayList<Character> line = level.get(lineIndex);
            int emptyCharacterIndex = 0;
            while (emptyCharacterIndex < leadingEmptyCharactersCount) {

                line.add(0, LEVEL_ITEM_SPACE);
                emptyCharacterIndex++;
            }

            while (line.size() < this.maximalSize.getWidth())
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
     *      Level's line index within the range [0; {@link #getMaximalHeight()} - 1].
     * @param column
     *      Level's column index within the range [0; {@link #getMaximalWidth()} - 1].
     * @return 
     *      Character of the item.
     * @see #setItemAt(java.lang.Character, int, int)
     * @see #allowedLevelItems
     */
    synchronized public Character getItemAt(int line, int column) {

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
     *      Level's line index within the range [0; {@link #getMaximalHeight()} - 1].
     * @param column
     *      Level's column index within the range [0; {@link #getMaximalWidth()} - 1].
     * @return
     *      {@code true} if character has been set, {@code false} otherwise.
     * @see #getItemAt(int, int)
     * @see #allowedLevelItems
     */
    synchronized public boolean setItemAt(Character levelItem, int line, int column) {

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
     * Retrieves worker's compound look direction.
     * 
     * @return 
     *      Worker's compound look direction.
     */
    public WorkerDirection getWorkerDirection() {
        
        return workerDirection;
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
    synchronized public int getMovesHistoryCount() {
        
        return movesHistory.size();
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
     * @param repeatMove
     *      Shows whether method's call was produced during move's repeat.
     * @see #takeBack()
     * @see #takeBack(int)
     * @see #repeatMove()
     * @see #repeatMoves(int)
     */
    synchronized protected void addMoveToHistory(MoveInformation moveInformation, boolean repeatMove) {
        
        if (moveInformation == null || moveInformation.getType().equals(MoveType.NOTHING) ||
                moveInformation.getDirection().equals(Direction.NONE)) {

            return;
        }

        if (!repeatMove) {
            
            while (movesHistory.size() > movesCount)
                movesHistory.remove(movesHistory.size() - 1);
        }

        movesCount++;
        if (moveInformation.getType().equals(MoveType.WORKER_AND_BOX))
            pushesCount++;

        if (!repeatMove)
            movesHistory.add(moveInformation);
    }
    
    /**
     * Takes game level's position back by one move.
     * 
     * @return
     *      A number of performed moves after the take-back or {@code -1}
     *      if level is not initialized or moves' history is empty.
     * @see #takeBack(int)
     * @see #repeatMove()
     * @see #repeatMoves(int)
     * @see #addMoveToHistory(org.ezze.games.storekeeper.Level.MoveInformation, boolean)
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
     * @see #repeatMove()
     * @see #repeatMoves(int)
     * @see #addMoveToHistory(org.ezze.games.storekeeper.Level.MoveInformation, boolean)
     */
    synchronized public int takeBack(int takeBackMovesCount) {
        
        if (levelState != LevelState.PLAYABLE || takeBackMovesCount <= 0 || takeBackMovesCount > getMovesCount())
            return -1;

        int lastRemovingMoveIndex = getMovesCount() - 1;
        int firstRemovingMoveIndex = lastRemovingMoveIndex - takeBackMovesCount + 1;
        int removingMoveIndex = lastRemovingMoveIndex;
        while (removingMoveIndex >= firstRemovingMoveIndex) {

            // Retrieving information of a move to be removed
            MoveInformation moveInformation = movesHistory.get(removingMoveIndex);
            if (!moveInformation.getType().equals(MoveType.NOTHING)) {

                Direction moveDirection = moveInformation.getDirection();                                

                if (moveInformation.getType().equals(MoveType.WORKER_AND_BOX)) {

                    // Retrieving box' current coordinates
                    int boxX = workerX;
                    int boxY = workerY;
                    if (moveDirection == Direction.LEFT)
                        boxX -= 1;
                    else if (moveDirection == Direction.RIGHT)
                        boxX += 1;
                    else if (moveDirection == Direction.UP)
                        boxY -= 1;
                    else if (moveDirection == Direction.DOWN)
                        boxY += 1;

                    // Restoring previous item at box' current position
                    Character levelItem = getItemAt(boxY, boxX);
                    if (levelItem.equals(LEVEL_ITEM_BOX_ON_GOAL))
                        setItemAt(LEVEL_ITEM_GOAL, boxY, boxX);
                    else if (levelItem.equals(LEVEL_ITEM_BOX))
                        setItemAt(LEVEL_ITEM_SPACE, boxY, boxX);

                    // Retrieving box' previous coordinates (it's where the worker right now)
                    boxX = workerX;
                    boxY = workerY;

                    // Retrieving box' destination item
                    levelItem = getItemAt(boxY, boxX);
                    if (levelItem.equals(LEVEL_ITEM_GOAL))
                        setItemAt(LEVEL_ITEM_BOX_ON_GOAL, boxY, boxX);
                    else if (levelItem.equals(LEVEL_ITEM_SPACE))
                        setItemAt(LEVEL_ITEM_BOX, boxY, boxX);

                    // Decreasing pushes count
                    pushesCount--;
                }

                // Moving worker back
                if (moveDirection == Direction.LEFT)
                    workerX += 1;
                else if (moveDirection == Direction.RIGHT)
                    workerX -= 1;
                else if (moveDirection == Direction.UP)
                    workerY += 1;
                else if (moveDirection == Direction.DOWN)
                    workerY -= 1;
                
                // Restoring worker's direction
                MoveInformation previousMoveInformation = null;
                if (removingMoveIndex >= 1)
                    previousMoveInformation = movesHistory.get(removingMoveIndex - 1);
                Direction previousDirection = previousMoveInformation != null ?
                        previousMoveInformation.getDirection() : new WorkerDirection().get();
                workerDirection.update(previousDirection);
                
                movesCount--;
            }

            removingMoveIndex--;
        }

        return movesCount;
    }
    
    /**
     * Repeats one previously taken back move.
     * 
     * @return 
     *      A number of performed moves after the repeat or {@code -1}
     *      if level is not initialized or if there no moves to repeat.
     * @see #repeatMoves(int)
     * @see #takeBack()
     * @see #takeBack(int)
     * @see #addMoveToHistory(org.ezze.games.storekeeper.Level.MoveInformation, boolean) 
     */
    public int repeatMove() {
        
        return repeatMoves(1);
    }
    
    /**
     * Repeats previously taken back moves.
     * 
     * @param repeatMovesCount
     *      Count of moves to repeat.
     * @return
     *      A number of performed moves after the repeat or {@code -1}
     *      if level is not initialized or {@code repeatMovesCount}
     *      is more than count of possible moves to repeat.
     * @see #takeBack()
     * @see #takeBack(int)
     * @see #repeatMove()
     * @see #addMoveToHistory(org.ezze.games.storekeeper.Level.MoveInformation, boolean)
     */
    synchronized public int repeatMoves(int repeatMovesCount) {
        
        if (levelState != LevelState.PLAYABLE || repeatMovesCount <= 0
                || getMovesHistoryCount() - getMovesCount() < repeatMovesCount) {

            return -1;
        }

        int firstRepeatingMoveIndex = getMovesCount();
        int lastRepeatingMoveIndex = firstRepeatingMoveIndex + repeatMovesCount - 1;
        int repeatingMoveIndex = firstRepeatingMoveIndex;
        while (repeatingMoveIndex <= lastRepeatingMoveIndex) {

            // Retrieving information of a move to be repeated
            MoveInformation moveInformation = movesHistory.get(repeatingMoveIndex);
            if (!moveInformation.getType().equals(MoveType.NOTHING)) {

                int workerDeltaX = 0;
                int workerDeltaY = 0;
                Direction moveDirection = moveInformation.getDirection();
                if (moveDirection == Direction.LEFT)
                    workerDeltaX = -1;
                else if (moveDirection == Direction.RIGHT)
                    workerDeltaX = 1;
                else if (moveDirection == Direction.UP)
                    workerDeltaY = -1;
                else if (moveDirection == Direction.DOWN)
                    workerDeltaY = 1;

                move(workerDeltaX, workerDeltaY, true);
            }

            repeatingMoveIndex++;
        }

        return movesCount;
    }

    /**
     * Checks whether level is completed.
     *
     * @return
     *      {@code true} if level is completed, {@code false} otherwise
     */
    public boolean isCompleted() {

        return boxesCount == boxesOnGoalsCount;
    }
    
    /**
     * Completes worker's move with specified shifts if it's possible.
     * 
     * @param workerDeltaX
     *      Worker's horizontal shift.
     * @param workerDeltaY
     *      Worker's vertical shift.
     * @return 
     *      Completed move's information.
     * @see #move(int, int, boolean)
     */
    protected MoveInformation move(int workerDeltaX, int workerDeltaY) {
        
        return move(workerDeltaX, workerDeltaY, false);
    }

    /**
     * Completes worker's move with specified shifts if it's possible.
     * 
     * @param workerDeltaX
     *      Worker's horizontal shift.
     * @param workerDeltaY
     *      Worker's vertical shift.
     * @param repeatMove
     *      Shows whether move is being repeated from moves history and the result information
     *      is not to be added to moves history.
     * @return
     *      Completed move's information.
     * @see #move(int, int)
     */
    synchronized protected MoveInformation move(int workerDeltaX, int workerDeltaY, boolean repeatMove) {

        if (workerDeltaX == 0 && workerDeltaY == 0)
            return new MoveInformation(MoveType.NOTHING, Direction.NONE);

        // Calculating worker's destination location
        int workerDestinationX = workerX + workerDeltaX;
        int workerDestinationY = workerY + workerDeltaY;

        // Checking that worker's destination position is not a wall
        Character workerDestinationLevelItem = getItemAt(workerDestinationY, workerDestinationX);
        if (workerDestinationLevelItem.equals(LEVEL_ITEM_BRICK))
            return new MoveInformation(MoveType.NOTHING, Direction.NONE);
        
        // Defining worker's move direction
        Direction moveDirection = Direction.NONE;
        if (workerDeltaX > 0)
            moveDirection = Direction.RIGHT;
        else if (workerDeltaX < 0)
            moveDirection = Direction.LEFT;
        else if (workerDeltaY > 0)
            moveDirection = Direction.DOWN;
        else if (workerDeltaY < 0)
            moveDirection = Direction.UP;

        // Checking whether worker's destination position is a box
        if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX) || workerDestinationLevelItem.equals(LEVEL_ITEM_BOX_ON_GOAL)) {

            // Looking for possibility to move the box
            int boxDestinationX = workerDestinationX + workerDeltaX;
            int boxDestinationY = workerDestinationY + workerDeltaY;

            // Checking whether the box' destination position is not a wall or another box
            Character boxDestinationLevelItem = getItemAt(boxDestinationY, boxDestinationX);
            if (boxDestinationLevelItem.equals(LEVEL_ITEM_BRICK) || boxDestinationLevelItem.equals(LEVEL_ITEM_BOX)
                    || boxDestinationLevelItem.equals(LEVEL_ITEM_BOX_ON_GOAL))
                return new MoveInformation(MoveType.NOTHING, Direction.NONE);

            // Removing the box from old location
            if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX))
                setItemAt(LEVEL_ITEM_SPACE, workerDestinationY, workerDestinationX);
            else if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX_ON_GOAL)) {

                setItemAt(LEVEL_ITEM_GOAL, workerDestinationY, workerDestinationX);
                boxesOnGoalsCount--;
            }

            // Placing the box in new location
            if (boxDestinationLevelItem.equals(LEVEL_ITEM_SPACE))
                setItemAt(LEVEL_ITEM_BOX, boxDestinationY, boxDestinationX);
            else if (boxDestinationLevelItem.equals(LEVEL_ITEM_GOAL)) {

                setItemAt(LEVEL_ITEM_BOX_ON_GOAL, boxDestinationY, boxDestinationX);
                boxesOnGoalsCount++;
            }

            workerX = workerDestinationX;
            workerY = workerDestinationY;
            
            workerDirection.update(moveDirection);

            // Adding the move to moves' history
            MoveInformation moveInformation = new MoveInformation(MoveType.WORKER_AND_BOX, moveDirection);
            addMoveToHistory(moveInformation, repeatMove);
            

            return moveInformation;
        }

        workerX = workerDestinationX;
        workerY = workerDestinationY;
        
        workerDirection.update(moveDirection);
        
        // Adding the move to moves' history
        MoveInformation moveInformation = new MoveInformation(MoveType.WORKER, moveDirection);
        addMoveToHistory(moveInformation, repeatMove);
        
        return moveInformation;
    }
}