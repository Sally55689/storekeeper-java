package org.ezze.games.storekeeper.desktop;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.ezze.games.storekeeper.Game;
import org.ezze.games.storekeeper.GameGraphics;
import org.ezze.games.storekeeper.Level.Direction;
import org.ezze.games.storekeeper.Level.WorkerDirection;

/**
 * Represents default game graphics implementation.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.3
 */
public class DesktopGameGraphics extends GameGraphics {
    
    @Override
    public Dimension getSpriteDimension(SpriteSize spriteDimension) {

        if (spriteDimension == SpriteSize.LARGE)
            return new Dimension(32, 32);
        else if (spriteDimension == SpriteSize.MEDIUM)
            return new Dimension(24, 24);
        else if (spriteDimension == SpriteSize.SMALL)
            return new Dimension(16,16);
        return null;
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
    public final int getActionSpritesCount(WorkerDirection direction) {
        
        return 9;
    }
    
    @Override
    public Image getActionSprite(WorkerDirection direction, int spriteIndex) {
        
        Direction horizontalDirection = direction.getHorizontal();
        Direction verticalDirection = direction.getVertical();
        WorkerDirection displayDirection = new WorkerDirection(horizontalDirection, verticalDirection, false);
        return super.getActionSprite(displayDirection, spriteIndex);
    }
    
    @Override
    protected Image getSpriteFromSource(SpriteSize spriteDimension, String imageID, int animationIndex) {

        if (spriteDimension == null || imageID == null)
            return null;
        
        String dimensionSubdirectoryName = "";
        if (spriteDimension == SpriteSize.LARGE)
            dimensionSubdirectoryName = "32x32";
        else if (spriteDimension == SpriteSize.MEDIUM)
            dimensionSubdirectoryName = "24x24";
        else if (spriteDimension == SpriteSize.SMALL)
            dimensionSubdirectoryName = "16x16";
        
        String imageFileName = "";
        if (imageID.equals(GameGraphics.IMAGE_ID_INTRODUCTION))
            imageFileName = "intro";
        else if (imageID.equals(GameGraphics.SPRITE_ID_WORKER_LEFT))
            imageFileName = String.format("gripe_left_%02d", animationIndex);
        else if (imageID.equals(GameGraphics.SPRITE_ID_WORKER_RIGHT))
            imageFileName = String.format("gripe_right_%02d", animationIndex);
        else
            imageFileName = imageID;
        
        String resourcePathToImage = String.format("/%s/resources/%s/%s.png",
                Game.class.getPackage().getName().replace('.', '/'), dimensionSubdirectoryName, imageFileName);
        URL imageURL = DesktopGameGraphics.class.getResource(resourcePathToImage);
        if (imageURL == null)
            return null;
        
        try {
            
            BufferedImage bufferedImage = ImageIO.read(imageURL);
            bufferedImage = toCompatibleImage(bufferedImage);
            return bufferedImage;
        }
        catch (IOException ex) {
        
            return null;
        }
    }
    
    protected BufferedImage toCompatibleImage(BufferedImage image) {
        
        // Obtain the current system graphical settings
        GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();

        // If image is already compatible and optimized for current system 
        // settings, simply return it
        if (image.getColorModel().equals(graphicsConfiguration.getColorModel()))
            return image;

        // Image is not optimized, so create a new image that is
        BufferedImage newImage = graphicsConfiguration.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());

        // Getting the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D)newImage.getGraphics();

        // Actually drawing the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // Return the new optimized image
        return newImage; 
    }
}