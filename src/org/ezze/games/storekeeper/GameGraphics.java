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
import org.ezze.games.storekeeper.Level.Direction;
import org.ezze.games.storekeeper.Level.WorkerDirection;

/**
 * Abstract class required to implement game's visual representation.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.3
 */
abstract public class GameGraphics {

    /**
     * String identifier for introduction image.
     */
    public final static String IMAGE_ID_INTRODUCTION = "introduction";
    
    /**
     * String identifier for empty sprite.
     */
    public final static String SPRITE_ID_EMPTY = "empty";
    
    /**
     * String identifier for left-oriented worker sprite.
     */
    public final static String SPRITE_ID_WORKER_LEFT = "worker_left";
    
    /**
     * String identifier for right-oriented worker sprite.
     */
    public final static String SPRITE_ID_WORKER_RIGHT = "worker_right";
    
    /**
     * String identifier for up-oriented worker sprite.
     */
    public final static String SPRITE_ID_WORKER_UP = "worker_up";
    
    /**
     * String identifier for down-oriented worker sprite.
     */
    public final static String SPRITE_ID_WORKER_DOWN = "worker_down";
    
    /**
     * String identifier for brick sprite.
     */
    public final static String SPRITE_ID_BRICK = "brick";
    
    /**
     * String identifier for cell sprite.
     */
    public final static String SPRITE_ID_CELL = "cell";
    
    /**
     * String identifier for box sprite.
     */
    public final static String SPRITE_ID_BOX = "box";
    
    /**
     * String identifier for box in cell sprite.
     */
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
    
    /**
     * Sets sprite's size.
     * 
     * @param spriteSize
     *      Desired sprite's size.
     * @see #determineOptimalSpriteSize(int, int)
     * @see #getSpriteSize()
     */
    public final void setSpriteSize(SpriteSize spriteSize) {
        
        this.spriteSize = spriteSize;
    }
    
    /**
     * Retrieves currently set sprite's size.
     * 
     * @return
     *      Currently set sprite's size.
     * @see #getSpriteDimension()
     * @see #getSpriteDimension(org.ezze.games.storekeeper.GameGraphics.SpriteSize)
     * @see #setSpriteSize(org.ezze.games.storekeeper.GameGraphics.SpriteSize)
     * @see #determineOptimalSpriteSize(int, int)
     */
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
     * @see #setSpriteSize(org.ezze.games.storekeeper.GameGraphics.SpriteSize)
     * @see #getSpriteSize()
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
     *      Sprite dimension.
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
    
    /**
     * Retrieves static sprite image by its size and string identifier.
     * 
     * @param spriteSize
     *      Desired sprite's size.
     * @param imageID
     *      Desired sprite's string identifier.
     * @return
     *      Desired sprite image or empty image if desired image is not found.
     * @see #getSprite(org.ezze.games.storekeeper.GameGraphics.SpriteSize, java.lang.String, int)
     * @see #getSpriteFromSource(org.ezze.games.storekeeper.GameGraphics.SpriteSize, java.lang.String, int)
     */
    protected Image getSprite(SpriteSize spriteSize, String imageID) {
        
        return getSprite(spriteSize, imageID, 0);
    }
    
    /**
     * Retrieves static or animated sprite image by its size and string identifier.
     * 
     * {@link #getSpriteFromSource(org.ezze.games.storekeeper.GameGraphics.SpriteSize, java.lang.String, int)}
     * method is used to retrieve a sprite. After the sprite will has been retrieved
     * it will be stored in {@link #spriteImages} and will be retrieved from there
     * at any further time.
     * 
     * @param spriteSize
     *      Desired sprite's size.
     * @param imageID
     *      Desired sprite's string identifier, must be one of the following values:
     *      <ul>
     *      <li>{@link #IMAGE_ID_INTRODUCTION} for introduction image;</li>
     *      <li>{@link #SPRITE_ID_WORKER_LEFT} for left-oriented worker sprite;</li>
     *      <li>{@link #SPRITE_ID_WORKER_RIGHT} for right-oriented worker sprite;</li>
     *      <li>{@link #SPRITE_ID_WORKER_UP} for up-oriented worker sprite;</li>
     *      <li>{@link #SPRITE_ID_WORKER_DOWN} for down-oriented worker sprite;</li> 
     *      <li>{@link #SPRITE_ID_BRICK} for brick sprite;</li>
     *      <li>{@link #SPRITE_ID_CELL} for cell sprite;</li>
     *      <li>{@link #SPRITE_ID_BOX} for box sprite;</li>
     *      <li>{@link #SPRITE_ID_BOX_IN_CELL} for box in cell sprite;</li>
     *      <li>{@link #SPRITE_ID_EMPTY} for empty sprite.</li>
     *      </ul>
     * @param animationIndex
     *      Animation position for animated sprite or {@code 0} for static sprite.
     * @return
     *      Desired sprite image or empty image if desired image is not found.
     * @see #getSprite(org.ezze.games.storekeeper.GameGraphics.SpriteSize, java.lang.String)
     * @see #getSpriteFromSource(org.ezze.games.storekeeper.GameGraphics.SpriteSize, java.lang.String, int)
     */
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
    
    /**
     * Retrieves required sprite image from resources.
     * 
     * This method must be implemented in inherited graphics class.
     * 
     * @param spriteSize
     *      Desired sprite's size.
     * @param imageID
     *      Desired sprite's string identifier, must be one of the following values:
     *      <ul>
     *      <li>{@link #IMAGE_ID_INTRODUCTION} for introduction image;</li>
     *      <li>{@link #SPRITE_ID_WORKER_LEFT} for left-oriented worker sprite;</li>
     *      <li>{@link #SPRITE_ID_WORKER_RIGHT} for right-oriented worker sprite;</li>
     *      <li>{@link #SPRITE_ID_BRICK} for brick sprite;</li>
     *      <li>{@link #SPRITE_ID_CELL} for cell sprite;</li>
     *      <li>{@link #SPRITE_ID_BOX} for box sprite;</li>
     *      <li>{@link #SPRITE_ID_BOX_IN_CELL} for box in cell sprite;</li>
     *      <li>{@link #SPRITE_ID_EMPTY} for empty sprite.</li>
     *      </ul>
     * @param animationIndex
     *      Animation position for animated sprite or {@code 0} for static sprite.
     * @return 
     *      Desired sprite image.
     * @see #getSprite(org.ezze.games.storekeeper.GameGraphics.SpriteSize, java.lang.String)
     * @see #getSprite(org.ezze.games.storekeeper.GameGraphics.SpriteSize, java.lang.String, int)
     */
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
     * Retrieves worker's action sprites' count for specified direction.
     * 
     * @return
     *      Worker's action sprites' count.
     */
    abstract public int getActionSpritesCount(WorkerDirection direction);
    
    public Image getActionSprite(WorkerDirection direction, int spriteIndex) {
        
        int actionSpritesCount = getActionSpritesCount(direction);
        if (spriteIndex < 0 || spriteIndex >= actionSpritesCount)
            return getSprite(spriteSize, SPRITE_ID_EMPTY);
        
        String spriteID = null;
        if (direction.get() == Direction.LEFT)
            spriteID = SPRITE_ID_WORKER_LEFT;
        else if (direction.get() == Direction.RIGHT)
            spriteID = SPRITE_ID_WORKER_RIGHT;
        else if (direction.get() == Direction.UP)
            spriteID = SPRITE_ID_WORKER_UP;
        else if (direction.get() == Direction.DOWN)
            spriteID = SPRITE_ID_WORKER_DOWN;
        
        return getSprite(spriteSize, spriteID, spriteIndex);
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