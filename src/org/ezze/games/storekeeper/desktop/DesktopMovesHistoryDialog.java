package org.ezze.games.storekeeper.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ezze.games.storekeeper.Game;

/**
 * A dialog to walk over moves' stack.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.1
 */
public class DesktopMovesHistoryDialog extends JDialog {

    /**
     * Hint to show when game's position will not be changed.
     */
    protected static final String HINT_POSITION_WILL_NOT_BE_CHANGED = "Position will not be changed.";
    
    /**
     * Hint to show when game's position will be taken back.
     */
    protected static final String HINT_POSITION_WILL_BE_TAKEN_BACK = "Position will be taken back by %d move(s).";
    
    /**
     * Hint to show when game's position will be restored.
     */
    protected static final String HINT_POSITION_WILL_BE_RESTORED = "Position will be restored by repeating %d move(s).";
    
    /**
     * Dialog's width.
     */
    protected static final int DIALOG_WIDTH = 600;
    
    /**
     * Dialog's horizontal padding.
     */
    protected static final int PADDING_HORIZONTAL = 10;
    
    /**
     * Dialog's vertical padding.
     */
    protected static final int PADDING_VERTICAL = 10;
    
    /**
     * Option's vertical gap.
     */
    protected static final int OPTION_GAP = 10;
    
    /**
     * Button's width.
     */
    protected static final int BUTTON_WIDTH = 80;
    
    /**
     * Button's horizontal gap.
     */
    protected static final int BUTTON_GAP = 4;
    
    /**
     * Moves history's changes listener class.
     */
    public class MovesHistoryListener implements ChangeListener {

        /**
         * Keeps initial moves count.
         */
        protected int initialMovesCount = 0;
        
        /**
         * Keeps current moves count.
         */
        protected int currentMovesCount = 0;

        /**
         * Listener's constructor.
         * 
         * @param movesCount 
         *      Initial moves count.
         */
        public MovesHistoryListener(int movesCount) {
            
            initialMovesCount = movesCount;
            currentMovesCount = movesCount;
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            
            Object sourceObject = e.getSource();
            if (!(sourceObject instanceof JSlider))
                return;

            JSlider slider = (JSlider)sourceObject;
            if (!slider.getValueIsAdjusting())
                return;

            int movesShift = slider.getValue() - currentMovesCount;
            if (movesShift < 0)
                desktopGame.getGameInstance().takeBack(Math.abs(movesShift));
            else if (movesShift > 0)
                desktopGame.getGameInstance().repeatMoves(movesShift);
            
            currentMovesCount += movesShift;
            
            int movesShiftSummary = getMovesShift();
            if (movesShiftSummary < 0) {
                
                movesHistoryLabel.setText(String.format(HINT_POSITION_WILL_BE_TAKEN_BACK, Math.abs(movesShiftSummary)));
            }
            else if (movesShiftSummary > 0) {
                
                movesHistoryLabel.setText(String.format(HINT_POSITION_WILL_BE_RESTORED, movesShiftSummary));
            }
            else {
                
                movesHistoryLabel.setText(HINT_POSITION_WILL_NOT_BE_CHANGED);
            }
            
            applyButton.setEnabled(movesShiftSummary != 0);
        }
        
        /**
         * Retrieves summary moves' count performed during position's changing.
         * 
         * @return 
         *      Performed moves' count, negative value if game's position has been taken back
         *      and position value if game's position has been restored.
         */
        public int getMovesShift() {
            
            return currentMovesCount - initialMovesCount;
        }
    }
    
    /**
     * Shows whether user has cancelled moves history changes.
     */
    protected boolean isCancelled = true;
    
    /**
     * A reference to desktop game's instance.
     */
    protected DesktopGame desktopGame = null;
    
    /**
     * An instance of moves history's changes listener.
     */
    protected MovesHistoryListener movesHistoryListener = null;
    
    /**
     * An instance of slider to walk over moves' stack.
     */
    protected JSlider movesHistorySlider = null;
    
    /**
     * A label to describe an action to be applied.
     */
    protected JLabel movesHistoryLabel = null;
    
    /**
     * Dialog's apply button's instance.
     */
    protected JButton applyButton = null;
    
    /**
     * Dialog's discard button's instance.
     */
    protected JButton discardButton = null;
 
