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
    
    public static final Character LEVEL_ITEM_WORKER = '@';
    public static final Character LEVEL_ITEM_WORKER_IN_CELL = '+';
    public static final Character LEVEL_ITEM_BRICK = '#';
    public static final Character LEVEL_ITEM_CELL = '.';
    public static final Character LEVEL_ITEM_BOX = '$';
    public static final Character LEVEL_ITEM_BOX_IN_CELL = '*';
    public static final Character LEVEL_ITEM_SPACE = ' ';
    
    public static enum MoveType {

        NOTHING,
        WORKER,
        WORKER_AND_BOX
    };
    
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
    
    private boolean isInitialized = false;
    private ArrayList<ArrayList<Character>> levelInitial = null;
    private ArrayList<ArrayList<Character>> level = null;
    private HashMap<String, Object> levelInfo = null;
    private int maximalLevelWidth = DEFAULT_LEVEL_WIDTH;
    private int maximalLevelHeight = DEFAULT_LEVEL_HEIGHT;
    private int cellsCount = 0;
    private int boxesCount = 0;
    private int boxesInCellsCount = 0;
    private int workerX = 0;
    private int workerY = 0;
    private int movesCount = 0;
    
    public GameLevel(ArrayList<String> levelLines, HashMap<String, Object> levelInfo) {
        
        this(levelLines, levelInfo, DEFAULT_LEVEL_WIDTH, DEFAULT_LEVEL_HEIGHT);
    }
    
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
    
    public final boolean setMaximalLevelWidth(int maximalLevelWidth) {
        
        if (maximalLevelWidth < MINIMAL_LEVEL_WIDTH || maximalLevelWidth > MAXIMAL_LEVEL_WIDTH)
            return false;
        this.maximalLevelWidth = maximalLevelWidth;
        return true;
    }
    
    public final boolean setMaximalLevelHeight(int maximalLevelHeight) {
        
        if (maximalLevelHeight < MINIMAL_LEVEL_HEIGHT || maximalLevelHeight > MAXIMAL_LEVEL_HEIGHT)
            return false;
        this.maximalLevelHeight = maximalLevelHeight;
        return true;
    }
    
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