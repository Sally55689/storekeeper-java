package org.ezze.games.storekeeper.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
    private final static int OPTION_PADDING_VERTICAL = 6;
    private final static int OPTION_GAP = 4;
    
    private final static int LABEL_WIDTH = 100;
 
    private final static int BUTTON_WIDTH = 80;
    private final static int BUTTON_GAP = 4;
    
    private DesktopGame desktopGame = null;
    
    private JTextField levelWidthTextField = null;
    private JTextField levelHeightTextField = null;
    
    private boolean isGameWindowRebuildRequired = false;
    
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
        Border groupPanelBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        
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
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, levelWidthLabel, OPTION_PADDING_HORIZONTAL, SpringLayout.WEST, interfaceGroupPanel);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, levelWidthLabel,
                textFieldBaseline - labelBaseline, SpringLayout.NORTH, levelWidthTextField);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelWidthLabel, LABEL_WIDTH, SpringLayout.WEST, levelWidthLabel);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, levelWidthTextField, 0, SpringLayout.EAST, levelWidthLabel);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, levelWidthTextField, OPTION_PADDING_VERTICAL, SpringLayout.NORTH, interfaceGroupPanel);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelWidthTextField, -OPTION_PADDING_HORIZONTAL, SpringLayout.EAST, interfaceGroupPanel);
        
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
                textFieldBaseline - labelBaseline, SpringLayout.NORTH, levelHeightTextField);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelHeightLabel, 0, SpringLayout.EAST, levelWidthLabel);
        
        interfaceGroupLayout.putConstraint(SpringLayout.WEST, levelHeightTextField, 0, SpringLayout.WEST, levelWidthTextField);
        interfaceGroupLayout.putConstraint(SpringLayout.NORTH, levelHeightTextField, OPTION_GAP, SpringLayout.SOUTH, levelWidthTextField);
        interfaceGroupLayout.putConstraint(SpringLayout.EAST, levelHeightTextField, 0, SpringLayout.EAST, levelWidthTextField);
        
        interfaceGroupLayout.putConstraint(SpringLayout.SOUTH, interfaceGroupPanel, OPTION_PADDING_VERTICAL, SpringLayout.SOUTH, levelHeightTextField);
        
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
    
    public boolean isGameWindowRebuildRequired() {
        
        return isGameWindowRebuildRequired;
    }
    
    private boolean applyChanges() {

        // Retrieving current configuration
        GameConfiguration gameConfiguration = desktopGame.getGameInstance().getGameConfiguration();
        Integer currentLevelWidth = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_WIDTH,
                GameConfiguration.DEFAULT_OPTION_LEVEL_WIDTH);
        Integer currentLevelHeight = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_HEIGHT,
                GameConfiguration.DEFAULT_OPTION_LEVEL_HEIGHT);
        
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
                || !currentLevelHeight.equals(levelHeight);
        
        if (areChangesMade) {
            
            // Applying the changes
            gameConfiguration.setOption(GameConfiguration.OPTION_LEVEL_WIDTH, levelWidth);
            gameConfiguration.setOption(GameConfiguration.OPTION_LEVEL_HEIGHT, levelHeight);
            
            // Trying to save the changes
            if (!gameConfiguration.save()) {

                JOptionPane.showMessageDialog(this,
                        "Unable to save the changes to configuration file", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            
            isGameWindowRebuildRequired = !currentLevelWidth.equals(levelWidth)
                    || !currentLevelHeight.equals(levelHeight);
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