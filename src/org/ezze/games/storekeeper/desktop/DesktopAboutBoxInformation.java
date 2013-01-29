package org.ezze.games.storekeeper.desktop;

import java.awt.Image;
import java.net.URL;
import java.util.Properties;
import javax.swing.ImageIcon;
import org.ezze.games.storekeeper.Game;
import org.ezze.utils.ui.aboutbox.AboutBoxInformation;

/**
 * This class provides the information for game's about box.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.2
 */
public class DesktopAboutBoxInformation extends AboutBoxInformation {

    /**
     * Application properties instance.
     */
    Properties applicationProperties = null;
    
    /**
     * Reference to application's properties.
     * 
     * @param applicationProperties
     *      Application properties instance
     */
    public DesktopAboutBoxInformation(Properties applicationProperties) {
       
        this.applicationProperties = applicationProperties;
    }

    @Override
    public int getHorizontalMargin() {

        return 8;
    }

    @Override
    public int getVerticalMargin() {

        return 8;
    }

    @Override
    public int getInformationAreaWidth() {

        return 250;
    }

    @Override
    public int getInformationLinesGap() {

        return 10;
    }
    
    @Override
    public Image getApplicationImage() {

        String resourcePathToImage = String.format("/%s/resources/about.png",
                Game.class.getPackage().getName().replace('.', '/'));
        URL imageURL = DesktopGameGraphics.class.getResource(resourcePathToImage);
        if (imageURL == null)
            return null;

        ImageIcon imageIcon = new ImageIcon(imageURL);
        if (imageIcon == null)
            return null;

        return imageIcon.getImage();
    }
    
    @Override
    public String getApplicationVendor() {

        try {
            
            String vendor = applicationProperties.getProperty("application.vendor");
            if (vendor != null)
                return vendor;
        }
        catch (NullPointerException ex) {
            
        }
        
        return "Ezze";
    }

    @Override
    public String getApplicationName() {

        try {
            
            String name = applicationProperties.getProperty("application.name");
            if (name != null)
                return name;
        }
        catch (NullPointerException ex) {
            
        }
        
        return "Storekeeper";
    }

    @Override
    public String getApplicationVersion() {
        
        try {
            
            String version = applicationProperties.getProperty("application.version");
            if (version != null)
                return version;
        }
        catch (NullPointerException ex) {
            
        }
        
        return "0.0.0";
    }

    @Override
    public String getApplicationDescription() {

        return "This is an implementation of classic Sokoban game dedicated to my beloved wife. Hope you will enjoy this one.";
    }
    
    @Override
    public String getCloseButtonText() {
        
        return "Close";
    }
}