package org.ezze.games.storekeeper;

import java.io.File;
import java.util.HashMap;
import org.ezze.utils.io.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class reads and writes game's configuration file
 * and provides an interface to manipulate the options
 * from the game.
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
    public static final String CONFIGURATION_XML_TAG_SPRITE_DIMENSION = "sprite_dimension";
    
    public static final String OPTION_GAME_CYCLE_TIME = String.format("%s.%s",
            CONFIGURATION_XML_TAG_GAMEPLAY, CONFIGURATION_XML_TAG_GAME_CYCLE_TIME);
    public static final Integer DEFAULT_OPTION_GAME_CYCLE_TIME = new Integer(45);
    public static final Integer MIN_OPTION_GAME_CYCLE_TIME = new Integer(20);
    public static final Integer MAX_OPTION_GAME_CYCLE_TIME = new Integer(80);
    
    public static final String OPTION_LEVEL_WIDTH = String.format("%s.%s",
            CONFIGURATION_XML_TAG_INTERFACE, CONFIGURATION_XML_TAG_LEVEL_WIDTH);
    public static final Integer DEFAULT_OPTION_LEVEL_WIDTH = GameLevel.DEFAULT_LEVEL_WIDTH;
    public static final Integer MIN_OPTION_LEVEL_WIDTH = new Integer(20);
    public static final Integer MAX_OPTION_LEVEL_WIDTH = new Integer(30);
    
    public static final String OPTION_LEVEL_HEIGHT = String.format("%s.%s",
            CONFIGURATION_XML_TAG_INTERFACE, CONFIGURATION_XML_TAG_LEVEL_HEIGHT);
    public static final Integer DEFAULT_OPTION_LEVEL_HEIGHT = GameLevel.DEFAULT_LEVEL_HEIGHT;
    public static final Integer MIN_OPTION_LEVEL_HEIGHT = new Integer(20);
    public static final Integer MAX_OPTION_LEVEL_HEIGHT = new Integer(30);
    
    /**
     * Stores a path to configuration XML file.
     */
    String configurationFileName = null;
    
    /**
     * Keeps a list of options' values.
     */
    HashMap<String, Object> optionsList = null;
    
    /**
     * Creates game configuration instance reading existing
     * configuration file specified by {@code configurationFileName}
     * if it exists, or initializes options list by default values otherwise.
     * 
     * @param configurationFileName 
     *      A path to configuration XML file name
     */
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
        
        Element xmlGameCycleTimeElement = XMLParser.getChildElement(xmlGameplayElement, CONFIGURATION_XML_TAG_GAME_CYCLE_TIME);        
        setOption(OPTION_GAME_CYCLE_TIME, adjustOptionByRange(XMLParser.getElementInteger(xmlGameCycleTimeElement,
                DEFAULT_OPTION_GAME_CYCLE_TIME), MIN_OPTION_GAME_CYCLE_TIME, MAX_OPTION_GAME_CYCLE_TIME));
        
        Element xmlInterfaceElement = XMLParser.getChildElement(xmlRootElement, CONFIGURATION_XML_TAG_INTERFACE);
        
        Element xmlLevelWidthElement = XMLParser.getChildElement(xmlInterfaceElement, CONFIGURATION_XML_TAG_LEVEL_WIDTH);
        setOption(OPTION_LEVEL_WIDTH, adjustOptionByRange(XMLParser.getElementInteger(xmlLevelWidthElement,
                DEFAULT_OPTION_LEVEL_WIDTH), MIN_OPTION_LEVEL_WIDTH, MAX_OPTION_LEVEL_WIDTH));
        
        Element xmlLevelHeightElement = XMLParser.getChildElement(xmlInterfaceElement, CONFIGURATION_XML_TAG_LEVEL_HEIGHT);
        setOption(OPTION_LEVEL_HEIGHT, adjustOptionByRange(XMLParser.getElementInteger(xmlLevelHeightElement,
                DEFAULT_OPTION_LEVEL_HEIGHT), MIN_OPTION_LEVEL_HEIGHT, MAX_OPTION_LEVEL_HEIGHT));
    }
    
    /**
     * Sets option's value.
     * 
     * @param optionName
     *      Option's name
     * @param optionValue
     *      Option's value
     */
    public final void setOption(String optionName, Object optionValue) {
        
        if (optionName == null || optionValue == null)
            return;
      
        optionsList.put(optionName, optionValue);
    }
    
    /**
     * Retrieves currently set option's value.
     * 
     * @param optionName
     *      Option's name
     * @param optionDefaultValue
     *      Option's default value to return in the case of option is not set
     * @return 
     *      Option's current value
     */
    public Object getOption(String optionName, Object optionDefaultValue) {
        
        if (optionName == null)
            return optionDefaultValue;
        
        if (optionsList.containsKey(optionName))
            return optionsList.get(optionName);
        
        return optionDefaultValue;
    }
    
    /**
     * Provides specified integer value to be within specified number range.
     * 
     * @param optionValue
     *      Integer value to correct
     * @param minValue
     *      Minimal possible value
     * @param maxValue
     *      Maximal possible value
     * @return 
     *      Corrected integer value laying within range [{@code minValue}; {@code maxValue}]
     */
    public static Integer adjustOptionByRange(Integer optionValue, int minValue, int maxValue) {
        
        if (optionValue == null)
            return null;
        
        if (optionValue < minValue)
            return minValue;
        
        if (optionValue > maxValue)
            return maxValue;
        
        return new Integer(optionValue);
    }
    
    /**
     * Saves or updates configuration file by currently set options' values.
     * 
     * @return 
     *      {@code true} on success, {@code false} otherwise
     */
    public boolean save() {
        
        Document xmlDocument = XMLParser.readXMLDocument(configurationFileName, true, CONFIGURATION_XML_TAG_ROOT);
        Element xmlRootElement = XMLParser.getDocumentElement(xmlDocument);
        
        Element xmlGameplayElement = XMLParser.getChildElement(xmlRootElement, CONFIGURATION_XML_TAG_GAMEPLAY);
        if (xmlGameplayElement == null)
            xmlGameplayElement = XMLParser.addChildElement(xmlDocument, xmlRootElement, CONFIGURATION_XML_TAG_GAMEPLAY);
        
        Element xmlGameCycleTimeElement = XMLParser.getChildElement(xmlGameplayElement, CONFIGURATION_XML_TAG_GAME_CYCLE_TIME);
        if (xmlGameCycleTimeElement == null)
            xmlGameCycleTimeElement = XMLParser.addChildElement(xmlDocument, xmlGameplayElement, CONFIGURATION_XML_TAG_GAME_CYCLE_TIME);
        XMLParser.setElementText(xmlGameCycleTimeElement, getOption(OPTION_GAME_CYCLE_TIME, DEFAULT_OPTION_GAME_CYCLE_TIME));
        
        Element xmlInterfaceElement = XMLParser.getChildElement(xmlRootElement, CONFIGURATION_XML_TAG_INTERFACE);
        if (xmlInterfaceElement == null)
            xmlInterfaceElement = XMLParser.addChildElement(xmlDocument, xmlRootElement, CONFIGURATION_XML_TAG_INTERFACE);
        
        Element xmlLevelWidthElement = XMLParser.getChildElement(xmlInterfaceElement, CONFIGURATION_XML_TAG_LEVEL_WIDTH);
        if (xmlLevelWidthElement == null)
            xmlLevelWidthElement = XMLParser.addChildElement(xmlDocument, xmlInterfaceElement, CONFIGURATION_XML_TAG_LEVEL_WIDTH);
        XMLParser.setElementText(xmlLevelWidthElement, getOption(OPTION_LEVEL_WIDTH, DEFAULT_OPTION_LEVEL_WIDTH));
        
        Element xmlLevelHeightElement = XMLParser.getChildElement(xmlInterfaceElement, CONFIGURATION_XML_TAG_LEVEL_HEIGHT);
        if (xmlLevelHeightElement == null)
            xmlLevelHeightElement = XMLParser.addChildElement(xmlDocument, xmlInterfaceElement, CONFIGURATION_XML_TAG_LEVEL_HEIGHT);
        XMLParser.setElementText(xmlLevelHeightElement, getOption(OPTION_LEVEL_HEIGHT, DEFAULT_OPTION_LEVEL_HEIGHT));
        
        return XMLParser.writeXMLDocument(xmlDocument, configurationFileName);
    }
}