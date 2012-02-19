package org.ezze.games.storekeeper.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.ezze.games.storekeeper.Game;
import org.ezze.games.storekeeper.Configuration;

/**
 * Options dialog for desktop game.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.2
 */
public class DesktopOptionsDialog extends JDialog {
    
    protected final static int DIALOG_WIDTH = 400;
    
    protected final static int PADDING_HORIZONTAL = 10;
    protected final static int PADDING_VERTICAL = 10;
    
    protected final static int GROUP_GAP = 8;
    
    protected final static int OPTION_PADDING_HORIZONTAL = 8;
    protected final static int OPTION_PADDING_VERTICAL = 8;
    protected final static int OPTION_GAP = 10;
    
    protected final static int LABEL_WIDTH = 120;
 
    protected final static int BUTTON_WIDTH = 80;
    protected final static int BUTTON_GAP = 4;
    
    protected final static String OPTION_VALUE_TEXT_OPTIMAL = "Optimal";
    protected final static String OPTION_VALUE_TEXT_LARGE = "Large";
    protected final static String OPTION_VALUE_TEXT_MEDIUM = "Medium";
    protected final static String OPTION_VALUE_TEXT_SMALL = "Small";
    
    /**
     * A reference to desktop game's instance.
     */
    protected DesktopGame desktopGame = null;
    
    /**
     * Slider to manipulate the game speed.
     */
    protected JSlider gameCycleTimeSlider = null;
    
    /**
     * Text field to edit level's maximal width.
     */
    protected JTextField levelWidthTextField = null;
    
    /**
     * Text field to edit level's maximal height.
     */
    protected JTextField levelHeightTextField = null;
    
    /**
     * Combobox to select sprite's size.
     */
    protected JComboBox spriteSizeComboBox = null;
    
    /**
     * Shows whether game window's rebuild is required after
     * options' changes have been applied.
     */
    protected boolean isGameWindowRebuildRequired = false;
    
    /**
     * Combobox' option class used to associate its value with a key.
     */
    protected class ComboBoxOption {
        
        /**
         * Option's key.
         */
        protected String optionKey = null;
        
        /**
         * Option's value.
         */
        protected Object optionValue = null;
        
        /**
         * Option's constructor.
         * 
         * @param optionKey
         *      Desired key to associate with {@code optionValue}.
         * @param optionValue 
         *      Option's value.
         */
        public ComboBoxOption(String optionKey, Object optionValue) {
            
            this.optionKey = optionKey;
            this.optionValue = optionValue;
        }
        
        /**
         * Retrieves a key associated with option's value.
         * 
         * @return 
         *      Option's key.
         */
        public String getKey() {
            
            return optionKey;
        }
        
        /**
         * Retrieves option's value.
         * 
         * @return
         *      Option's value.
         */
        public Object getValue() {
            
            return optionValue;
        }
        
        @Override
        public String toString() {
            
            return optionValue != null ? optionValue.toString() : "";
        }
    }
    
