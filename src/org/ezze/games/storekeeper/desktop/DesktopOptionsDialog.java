package org.ezze.games.storekeeper.desktop;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.ezze.games.storekeeper.Game;
import org.ezze.games.storekeeper.GameConfiguration;

/**
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public class DesktopOptionsDialog extends JDialog {
    
    private final static int PADDING_HORIZONTAL = 10;
    private final static int PADDING_VERTICAL = 10;
    
    private final static int GROUP_WIDTH = 300;
    private final static int GROUP_GAP = 8;
    
    private final static int OPTION_PADDING_HORIZONTAL = 8;
    private final static int OPTION_PADDING_VERTICAL = 8;
    private final static int OPTION_GAP = 10;
    
    private final static int LABEL_WIDTH = 100;
 
    private final static int BUTTON_WIDTH = 80;
    private final static int BUTTON_GAP = 4;
    
    private final static String OPTION_VALUE_TEXT_OPTIMAL = "Optimal";
    private final static String OPTION_VALUE_TEXT_LARGE = "Large";
    private final static String OPTION_VALUE_TEXT_MEDIUM = "Medium";
    private final static String OPTION_VALUE_TEXT_SMALL = "Small";
    
    /**
     * A reference to desktop game's instance.
     */
    private DesktopGame desktopGame = null;
    
    /**
     * Text field to edit level's maximal width.
     */
    private JTextField levelWidthTextField = null;
    
    /**
     * Text field to edit level's maximal height.
     */
    private JTextField levelHeightTextField = null;
    
    /**
     * Combobox to select sprite's size.
     */
    private JComboBox spriteSizeComboBox = null;
    
    /**
     * Shows whether game window's rebuild is required after
     * options' changes have been applied.
     */
    private boolean isGameWindowRebuildRequired = false;
    
    /**
     * Combobox' option class used to associate its value with a key.
     */
    private class ComboBoxOption {
        
        /**
         * Option's key.
         */
        private String optionKey = null;
        
        /**
         * Option's value.
         */
        private Object optionValue = null;
        
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
    public DesktopOptionsDialog(DesktopGame desktopGame, JFrame parentFrame) {
        
        // Creating modal dialog
        super(parentFrame, true);
        
        // Saving a reference to game instance
        this.desktopGame = desktopGame;
        Game game = desktopGame.getGameInstance();
        GameConfiguration gameConfiguration = game.getGameConfiguration();
        
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
        
        // Creating right aligned list cell renderer
        final ListCellRenderer rightAlignedListCellRenderer = new DefaultListCellRenderer() {
            
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (renderer instanceof JLabel)
                    ((JLabel)renderer).setHorizontalAlignment(JLabel.RIGHT);
                
                return renderer;
            }
        };
        
        // Creating gameplay group
        JPanel gameplayGroupPanel = new JPanel();
        gameplayGroupPanel.setPreferredSize(new Dimension(GROUP_WIDTH, gameplayGroupPanel.getPreferredSize().height));
        gameplayGroupPanel.setBorder(new TitledBorder(groupPanelBorder, "Gameplay"));
        
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
        levelWidthTextField.setText(gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_WIDTH,
                GameConfiguration.DEFAULT_OPTION_LEVEL_WIDTH).toString());
        int labelBaseline = levelWidthLabel.getBaseline(0, levelWidthLabel.getPreferredSize().height);
        int textFieldBaseline = levelWidthTextField.getBaseline(0, levelWidthTextField.getPreferredSize().height);
        
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
        levelHeightTextField.setText(gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_HEIGHT,
                GameConfiguration.DEFAULT_OPTION_LEVEL_HEIGHT).toString());
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
        spriteSizeComboBox.setRenderer(rightAlignedListCellRenderer);
        spriteSizeComboBox.addItem(new ComboBoxOption(GameConfiguration.OPTION_SPRITE_SIZE_OPTIMAL, OPTION_VALUE_TEXT_OPTIMAL));
        spriteSizeComboBox.addItem(new ComboBoxOption(GameConfiguration.OPTION_SPRITE_SIZE_LARGE, OPTION_VALUE_TEXT_LARGE));
        spriteSizeComboBox.addItem(new ComboBoxOption(GameConfiguration.OPTION_SPRITE_SIZE_MEDIUM, OPTION_VALUE_TEXT_MEDIUM));
        spriteSizeComboBox.addItem(new ComboBoxOption(GameConfiguration.OPTION_SPRITE_SIZE_SMALL, OPTION_VALUE_TEXT_SMALL));
        
        // Initializing sprite size's combobox
        String spriteSizeKey = (String)gameConfiguration.getOption(GameConfiguration.OPTION_SPRITE_SIZE,
                GameConfiguration.DEFAULT_OPTION_SPRITE_SIZE);
        
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
                labelBaseline - textFieldBaseline, SpringLayout.NORTH, spriteSizeLabel);
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
    private boolean applyChanges() {

        // Retrieving current configuration
        GameConfiguration gameConfiguration = desktopGame.getGameInstance().getGameConfiguration();
        Integer currentLevelWidth = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_WIDTH,
                GameConfiguration.DEFAULT_OPTION_LEVEL_WIDTH);
        Integer currentLevelHeight = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_HEIGHT,
                GameConfiguration.DEFAULT_OPTION_LEVEL_HEIGHT);
        String currentSpriteSize = (String)gameConfiguration.getOption(GameConfiguration.OPTION_SPRITE_SIZE,
                GameConfiguration.DEFAULT_OPTION_SPRITE_SIZE);
        
        ArrayList<String> errors = new ArrayList<String>();
        
        // Validating level width
        Integer levelWidth = null;
        try {
            
            levelWidth = Integer.parseInt(levelWidthTextField.getText());
            if (levelWidth < GameConfiguration.MIN_OPTION_LEVEL_WIDTH || levelWidth > GameConfiguration.MAX_OPTION_LEVEL_WIDTH) {
                
                errors.add(String.format("Level width must be a decimal number no less than %d and no more than %d.",
                        GameConfiguration.MIN_OPTION_LEVEL_WIDTH, GameConfiguration.MAX_OPTION_LEVEL_WIDTH));
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
            if (levelHeight < GameConfiguration.MIN_OPTION_LEVEL_HEIGHT || levelHeight > GameConfiguration.MAX_OPTION_LEVEL_HEIGHT) {
                
                errors.add(String.format("Level height must be a decimal number no less than %d and no more than %d.",
                        GameConfiguration.MIN_OPTION_LEVEL_HEIGHT, GameConfiguration.MAX_OPTION_LEVEL_HEIGHT));
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
        boolean areChangesMade = !currentLevelWidth.equals(levelWidth)
                || !currentLevelHeight.equals(levelHeight)
                || !currentSpriteSize.equals(spriteSize);
        
        if (areChangesMade) {
            
            // Applying the changes
            gameConfiguration.setOption(GameConfiguration.OPTION_LEVEL_WIDTH, levelWidth);
            gameConfiguration.setOption(GameConfiguration.OPTION_LEVEL_HEIGHT, levelHeight);
            gameConfiguration.setOption(GameConfiguration.OPTION_SPRITE_SIZE, spriteSize);
            
            // Trying to save the changes
            if (!gameConfiguration.save()) {

                JOptionPane.showMessageDialog(this,
                        "Unable to save the changes to configuration file", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            
            isGameWindowRebuildRequired = !currentLevelWidth.equals(levelWidth)
                    || !currentLevelHeight.equals(levelHeight)
                    || !currentSpriteSize.equals(spriteSize);
        }

        return true;
    }
    
    private void closeDialog() {
        
        WindowListener[] windowListeners = getWindowListeners();
        if (windowListeners.length > 0) {
            
            for (WindowListener windowListener : windowListeners)
                windowListener.windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    private void onCloseDialog() {
        
        dispose();
    }
}