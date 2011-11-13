package org.ezze.games.storekeeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

/**
 * Interface required to implement game's visual representation.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public interface GameGraphics {

    /**
     * Retrieves game's loop iteration time in milliseconds.
     * 
     * @return
     *      Loop iteration time in milliseconds
     */
    public int getGameLoopIterationTime();
    
    /**
     * Retrieves worker's shift as part of level item per one game's loop iteration.
     * 
     * It's strongly advised return value * N = 1.0, where N is action animation steps' count.
     * 
     * @return
     *      Animation step's shift
     */
    public double getAnimationStepShift();
    
    /**
     * Retrieves background color of the game.
     * 
     * @return 
     *      Background color
     */
    public Color getBackground();
    
    /**
     * Retrieves game's introduction image.
     * 
     * @return 
     *      Introduction image
     */
    public Image getIntroductionImage();
    
    /**
     * Retrieves sprite's dimension in pixels.
     * 
     * @return 
     *      Sprite dimension
     */
    public Dimension getSpriteDimension();
    
    /**
     * Retrieves worker's action sprites' count.
     * 
     * This count also includes worker's static position.
     */
    public int getActionSpritesCount();
    
    /**
     * Retrieves animation sprite of the worker looking to the left.
     * 
     * @param spriteIndex
     *      Animation sprite's index in the range [0; {@link #getActionSpritesCount()} - 1]
     * @return 
     *      Worker's animation sprite
     */
    public Image getLeftActionSprite(int spriteIndex);
    
    /**
     * Retrieves animation sprite of the worker looking to the right.
     * 
     * @param spriteIndex
     *      Animation sprite's index in the range [0; {@link #getActionSpritesCount()} - 1]
     * @return 
     *      Worker's animation sprite
     */
    public Image getRightActionSprite(int spriteIndex);
    
    /**
     * Retrieves brick's sprite.
     * 
     * @return
     *      Brick's sprite
     */
    public Image getBrickSprite();
    
    /**
     * Retrieves cell's sprite.
     * 
     * @return
     *      Cell's sprite
     */
    public Image getCellSprite();
    
    /**
     * Retrieves box' sprite.
     * 
     * @return
     *      Box' sprite
     */
    public Image getBoxSprite();
    
    /**
     * Retrieves sprite of a box in a cell.
     * 
     * @return 
     *      Box in a cell sprite
     */
    public Image getBoxInCellSprite();
}