    /**
     * Options' dialog constructor.
     * 
     * @param desktopGame
     *      A reference to desktop game.
     * @param parentFrame
     *      A reference to desktop game's main window.
     */
    public DesktopOptionsDialog(DesktopGame desktopGame, Frame parentFrame) {
        
        // Creating modal dialog
        super(parentFrame, true);
        
        // Saving a reference to game instance
        this.desktopGame = desktopGame;
        Game game = desktopGame.getGameInstance();
        Configuration gameConfiguration = game.getGameConfiguration();
        
        // Setting window closing handler
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
           
            @Override
            public void windowClosing(WindowEvent e) {
                
                onCloseDialog();
            }
        });
        
        // Setting dialog's title
        setTitle("Options");
        
        // Retrieving a reference to dialog's content pane
        Container contentPane = getContentPane();
        
        // Applying layout manager
        SpringLayout contentLayout = new SpringLayout();
        contentPane.setLayout(contentLayout);
        
        // Creating border instance for group panels
        final Border groupPanelBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        
        // Defining baselines for different components
        JLabel baselineLabel = new JLabel(" ");
        JTextField baselineTextField = new JTextField(" ");
        JComboBox baselineComboBox = new JComboBox();
        int labelBaseline = baselineLabel.getBaseline(0, baselineLabel.getPreferredSize().height);
        int textFieldBaseline = baselineTextField.getBaseline(0, baselineTextField.getPreferredSize().height);
        int comboBoxBaseline = baselineComboBox.getBaseline(0, baselineComboBox.getPreferredSize().height);
        
        // Creating gameplay group
        JPanel gameplayGroupPanel = new JPanel();
        SpringLayout gameplayGroupLayout = new SpringLayout();
        gameplayGroupPanel.setLayout(gameplayGroupLayout);
        gameplayGroupPanel.setBorder(new TitledBorder(groupPanelBorder, "Gameplay"));
        
        // Creating game cycle time's label and slider
        JLabel gameCycleTimeLabel = new JLabel("Game Speed");
        int gameCycleTimeValue = (Integer)gameConfiguration.getOption(Configuration.OPTION_GAME_CYCLE_TIME,
                Configuration.DEFAULT_OPTION_GAME_CYCLE_TIME);
        int gameCycleTimeSliderInitialValue = Configuration.MIN_OPTION_GAME_CYCLE_TIME +
                (Configuration.MAX_OPTION_GAME_CYCLE_TIME - gameCycleTimeValue);
        gameCycleTimeSlider = new JSlider(Configuration.MIN_OPTION_GAME_CYCLE_TIME,
                Configuration.MAX_OPTION_GAME_CYCLE_TIME, gameCycleTimeSliderInitialValue);
        gameCycleTimeSlider.setMajorTickSpacing(10);
        gameCycleTimeSlider.setPaintTicks(true);
        Hashtable<Integer, JLabel> gameCycleTimeLabelsTable = new Hashtable<Integer, JLabel>();
        gameCycleTimeLabelsTable.put(new Integer(Configuration.MIN_OPTION_GAME_CYCLE_TIME), new JLabel("Slow"));
        gameCycleTimeLabelsTable.put(new Integer((Configuration.MAX_OPTION_GAME_CYCLE_TIME +
                Configuration.MIN_OPTION_GAME_CYCLE_TIME) / 2), new JLabel("Normal"));
        gameCycleTimeLabelsTable.put(new Integer(Configuration.MAX_OPTION_GAME_CYCLE_TIME), new JLabel("Fast"));
        gameCycleTimeSlider.setLabelTable(gameCycleTimeLabelsTable);
        gameCycleTimeSlider.setPaintLabels(true);
        gameplayGroupPanel.add(gameCycleTimeLabel);
        gameplayGroupPanel.add(gameCycleTimeSlider);
        
        gameplayGroupLayout.putConstraint(SpringLayout.WEST, gameCycleTimeLabel,
                OPTION_PADDING_HORIZONTAL, SpringLayout.WEST, gameplayGroupPanel);
        gameplayGroupLayout.putConstraint(SpringLayout.NORTH, gameCycleTimeLabel,
                (gameCycleTimeSlider.getPreferredSize().height - gameCycleTimeLabel.getPreferredSize().height) / 2,
                SpringLayout.NORTH, gameCycleTimeSlider);
        gameplayGroupLayout.putConstraint(SpringLayout.EAST, gameCycleTimeLabel,
                LABEL_WIDTH, SpringLayout.WEST, gameCycleTimeLabel);
        
        gameplayGroupLayout.putConstraint(SpringLayout.WEST, gameCycleTimeSlider,
                0, SpringLayout.EAST, gameCycleTimeLabel);
        gameplayGroupLayout.putConstraint(SpringLayout.NORTH, gameCycleTimeSlider,
                OPTION_PADDING_VERTICAL, SpringLayout.NORTH, gameplayGroupPanel);
        gameplayGroupLayout.putConstraint(SpringLayout.EAST, gameCycleTimeSlider,
                -OPTION_PADDING_HORIZONTAL, SpringLayout.EAST, gameplayGroupPanel);
        
        // Adjusting gameplay group's vertical size
        gameplayGroupLayout.putConstraint(SpringLayout.SOUTH, gameplayGroupPanel,
                OPTION_PADDING_VERTICAL, SpringLayout.SOUTH, gameCycleTimeSlider);
        
        // Adding gameplay group to content pane
        contentPane.add(gameplayGroupPanel);
        contentLayout.putConstraint(SpringLayout.WEST, gameplayGroupPanel, PADDING_HORIZONTAL, SpringLayout.WEST, contentPane);
        contentLayout.putConstraint(SpringLayout.NORTH, gameplayGroupPanel, PADDING_VERTICAL, SpringLayout.NORTH, contentPane);
        
        // Creating interface group
        JPanel interfaceGroupPanel = new JPanel();
        SpringLayout interfaceGroupLayout = new SpringLayout();
        interfaceGroupPanel.setLayout(interfaceGroupLayout);
        interfaceGroupPanel.setBorder(new TitledBorder(groupPanelBorder, "Interface"));
        
        // Creating level width's label and edit field
        JLabel levelWidthLabel = new JLabel("Level Width");
        levelWidthTextField = new JTextField();
        levelWidthTextField.setHorizontalAlignment(JTextField.TRAILING);
        levelWidthTextField.setText(gameConfiguration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH).toString());        
        
        interfaceGroupPanel.add(levelWidthLabel);
        interfaceGroupPanel.add(levelWidthTextField);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, levelWidthLabel,
                OPTION_PADDING_HORIZONTAL, SpringLayout.WEST, interfaceGroupPanel);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, levelWidthLabel,
                OPTION_PADDING_VERTICAL, SpringLayout.NORTH, interfaceGroupPanel);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelWidthLabel, LABEL_WIDTH, SpringLayout.WEST, levelWidthLabel);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, levelWidthTextField, 0, SpringLayout.EAST, levelWidthLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, levelWidthTextField,
                labelBaseline - textFieldBaseline, SpringLayout.NORTH, levelWidthLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelWidthTextField,
                -OPTION_PADDING_HORIZONTAL, SpringLayout.EAST, interfaceGroupPanel);
        
        // Creating level height's label and edit field
        JLabel levelHeightLabel = new JLabel("Level Height");
        levelHeightTextField = new JTextField();
        levelHeightTextField.setHorizontalAlignment(JTextField.TRAILING);
        levelHeightTextField.setText(gameConfiguration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT).toString());
        interfaceGroupPanel.add(levelHeightLabel);
        interfaceGroupPanel.add(levelHeightTextField);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, levelHeightLabel, 0, SpringLayout.WEST, levelWidthLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, levelHeightLabel,
                OPTION_GAP, SpringLayout.SOUTH, levelWidthLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelHeightLabel, 0, SpringLayout.EAST, levelWidthLabel);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, levelHeightTextField, 0, SpringLayout.WEST, levelWidthTextField);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, levelHeightTextField,
                labelBaseline - textFieldBaseline, SpringLayout.NORTH, levelHeightLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelHeightTextField, 0, SpringLayout.EAST, levelWidthTextField);
        
        // Creating sprite size's label and combobox
        JLabel spriteSizeLabel = new JLabel("Sprite Size");
        spriteSizeComboBox = new JComboBox();
        spriteSizeComboBox.addItem(new ComboBoxOption(Configuration.OPTION_SPRITE_SIZE_OPTIMAL, OPTION_VALUE_TEXT_OPTIMAL));
        spriteSizeComboBox.addItem(new ComboBoxOption(Configuration.OPTION_SPRITE_SIZE_LARGE, OPTION_VALUE_TEXT_LARGE));
        spriteSizeComboBox.addItem(new ComboBoxOption(Configuration.OPTION_SPRITE_SIZE_MEDIUM, OPTION_VALUE_TEXT_MEDIUM));
        spriteSizeComboBox.addItem(new ComboBoxOption(Configuration.OPTION_SPRITE_SIZE_SMALL, OPTION_VALUE_TEXT_SMALL));
        
        // Initializing sprite size's combobox
        String spriteSizeKey = (String)gameConfiguration.getOption(Configuration.OPTION_SPRITE_SIZE,
                Configuration.DEFAULT_OPTION_SPRITE_SIZE);
        
        boolean isSpriteSizeSelected = false;
        int spriteSizeItemIndex = 0;
        while (!isSpriteSizeSelected && spriteSizeItemIndex < spriteSizeComboBox.getItemCount()) {
            
            if (spriteSizeKey.equals(((ComboBoxOption)spriteSizeComboBox.getItemAt(spriteSizeItemIndex)).getKey())) {
                
                isSpriteSizeSelected = true;
                spriteSizeComboBox.setSelectedIndex(spriteSizeItemIndex);
            }
            spriteSizeItemIndex++;
        }
        interfaceGroupPanel.add(spriteSizeLabel);
        interfaceGroupPanel.add(spriteSizeComboBox);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, spriteSizeLabel, 0, SpringLayout.WEST, levelHeightLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, spriteSizeLabel,
                OPTION_GAP, SpringLayout.SOUTH, levelHeightLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, spriteSizeLabel, 0, SpringLayout.EAST, levelHeightLabel);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, spriteSizeComboBox, 0, SpringLayout.WEST, levelHeightTextField);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, spriteSizeComboBox,
                labelBaseline - comboBoxBaseline, SpringLayout.NORTH, spriteSizeLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, spriteSizeComboBox, 0, SpringLayout.EAST, levelHeightTextField);
        
        // Adjusting interface group's vertical size
        interfaceGroupLayout.putConstraint(SpringLayout.SOUTH, interfaceGroupPanel,
                OPTION_PADDING_VERTICAL, SpringLayout.SOUTH, spriteSizeLabel);
        
        // Adding interface group to content pane
        contentPane.add(interfaceGroupPanel);
        contentLayout.putConstraint(SpringLayout.WEST, interfaceGroupPanel, 0, SpringLayout.WEST, gameplayGroupPanel);
        contentLayout.putConstraint(SpringLayout.NORTH, interfaceGroupPanel, GROUP_GAP, SpringLayout.SOUTH, gameplayGroupPanel);
        contentLayout.putConstraint(SpringLayout.EAST, interfaceGroupPanel, 0, SpringLayout.EAST, gameplayGroupPanel);
        
        // Creating buttons panel
        JPanel buttonsPanel = new JPanel();
        SpringLayout buttonsLayout = new SpringLayout();
        buttonsPanel.setLayout(buttonsLayout);
        
        // Creating dialog buttons
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (applyChanges())
                    closeDialog();
            }
        });
        JButton discardButton = new JButton("Discard");
        discardButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                closeDialog();
            }
        });
        buttonsPanel.add(applyButton);
        buttonsPanel.add(discardButton);
        
        buttonsLayout.putConstraint(SpringLayout.WEST, applyButton, -BUTTON_WIDTH, SpringLayout.EAST, applyButton);
        buttonsLayout.putConstraint(SpringLayout.NORTH, applyButton, 0, SpringLayout.NORTH, discardButton);
        buttonsLayout.putConstraint(SpringLayout.EAST, applyButton, -BUTTON_GAP, SpringLayout.WEST, discardButton);
        buttonsLayout.putConstraint(SpringLayout.WEST, discardButton, -BUTTON_WIDTH, SpringLayout.EAST, discardButton);
        buttonsLayout.putConstraint(SpringLayout.NORTH, discardButton, 0, SpringLayout.NORTH, buttonsPanel);
        buttonsLayout.putConstraint(SpringLayout.EAST, discardButton, 0, SpringLayout.EAST, buttonsPanel);
        buttonsLayout.putConstraint(SpringLayout.SOUTH, buttonsPanel, 0, SpringLayout.SOUTH, discardButton);
        
        // Adding buttons panel to content pane
        contentPane.add(buttonsPanel);
        contentLayout.putConstraint(SpringLayout.WEST, buttonsPanel, 0, SpringLayout.WEST, interfaceGroupPanel);
        contentLayout.putConstraint(SpringLayout.NORTH, buttonsPanel, GROUP_GAP, SpringLayout.SOUTH, interfaceGroupPanel);
        contentLayout.putConstraint(SpringLayout.EAST, buttonsPanel, 0, SpringLayout.EAST, interfaceGroupPanel);

        // Adjusting dialog's size
        contentLayout.putConstraint(SpringLayout.EAST, contentPane, PADDING_HORIZONTAL, SpringLayout.EAST, buttonsPanel);
        contentLayout.putConstraint(SpringLayout.SOUTH, contentPane, PADDING_VERTICAL, SpringLayout.SOUTH, buttonsPanel);
        
        pack();
        setSize(new Dimension(DIALOG_WIDTH, getSize().height));
        
        // Preventing the dialog from being resized
        setResizable(false);
        
        // Centering the dialog
        setLocationRelativeTo(null);
    }
    
    /**
     * Checks whether game window's rebuild is required after
     * changes have been applied.
     * 
     * @return
     *      {@code true} if game window's rebuild is required, {@code false} otherwise.
     * @see #isGameWindowRebuildRequired
     * @see #applyChanges()
     */
    public boolean isGameWindowRebuildRequired() {
        
        return isGameWindowRebuildRequired;
    }
    
    /**
     * Applies changes made by user in the dialog.
     * 
     * @return
     *      {@code true} if changes have been successfully applied, {@code false} otherwise.
     * @see #isGameWindowRebuildRequired()
     */
    protected boolean applyChanges() {

        // Retrieving current configuration
        Configuration gameConfiguration = desktopGame.getGameInstance().getGameConfiguration();
        Integer currentGameCycleTime = (Integer)gameConfiguration.getOption(Configuration.OPTION_GAME_CYCLE_TIME,
                Configuration.DEFAULT_OPTION_GAME_CYCLE_TIME);
        Integer currentLevelWidth = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
        Integer currentLevelHeight = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        String currentSpriteSize = (String)gameConfiguration.getOption(Configuration.OPTION_SPRITE_SIZE,
                Configuration.DEFAULT_OPTION_SPRITE_SIZE);
        
        ArrayList<String> errors = new ArrayList<String>();
        
        // Validating game cycle time
        Integer gameCycleTime = Configuration.MAX_OPTION_GAME_CYCLE_TIME -
                (gameCycleTimeSlider.getValue() - Configuration.MIN_OPTION_GAME_CYCLE_TIME);
        if (gameCycleTime < Configuration.MIN_OPTION_GAME_CYCLE_TIME ||
                gameCycleTime > Configuration.MAX_OPTION_GAME_CYCLE_TIME) {
            
            errors.add("Game speed is incorrect.");
        }
        
        // Validating level width
        Integer levelWidth = null;
        try {
            
            levelWidth = Integer.parseInt(levelWidthTextField.getText());
            if (levelWidth < Configuration.MIN_OPTION_LEVEL_WIDTH || levelWidth > Configuration.MAX_OPTION_LEVEL_WIDTH) {
                
                errors.add(String.format("Level width must be a decimal number no less than %d and no more than %d.",
                        Configuration.MIN_OPTION_LEVEL_WIDTH, Configuration.MAX_OPTION_LEVEL_WIDTH));
                levelWidth = null;
            }
        }
        catch (NumberFormatException ex) {
            
            errors.add("Level width must be a decimal number.");
        }
        
        // Validating level height
        Integer levelHeight = null;
        try {
            
            levelHeight = Integer.parseInt(levelHeightTextField.getText());
            if (levelHeight < Configuration.MIN_OPTION_LEVEL_HEIGHT || levelHeight > Configuration.MAX_OPTION_LEVEL_HEIGHT) {
                
                errors.add(String.format("Level height must be a decimal number no less than %d and no more than %d.",
                        Configuration.MIN_OPTION_LEVEL_HEIGHT, Configuration.MAX_OPTION_LEVEL_HEIGHT));
                levelHeight = null;
            }
        }
        catch (NumberFormatException ex) {
            
            errors.add("Level height must be a decimal number.");
        }
        
        // Validating sprite size
        String spriteSize = null;
        try {
            
            spriteSize = ((ComboBoxOption)spriteSizeComboBox.getSelectedItem()).getKey();
        }
        catch (NullPointerException ex) {
            
            errors.add("Sprite size is not selected.");
        }
        
        if (!errors.isEmpty()) {
            
            String errorString = "";
            for (String error : errors) {
                
                if (!errorString.isEmpty())
                    errorString += "\n";
                errorString += error;
            }
            
            JOptionPane.showMessageDialog(this, errorString, "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Checking whether changes have been made
        boolean areChangesMade = !currentGameCycleTime.equals(gameCycleTime)
                || !currentLevelWidth.equals(levelWidth)
                || !currentLevelHeight.equals(levelHeight)
                || !currentSpriteSize.equals(spriteSize);
        
        if (areChangesMade) {
            
            // Applying the changes
            gameConfiguration.setOption(Configuration.OPTION_GAME_CYCLE_TIME, gameCycleTime);
            gameConfiguration.setOption(Configuration.OPTION_LEVEL_WIDTH, levelWidth);
            gameConfiguration.setOption(Configuration.OPTION_LEVEL_HEIGHT, levelHeight);
            gameConfiguration.setOption(Configuration.OPTION_SPRITE_SIZE, spriteSize);
            
            // Trying to save the changes
            if (!gameConfiguration.save()) {

                JOptionPane.showMessageDialog(this,
                        "Unable to save the changes to configuration file.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            
            isGameWindowRebuildRequired = !currentLevelWidth.equals(levelWidth)
                    || !currentLevelHeight.equals(levelHeight)
                    || !currentSpriteSize.equals(spriteSize);
        }

        return true;
    }
    
    protected void closeDialog() {
        
        WindowListener[] windowListeners = getWindowListeners();
        if (windowListeners.length > 0) {
            
            for (WindowListener windowListener : windowListeners)
                windowListener.windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    protected void onCloseDialog() {
        
        dispose();
    }
}