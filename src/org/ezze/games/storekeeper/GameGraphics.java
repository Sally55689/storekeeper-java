package org.ezze.games.storekeeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import javax.swing.JFrame;

/**
 * Abstract class required to implement game's visual representation.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.2
 */
abstract public class GameGraphics {

    public final static String IMAGE_ID_INTRODUCTION = "introduction";
    public final static String SPRITE_ID_EMPTY = "empty";
    public final static String SPRITE_ID_WORKER_LEFT = "worker_left";
    public final static String SPRITE_ID_WORKER_RIGHT = "worker_right";
    public final static String SPRITE_ID_BRICK = "brick";
    public final static String SPRITE_ID_CELL = "cell";
    public final static String SPRITE_ID_BOX = "box";
    public final static String SPRITE_ID_BOX_IN_CELL = "box_in_cell";

    /**
     * Sprite size enumeration.
     */
    public enum SpriteSize {
        
        /**
         * Means that large sprites are to be used.
         */
        LARGE,
        
        /**
         * Means that medium sprites are to be used.
         */
        MEDIUM,
        
        /**
         * Means that small sprites are to be used.
         */
        SMALL
    }
    
    /**
     * Stores instance's sprite size {@link SpriteSize}.
     */
    private SpriteSize spriteSize = SpriteSize.LARGE;
    
    EnumMap<SpriteSize, HashMap<String, ArrayList<Image>>> spriteImages =
            new EnumMap<SpriteSize, HashMap<String, ArrayList<Image>>>(SpriteSize.class);
    
    /**
     * Game graphics' default constructor.
     * 
     * @see #GameGraphics(org.ezze.games.storekeeper.GameGraphics.SpriteSize)
     */
    public GameGraphics() {
        
        this(SpriteSize.LARGE);
    }
    
    /**
     * Game graphics' constructor.
     * 
     * One have to provide desired sprite size as {@code spriteSize}.
     * Generally you will want to use large sprites so pass {@link SpriteSize#LARGE}.
     * This is automatically done by default constructor {@link #GameGraphics()}.
     *
     * In some cases you may want to change sprite size by calling
     * {@link #setSpriteSize(org.ezze.games.storekeeper.GameGraphics.SpriteSize)}.
     * Maximal possible sprite size for specified level's width and height and
     * current screen's resolution can be determined by {@link #determineOptimalSpriteSize(int, int)}.
     * 
     * @param spriteSize
     *      Desired sprite size {@link SpriteSize}
     */
    public GameGraphics(SpriteSize spriteSize) {
        
        setSpriteSize(spriteSize);
    }
    
    public final void setSpriteSize(SpriteSize spriteSize) {
        
        this.spriteSize = spriteSize;
    }
    
    public SpriteSize getSpriteSize() {
        
        return spriteSize;
    }
    
    /**
     * Determines maximal possible sprite size for specified level's
     * width and height and current screen's resolution.
     * 
     * @param levelWidth
     *      Level's maximal width.
     * @param levelHeight
     *      Levels' maximal height.
     * @return 
     *      Maximal possible sprite size or {@code null} if convenient
     *      size cannot be determined.
     */
    public SpriteSize determineOptimalSpriteSize(int levelWidth, int levelHeight) {
        
        // Checking screen resolution
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        Insets dialogInsets = new JFrame().getInsets();

        SpriteSize[] spriteSizes = SpriteSize.values();
        int spriteSizeIndex = 0;
        while (spriteSizeIndex < spriteSizes.length) {
            
            Dimension dimension = getSpriteDimension(spriteSizes[spriteSizeIndex]);
            if (screenDimension.width - dialogInsets.left - dialogInsets.right - levelWidth * dimension.width > 0
                    && screenDimension.height - dialogInsets.top - dialogInsets.bottom - levelHeight * dimension.height > 0) {
                
                return spriteSizes[spriteSizeIndex];
            }
                
            spriteSizeIndex++;
        }
            
        return null;
    }

    /**
     * Retrieves sprite's dimension in pixels for desired sprite size.
     * 
     * @param spriteDimension
     *      Desired sprite dimension type.
     * @return 
     *      Sprite dimension.
     */
    abstract public Dimension getSpriteDimension(SpriteSize spriteDimension);
    
    /**
     * Retrieves currently set sprite's dimension in pixels.
     * 
     * @return 
     *      Sprite dimension
     */
    public Dimension getSpriteDimension() {
        
        return getSpriteDimension(spriteSize);
    }
    
    /**
     * Retrieves worker's shift as part of level item per one game's loop iteration.
     * 
     * It's strongly advised return {@code value * N = 1.0}, where {@code N}
     * is action animation steps' count.
     * 
     * @return
     *      Animation step's shift
     */
    abstract public double getAnimationStepShift();
    
    /**
     * Retrieves background color of the game.
     * 
     * @return 
     *      Background color
     */
    abstract public Color getBackground();
    