    /**
     * Dialog's constructor.
     * 
     * @param desktopGame
     *      A reference to desktop game's instance.
     * @param parentFrame 
     *      A reference to dialog's parent frame.
     */
    public DesktopMovesHistoryDialog(DesktopGame desktopGame, Frame parentFrame) {
        
        super(parentFrame, true);
        
        this.desktopGame = desktopGame;
        Game game = this.desktopGame.getGameInstance();
 
        int movesCount = 0;
        int movesHistoryCount = 0;
        try {
            
            movesCount = game.getLevelsSet().getCurrentLevel().getMovesCount();
            movesHistoryCount = game.getLevelsSet().getCurrentLevel().getMovesHistoryCount();
        }
        catch (NullPointerException ex) {
            
            String message = "Level instance is not available.";
            Logger.getLogger(DesktopMovesHistoryDialog.class.getPackage().getName()).severe(message);
            throw new IllegalStateException(message);
        }
        
        // Setting window's close handler
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
           
            @Override
            public void windowClosing(WindowEvent e) {
                
                onClose();
            }
        });
        
        setTitle("Moves History");
        
        Container contentPane = getContentPane();
        SpringLayout contentLayout = new SpringLayout();
        contentPane.setLayout(contentLayout);
        
        // Creating moves history slider and label
        movesHistorySlider = new JSlider(0, movesHistoryCount, movesCount);
        movesHistorySlider.setMajorTickSpacing(movesHistoryCount <= 10 ? 1 : movesHistoryCount / 10);
        movesHistorySlider.setPaintTicks(true);
        movesHistorySlider.setPaintLabels(true);
        movesHistoryLabel = new JLabel(HINT_POSITION_WILL_NOT_BE_CHANGED);
        
        movesHistoryListener = new MovesHistoryListener(movesCount);
        movesHistorySlider.addChangeListener(movesHistoryListener);
        
        // Adding moves history slider and label to content pane
        contentPane.add(movesHistorySlider);
        contentPane.add(movesHistoryLabel);
        
        contentLayout.putConstraint(SpringLayout.WEST, movesHistorySlider,
                PADDING_HORIZONTAL, SpringLayout.WEST, contentPane);
        contentLayout.putConstraint(SpringLayout.NORTH, movesHistorySlider,
                PADDING_VERTICAL, SpringLayout.NORTH, contentPane);
        
        contentLayout.putConstraint(SpringLayout.WEST, movesHistoryLabel, 0, SpringLayout.WEST, movesHistorySlider);
        contentLayout.putConstraint(SpringLayout.NORTH, movesHistoryLabel,
                OPTION_GAP, SpringLayout.SOUTH, movesHistorySlider);
        contentLayout.putConstraint(SpringLayout.EAST, movesHistoryLabel, 0, SpringLayout.EAST, movesHistorySlider);
        
        // Creating buttons
        applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                isCancelled = false;
                DesktopMovesHistoryDialog.this.close();
            }
        });
        
        discardButton = new JButton("Discard");
        discardButton.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent e) {
                               
                DesktopMovesHistoryDialog.this.close();
            }
        });
        
        // Adding buttons to content pane
        contentPane.add(applyButton);
        contentPane.add(discardButton);
        
        contentLayout.putConstraint(SpringLayout.WEST, applyButton, -BUTTON_WIDTH, SpringLayout.EAST, applyButton);
        contentLayout.putConstraint(SpringLayout.NORTH, applyButton, OPTION_GAP, SpringLayout.SOUTH, movesHistoryLabel);
        contentLayout.putConstraint(SpringLayout.EAST, applyButton, -BUTTON_GAP, SpringLayout.WEST, discardButton);
        
        contentLayout.putConstraint(SpringLayout.WEST, discardButton, -BUTTON_WIDTH, SpringLayout.EAST, discardButton);
        contentLayout.putConstraint(SpringLayout.NORTH, discardButton, 0, SpringLayout.NORTH, applyButton);
        contentLayout.putConstraint(SpringLayout.EAST, discardButton, 0, SpringLayout.EAST, movesHistoryLabel);
        
        // Adjusting dialog's size
        contentLayout.putConstraint(SpringLayout.EAST, contentPane,
                PADDING_HORIZONTAL, SpringLayout.EAST, movesHistorySlider);
        contentLayout.putConstraint(SpringLayout.SOUTH, contentPane,
                PADDING_VERTICAL, SpringLayout.SOUTH, discardButton);
        
        pack();
        
        // Stretching dialog horizontally
        setSize(new Dimension(DIALOG_WIDTH, getSize().height));
        
        setResizable(false);
        setLocationRelativeTo(parentFrame);
    }
    
    /**
     * Fires dialog's closing event by invoking convenient methods of its
     * window listeners.
     */
    public void close() {
        
        for (WindowListener windowListener : getWindowListeners())
            windowListener.windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
    
    /**
     * Discards any moves history's changes if they are not applied by the user
     * and closes the dialog.
     */
    public void onClose() {
        
        if (isCancelled) {
            
            int movesShift = movesHistoryListener.getMovesShift();
            if (movesShift < 0)
                desktopGame.getGameInstance().repeatMoves(Math.abs(movesShift));
            else if (movesShift > 0)
                desktopGame.getGameInstance().takeBack(movesShift);
        }

        dispose();
    }
}