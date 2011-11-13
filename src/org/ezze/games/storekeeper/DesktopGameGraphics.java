package org.ezze.games.storekeeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.SpinnerDateModel;

/**
 * Represents default game graphics implementation.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public class DesktopGameGraphics implements GameGraphics {
    
    /**
     * Sprite dimension enumeration.
     */
    public enum SpriteDimension {
        
        /**
         * Means that 32x32 sprites are to be used.
         */
        DIMENSION_32X32,
        
        /**
         * Means that 16x16 sprites are to be used.
         */
        DIMENSION_16X16
    }
    
    /**
     * Stores instance's sprite dimension {@link SpriteDimension}.
     */
    SpriteDimension spriteDimension = SpriteDimension.DIMENSION_32X32;
    
    /**
     * A reference to game's introduction picture.
     */
    Image introductionImage = null;
    
    /**
     * A reference to looking to the left worker's sprites.
     */
    ArrayList<Image> workerLeftSprites = null;
    
    /**
     * A reference to looking to the right worker's sprites.
     */
    ArrayList<Image> workerRightSprites = null;
    
    /**
     * A reference to brick sprite.
     */
    Image brickSprite = null;
    
    /**
     * A reference to cell sprite.
     */
    Image cellSprite = null;
    
    /**
     * A reference to box sprite.
     */
    Image boxSprite = null;
    
    /**
     * A reference to box in cell sprite.
     */
    Image boxInCellSprite = null;
    
    /**
     * Game graphics' default constructor.
     * 
     * Creates an instance providing 32x32 sprites.
     * 
     * @see #DesktopGameGraphics(org.ezze.games.storekeeper.DesktopGameGraphics.SpriteDimension)
     */
    public DesktopGameGraphics() {
        
        this(SpriteDimension.DIMENSION_32X32);
    }
    
    /**
     * Game graphics' constructor.
     * 
     * One have to provide desired sprite dimension as {@code spriteDimension}.
     * Generally you will want to use 32x32 sprites so pass {@link SpriteDimension#DIMENSION_32X32}.
     * This automatically is done by default constructor {@link #DesktopGameGraphics()}.
     * But in some cases you may need to create an instance providing 16x16 sprites
     * and then pass {@link SpriteDimension#DIMENSION_16X16} (e.g. for small screen resolution).
     * 
     * @param spriteDimension
     *      Desired sprite dimension {@link SpriteDimension}
     */
    public DesktopGameGraphics(SpriteDimension spriteDimension) {
        
        this.spriteDimension = spriteDimension;
        String dimensionDirectoryName = "32x32";
        if (spriteDimension == SpriteDimension.DIMENSION_16X16)
            dimensionDirectoryName = "16x16";
        
        introductionImage = getImage(String.format("%s/intro", dimensionDirectoryName));
        workerLeftSprites = new ArrayList<Image>();
        workerRightSprites = new ArrayList<Image>();
        
        for (int actionSpriteIndex = 0; actionSpriteIndex < getActionSpritesCount(); actionSpriteIndex++) {
            
            workerLeftSprites.add(getImage(String.format("%s/gripe_left_%02d", dimensionDirectoryName, actionSpriteIndex)));
            workerRightSprites.add(getImage(String.format("%s/gripe_right_%02d", dimensionDirectoryName, actionSpriteIndex)));
        }
        brickSprite = getImage(String.format("%s/brick", dimensionDirectoryName));
        cellSprite = getImage(String.format("%s/cell", dimensionDirectoryName));
        boxSprite = getImage(String.format("%s/box", dimensionDirectoryName));
        boxInCellSprite = getImage(String.format("%s/box_in_cell", dimensionDirectoryName));
    }
    
    /**
     * Retrieves png sprite from resources by its name.
     * 
     * @param imageID
     *      Sprite's identifier used as png file name
     * @return 
     *      Sprite image
     */
    private Image getImage(String imageID) {

        if (imageID == null)
            return null;
        
        String resourcePathToImage = String.format("/%s/resources/%s.png",
                DesktopGameGraphics.class.getPackage().getName().replace('.', '/'), imageID);
        URL imageURL = DesktopGameGraphics.class.getResource(resourcePathToImage);
        if (imageURL == null)
            return null;
        
        ImageIcon imageIcon = new ImageIcon(imageURL);
        if (imageIcon == null)
            return null;
        
        return imageIcon.getImage();
    }
    
    @Override
    public int getGameLoopIterationTime() {
        
        return 45;
    }
    
    @Override
    public double getAnimationStepShift() {
    
        return 0.1;
    }
    
    @Override
    public Color getBackground() {
        
        return Color.BLACK;
    }
    
    @Override
    public Image getIntroductionImage() {
        
        return introductionImage;
    }
    
    @Override
    public Dimension getSpriteDimension() {
        
        if (spriteDimension == SpriteDimension.DIMENSION_32X32)
            return new Dimension(32, 32);
        else if (spriteDimension == SpriteDimension.DIMENSION_16X16)
            return new Dimension(16, 16);
        return null;
    }

    @Override
    public final int getActionSpritesCount() {
        
        return 9;
    }

    @Override
    public Image getLeftActionSprite(int spriteIndex) {
        
        if (spriteIndex < 0 || spriteIndex >= getActionSpritesCount() || spriteIndex >= workerLeftSprites.size())
            return null;
        
        return workerLeftSprites.get(spriteIndex);
    }

    @Override
    public Image getRightActionSprite(int spriteIndex) {
        
        if (spriteIndex < 0 || spriteIndex >= getActionSpritesCount() || spriteIndex >= workerRightSprites.size())
            return null;
        
        return workerRightSprites.get(spriteIndex);
    }

    @Override
    public Image getBrickSprite() {
        
        return brickSprite;
    }

    @Override
    public Image getCellSprite() {
        
        return cellSprite;
    }

    @Override
    public Image getBoxSprite() {
        
        return boxSprite;
    }

    @Override
    public Image getBoxInCellSprite() {
        
        return boxInCellSprite;
    }
}