    protected Image getSprite(SpriteSize spriteSize, String imageID) {
        
        return getSprite(spriteSize, imageID, 0);
    }
    
    protected Image getSprite(SpriteSize spriteSize, String imageID, int animationIndex) {
        
        if (spriteSize == null || imageID == null || animationIndex < 0)
            return null;
        
        // Checking whether sprite size group exists
        if (!spriteImages.containsKey(spriteSize))
            spriteImages.put(spriteSize, new HashMap<String, ArrayList<Image>>());
        HashMap<String, ArrayList<Image>> spriteSizeGroup = spriteImages.get(spriteSize);
        
        // Looking whether specified sprite images' group exists
        if (!spriteSizeGroup.containsKey(imageID))
            spriteSizeGroup.put(imageID, new ArrayList<Image>());
        
        // Retrieving sprite images group
        ArrayList<Image> imageSpritesGroup = spriteSizeGroup.get(imageID);
        if (animationIndex >= imageSpritesGroup.size()) {
            
            int emptyAnimationIndex = imageSpritesGroup.size();
            while (emptyAnimationIndex <= animationIndex) {
                
                imageSpritesGroup.add(emptyAnimationIndex, null);
                emptyAnimationIndex++;
            }
        }
        
        Image spriteImage = imageSpritesGroup.get(animationIndex);
        if (spriteImage != null)
            return spriteImage;
        
        spriteImage = getSpriteFromSource(spriteSize, imageID, animationIndex);
        if (spriteImage != null) {
            
            imageSpritesGroup.set(animationIndex, spriteImage);
            return spriteImage;
        }
        
        // Looking for empty sprite
        if (!spriteSizeGroup.containsKey(SPRITE_ID_EMPTY))
            spriteSizeGroup.put(SPRITE_ID_EMPTY, new ArrayList<Image>());
        ArrayList<Image> emptySpritesGroup = spriteSizeGroup.get(SPRITE_ID_EMPTY);
        if (emptySpritesGroup.isEmpty()) {
            
            emptySpritesGroup.add(new BufferedImage(getSpriteDimension().width,
                    getSpriteDimension().height, BufferedImage.TYPE_INT_ARGB));
        }
        
        return emptySpritesGroup.get(0);
    }
    
    abstract protected Image getSpriteFromSource(SpriteSize spriteSize, String imageID, int animationIndex);
    
    /**
     * Retrieves game's introduction image.
     * 
     * @return 
     *      Introduction image
     */
    public Image getIntroductionImage() {
        
        return getSprite(spriteSize, IMAGE_ID_INTRODUCTION);
    }
    
    /**
     * Retrieves worker's action sprites' count.
     * 
     * This count also includes worker's static position.
     * 
     * @return
     *      Worker's action sprites' count.
     */
    abstract public int getActionSpritesCount();
    
    /**
     * Retrieves animation sprite of the worker looking to the left.
     * 
     * @param spriteIndex
     *      Animation sprite's index in the range [0; {@link #getActionSpritesCount()} - 1]
     * @return 
     *      Worker's animation sprite
     */
    public Image getLeftActionSprite(int spriteIndex) {
        
        if (spriteIndex < 0 || spriteIndex >= getActionSpritesCount())
            return getSprite(spriteSize, SPRITE_ID_EMPTY);
        
        return getSprite(spriteSize, SPRITE_ID_WORKER_LEFT, spriteIndex);
    }
    
    /**
     * Retrieves animation sprite of the worker looking to the right.
     * 
     * @param spriteIndex
     *      Animation sprite's index in the range [0; {@link #getActionSpritesCount()} - 1]
     * @return 
     *      Worker's animation sprite
     */
    public Image getRightActionSprite(int spriteIndex) {
      
        if (spriteIndex < 0 || spriteIndex >= getActionSpritesCount())
            return getSprite(spriteSize, SPRITE_ID_EMPTY);
        
        return getSprite(spriteSize, SPRITE_ID_WORKER_RIGHT, spriteIndex);
    }
    
    /**
     * Retrieves brick's sprite.
     * 
     * @return
     *      Brick's sprite
     */
    public Image getBrickSprite() {
        
        return getSprite(spriteSize, SPRITE_ID_BRICK);
    }
    
    /**
     * Retrieves cell's sprite.
     * 
     * @return
     *      Cell's sprite
     */
    public Image getCellSprite() {
        
        return getSprite(spriteSize, SPRITE_ID_CELL);
    }
    
    /**
     * Retrieves box' sprite.
     * 
     * @return
     *      Box' sprite
     */
    public Image getBoxSprite() {
        
        return getSprite(spriteSize, SPRITE_ID_BOX);
    }
    
    /**
     * Retrieves sprite of a box in a cell.
     * 
     * @return 
     *      Box in a cell sprite
     */
    public Image getBoxInCellSprite() {
        
        return getSprite(spriteSize, SPRITE_ID_BOX_IN_CELL);
    }
}