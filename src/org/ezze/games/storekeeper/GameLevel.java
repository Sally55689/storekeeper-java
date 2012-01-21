package org.ezze.games.storekeeper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class stores an inner representation of storekeeper's level.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.2
 */
public class GameLevel {

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
    public static final int MAXIMAL_LEVEL_WIDTH = 30;
    
    /**
     * Maximal value of maximal count of level items per column.
     */
    public static final int MAXIMAL_LEVEL_HEIGHT = 30;
    
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
    
    /**
     * Represents a result of a move attempted by {@link #move(int, int)} method.
     */
    public static enum MoveType {

        /**
         * Nothing has been changed after the attempt to move.
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
     * Shows whether the level is initialized.
     */
    private boolean isInitialized = false;
    
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
     * Level's default constructor.
     * 
     * @param levelLines
     *      Lines to initialize a level from.
     * @param levelInfo 
     *      Level's information.
     * @see #GameLevel(java.util.ArrayList, java.util.HashMap, int, int)
     */
    public GameLevel(ArrayList<String> levelLines, HashMap<String, Object> levelInfo) {
        
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
     * @see #GameLevel(java.util.ArrayList, java.util.HashMap)
     */
    public GameLevel(ArrayList<String> levelLines, HashMap<String, Object> levelInfo, int maximalLevelWidth, int maximalLevelHeight) {
        
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

        isInitialized = false;
        
        cellsCount = 0;
        boxesCount = 0;
        boxesInCellsCount = 0;
        int workersCount = 0;
        workerX = 0;
        workerY = 0;
        movesCount = 0;
        
        if (levelInitial == null)
            return false;
        
        if (levelInitial.isEmpty() || levelInitial.size() > maximalLevelHeight)
            return false;

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
        if (boxesCount != cellsCount || workersCount != 1 || maxLineWidth > maximalLevelWidth)
            return false;
        
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
        
        isInitialized = true;
        return true;
    }
    
    /**
     * Shows whether level has been successfully initialized.
     * 
     * @return
     *      {@code true} if level has been initialized, {@code false} otherwise
     */
    public boolean isInitialized() {

        return isInitialized;
    }
    
    /**
     * Retrieves level's numeric identifier.
     * 
     * @return 
     *      Level's numeric identifier or {@code 0} if it's not determined.
     */
    public int getLevelID() {
        
        try {
            
            return (Integer)levelInfo.get("id");
        }
        catch (NullPointerException ex) {
            
        }
        catch (ClassCastException ex) {
            
        }
        
        return 0;
    }
    
    /**
     * Retrieves level's name.
     * 
     * @return 
     *      Level's name or empty string if it's not determined.
     */
    public String getLevelName() {
        
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
     * @see #setLevelItemAt(java.lang.Character, int, int)
     * @see #allowedLevelItems
     */
    public Character getLevelItemAt(int line, int column) {

        if (!isInitialized)
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
     * @see #getLevelItemAt(int, int)
     * @see #allowedLevelItems
     */
    public boolean setLevelItemAt(Character levelItem, int line, int column) {

        if (!isInitialized)
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
    public MoveType move(int workerDeltaX, int workerDeltaY) {

        if (workerDeltaX == 0 && workerDeltaY == 0)
            return MoveType.NOTHING;

        // Calculating worker's destination location
        int workerDestinationX = workerX + workerDeltaX;
        int workerDestinationY = workerY + workerDeltaY;

        // Checking that worker's destination position is not a wall
        Character workerDestinationLevelItem = getLevelItemAt(workerDestinationY, workerDestinationX);
        if (workerDestinationLevelItem.equals(LEVEL_ITEM_BRICK))
            return MoveType.NOTHING;

        // Checking whether worker's destination position is a box
        if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX) || workerDestinationLevelItem.equals(LEVEL_ITEM_BOX_IN_CELL)) {

            // Looking for possibility to move the box
            int boxDestinationX = workerDestinationX + workerDeltaX;
            int boxDestinationY = workerDestinationY + workerDeltaY;

            // Checking whether the box' destination position is not a wall or another box
            Character boxDestinationLevelItem = getLevelItemAt(boxDestinationY, boxDestinationX);
            if (boxDestinationLevelItem.equals(LEVEL_ITEM_BRICK) || boxDestinationLevelItem.equals(LEVEL_ITEM_BOX)
                    || boxDestinationLevelItem.equals(LEVEL_ITEM_BOX_IN_CELL))
                return MoveType.NOTHING;

            // Removing the box from old location
            if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX))
                setLevelItemAt(LEVEL_ITEM_SPACE, workerDestinationY, workerDestinationX);
            else if (workerDestinationLevelItem.equals(LEVEL_ITEM_BOX_IN_CELL)) {

                setLevelItemAt(LEVEL_ITEM_CELL, workerDestinationY, workerDestinationX);
                boxesInCellsCount--;
            }

            // Placing the box in new location
            if (boxDestinationLevelItem.equals(LEVEL_ITEM_SPACE))
                setLevelItemAt(LEVEL_ITEM_BOX, boxDestinationY, boxDestinationX);
            else if (boxDestinationLevelItem.equals(LEVEL_ITEM_CELL)) {

                setLevelItemAt(LEVEL_ITEM_BOX_IN_CELL, boxDestinationY, boxDestinationX);
                boxesInCellsCount++;
            }

            workerX = workerDestinationX;
            workerY = workerDestinationY;

            movesCount += 1;

            return MoveType.WORKER_AND_BOX;
        }

        workerX = workerDestinationX;
        workerY = workerDestinationY;

        movesCount += 1;
        
        return MoveType.WORKER;
    }
}