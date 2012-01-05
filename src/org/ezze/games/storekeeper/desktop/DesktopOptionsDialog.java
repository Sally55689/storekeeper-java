package org.ezze.games.storekeeper.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public class DesktopOptionsDialog extends JDialog {
    
    private final static int PADDING_HORIZONTAL = 10;
    private final static int PADDING_VERTICAL = 10;
    
    private final static int GROUP_WIDTH = 300;
    private final static int GROUP_GAP = 8;
    
    private final static int BUTTON_WIDTH = 80;
    private final static int BUTTON_GAP = 4;
    
    public DesktopOptionsDialog(JFrame parentFrame) {
        
        // Creating modal dialog
        super(parentFrame, true);
        
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
        interfaceGroupPanel.setBorder(new TitledBorder(groupPanelBorder, "Interface"));
        
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
                
                // TODO: implement applyings
                
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