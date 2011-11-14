package org.ezze.games.storekeeper;

import java.io.File;
import java.util.HashMap;
import org.ezze.utils.io.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public class GameConfiguration {
 
    public static final String CONFIGURATION_XML_TAG_ROOT = "configuration";
    public static final String CONFIGURATION_XML_TAG_GAMEPLAY = "gameplay";
    public static final String CONFIGURATION_XML_TAG_GAME_CYCLE_TIME = "game_cycle_time";
    public static final String CONFIGURATION_XML_TAG_INTERFACE = "interface";
    public static final String CONFIGURATION_XML_TAG_LEVEL_WIDTH = "level_width";
    public static final String CONFIGURATION_XML_TAG_LEVEL_HEIGHT = "level_height";
    
    public static final String OPTION_GAME_CYCLE_TIME = String.format("%s.%s",
            CONFIGURATION_XML_TAG_GAMEPLAY, CONFIGURATION_XML_TAG_GAME_CYCLE_TIME);
    public static final Integer DEFAULT_OPTION_GAME_CYCLE_TIME = new Integer(45);
    
    public static final String OPTION_LEVEL_WIDTH = String.format("%s.%s",
            CONFIGURATION_XML_TAG_INTERFACE, CONFIGURATION_XML_TAG_LEVEL_WIDTH);
    public static final Integer DEFAULT_OPTION_LEVEL_WIDTH = GameLevel.DEFAULT_LEVEL_WIDTH;
    
    public static final String OPTION_LEVEL_HEIGHT = String.format("%s.%s",
            CONFIGURATION_XML_TAG_INTERFACE, CONFIGURATION_XML_TAG_LEVEL_HEIGHT);
    public static final Integer DEFAULT_OPTION_LEVEL_HEIGHT = GameLevel.DEFAULT_LEVEL_HEIGHT;
    
    String configurationFileName = null;
    HashMap<String, Object> optionsList = null;
   
    public GameConfiguration() {
        
        this(null);
    }
    
    public GameConfiguration(String configurationFileName) {
        
        // Creating options list instance
        optionsList = new HashMap<String, Object>();
        this.configurationFileName = configurationFileName;
        if (this.configurationFileName == null)
            return;
        
        // Checking whether specified configuration file exists
        File configurationFile = new File(configurationFileName);
        if (!configurationFile.exists() || !configurationFile.isFile())
            return;
        
        // Parsing configuration file and retrieving options
        Document xmlDocument = XMLParser.readXMLDocument(configurationFileName, true, CONFIGURATION_XML_TAG_ROOT);
        Element xmlRootElement = XMLParser.getDocumentElement(xmlDocument);
        
        Element xmlGameplayElement = XMLParser.getChildElement(xmlRootElement, CONFIGURATION_XML_TAG_GAMEPLAY);
        setOption(OPTION_GAME_CYCLE_TIME, XMLParser.getElementInteger(xmlGameplayElement, DEFAULT_OPTION_GAME_CYCLE_TIME));
    }
    
    public final void setOption(String optionName, Object optionValue) {
        
        if (optionName == null || optionValue == null)
            return;
        
        optionsList.put(optionName, optionValue);
    }
    
    public Object getOption(String optionName, Object optionDefaultValue) {
        
        if (optionName == null)
            return optionDefaultValue;
        
        if (optionsList.containsKey(optionName))
            return optionsList.get(optionName);
        
        return optionDefaultValue;
    }
}