package org.ezze.games.storekeeper.desktop;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import javax.swing.UnsupportedLookAndFeelException;
import org.ezze.utils.application.ApplicationPath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.ezze.games.storekeeper.Game;
import org.ezze.games.storekeeper.Configuration;
import org.ezze.games.storekeeper.Game.GameState;
import org.ezze.games.storekeeper.GameGraphics;
import org.ezze.games.storekeeper.GameGraphics.SpriteSize;
import org.ezze.games.storekeeper.Level;
import org.ezze.games.storekeeper.Level.LevelSize;
import org.ezze.games.storekeeper.LevelCompletionListener;
import org.ezze.games.storekeeper.LevelsSet;
import org.ezze.utils.io.CompoundFileFilter;
import org.ezze.utils.ui.FileBrowser;
import org.ezze.utils.ui.aboutbox.AboutBox;
import org.ezze.utils.ui.aboutbox.AboutBoxInformation;

/**
 * Desktop version of the game.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.6
 */
public final class DesktopGame extends JFrame {
    
    /**
     * Used as free vertical space in
     * {@link DesktopGameGraphics#determineOptimalSpriteSize(org.ezze.games.storekeeper.Level.LevelSize, int, int)}.
     */
    protected static final int VERTICAL_PADDING = 50;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        new DesktopGame();
    }
    
    /**
     * Game instance.
     */
    protected Game game = null;
    
    /**
     * An instance of game's graphics.
     */
    protected DesktopGameGraphics desktopGameGraphics = null;
    
    /**
     * Menu bar instance.
     */
    protected JMenuBar menuBar = null;
    
    /**
     * File menu instance.
     */
    protected JMenu menuFile = null;
    
    /**
     * File menu item to load levels' set.
     */
    protected JMenuItem menuItemLoadLevelsSet = null;
    
    /**
     * File menu item to load default levels' set.
     */
    protected JMenuItem menuItemLoadDefaultLevelsSet = null;
    
    /**
     * File menu item to exit the game.
     */
    protected JMenuItem menuItemExit = null;
    
    /**
     * Action menu instance.
     */
    protected JMenu menuAction = null;
    
    /**
     * Action menu item to start the game.
     */
    protected JMenuItem menuItemStartTheGame = null;
    
    /**
     * Action menu item to stop the game.
     */
    protected JMenuItem menuItemStopTheGame = null;
    
    /**
     * Action menu item to restart game's current level.
     */
    protected JMenuItem menuItemRestartLevel = null;
    
    /**
     * Action menu item to jump to game's previous level.
     */
    protected JMenuItem menuItemPreviousLevel = null;
    
    /**
     * Action menu item to jump to game's next level.
     */
    protected JMenuItem menuItemNextLevel = null;
    
    /**
     * Action menu item to repeat a move from the history.
     */
    protected JMenuItem menuItemRepeatMove = null;
    
    /**
     * Action menu item to take game level's position back by one move.
     */
    protected JMenuItem menuItemTakeBack = null;
    
    /**
     * Action menu item to show moves history dialog.
     */
    protected JMenuItem menuItemMovesHistory = null;
    
    /**
     * Tools menu instance.
     */
    protected JMenu menuTools = null;
    
    /**
     * Tools menu item to show options dialog.
     */
    protected JMenuItem menuItemOptions = null;
    
    /**
     * Help menu instance.
     */
    protected JMenu menuHelp = null;
    
    /**
     * Help menu item to show about box.
     */
    protected JMenuItem menuItemAbout = null;
    
    /**
     * An instance of status bar.
     */
    protected JPanel statusBar = null;
    
    /**
     * An instance of label to show level's elapsed time.
     */
    protected JLabel timeLabel = null;
    
    /**
     * An instance of level's informational label.
     */
    protected JLabel levelInfoLabel = null;
    
    /**
     * An instance of label to show moves' count performed by the worker.
     */
    protected JLabel movesCountLabel = null;
    
    /**
     * An instance of label to show pushes' count performed by the worker.
     */
    protected JLabel pushesCountLabel = null;
    
    /**
     * Storekeeper game's desktop implementation main class.
     * 
     * This class is used as main one to build the desktop version of the game.
     */
    public DesktopGame() {
        
        try {
            
            // Preventing sliders from painting values
            UIManager.put("Slider.paintValue", false);
            
            // Setting operating system's look and feel
            String operatingSystemName = System.getProperty("os.name");
            if (operatingSystemName.matches("^.*Windows.*$"))
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            else if (operatingSystemName.matches("^.*Linux.*$"))
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        }
        catch (ClassNotFoundException ex) {
            
        }
        catch (InstantiationException ex) {
        
        }
        catch (IllegalAccessException ex) {
        
        }
        catch (UnsupportedLookAndFeelException ex) {
            
        }       
        
        // Appending close action listener
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent we) {
                
                onCloseApplication();
            }
        });
        
        // Reading application's properties
        String applicationProperitesPath = String.format("/%s/resources/storekeeper.properties",
                Game.class.getPackage().getName().replace('.', '/'));
        final Properties applicationProperties = new Properties();
        try {
            
            applicationProperties.load(DesktopGame.class.getResourceAsStream(applicationProperitesPath));
        }
        catch (Exception ex) {
            
            // We were unable to find properties in resources
            JOptionPane.showMessageDialog(null, "Application is corrupted and will be closed now.",
                    "Fatal Error", JOptionPane.ERROR_MESSAGE);
            
            // Closing the application in Event Dispatch Thread
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    
                    closeApplication();
                }
            });
            
            return;
        }              
        
        // Setting game window's title
        setTitle("Storekeeper");
        
        // Setting game window's icon
        String resourcePathToIcon = String.format("/%s/resources/16x16/gripe_right_00.png",
                Game.class.getPackage().getName().replace('.', '/'));
        final URL windowIconURL = DesktopGameGraphics.class.getResource(resourcePathToIcon);
        if (windowIconURL != null)
            setIconImage(new ImageIcon(windowIconURL).getImage());
        
        // Reading game's configuration
        String configurationFileName = String.format("%s/storekeeperConfig.xml",
                ApplicationPath.getApplicationPath(DesktopGame.class));
        final Configuration gameConfiguration = new Configuration(configurationFileName);
        
        // Creating game graphics' instance
        desktopGameGraphics = new DesktopGameGraphics();
        
        // Creating menu bar
        createMenuBar(applicationProperties, windowIconURL);
        setJMenuBar(menuBar);
        
        // Creating status bar
        createStatusBar();        
        
        // Determining default sprite's size
        LevelSize defaultLevelSize = new LevelSize(Level.DEFAULT_LEVEL_WIDTH, Level.DEFAULT_LEVEL_HEIGHT);
        SpriteSize spriteSize = desktopGameGraphics.determineOptimalSpriteSize(defaultLevelSize,
                0, menuBar.getPreferredSize().height + statusBar.getPreferredSize().height + VERTICAL_PADDING);
        if (spriteSize == null) {
        
            JOptionPane.showMessageDialog(null, "Your screen resolution is too small to run the game.\n"
                    + "Please choose another screen resolution if it's possible.", "Screen Resolution Warning",
                    JOptionPane.WARNING_MESSAGE);
            closeApplication();
            return;
        }

        // Creating game instance
        desktopGameGraphics.setSpriteSize(spriteSize);
        game = new Game(gameConfiguration, desktopGameGraphics, new LevelCompletionListener() {
                
            @Override
            public void levelCompleted(Level gameLevel) {
                
                // Informing the user that the level has been successfully completed
                JOptionPane.showMessageDialog(null, "Level has been successfully completed!", "Congratulations", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        game.setDisplayLevelInfo(false);
                        
        JPanel contentPane = new JPanel();
        SpringLayout contentLayout = new SpringLayout();
        contentPane.setLayout(contentLayout);
        
        contentPane.add(game);
        contentLayout.putConstraint(SpringLayout.WEST, game, 0, SpringLayout.WEST, contentPane);
        contentLayout.putConstraint(SpringLayout.NORTH, game, 0, SpringLayout.NORTH, contentPane);
        contentLayout.putConstraint(SpringLayout.EAST, game,
                game.getGameGraphics().getSpriteDimension().width * defaultLevelSize.getWidth(), SpringLayout.WEST, game);
        contentLayout.putConstraint(SpringLayout.SOUTH, game,
                (game.getGameGraphics().getSpriteDimension().height) * defaultLevelSize.getHeight(), SpringLayout.NORTH, game);
        
        contentPane.add(statusBar);
        contentLayout.putConstraint(SpringLayout.WEST, statusBar, 0, SpringLayout.WEST, game);
        contentLayout.putConstraint(SpringLayout.NORTH, statusBar, 0, SpringLayout.SOUTH, game);
        contentLayout.putConstraint(SpringLayout.EAST, statusBar, 0, SpringLayout.EAST, game);
        
        contentLayout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, statusBar);
        contentLayout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, statusBar);
                
        setContentPane(contentPane);
        setResizable(false);                               
        
        pack();
        centerTheWindow();
        
        // Attaching key listener
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
             
            }

            @Override
            public void keyPressed(KeyEvent e) {
             
                if (game.getGameState() != GameState.PLAY)
                    return;
                
                if (e.getKeyCode() == KeyEvent.VK_LEFT)
                    game.forceWorkerToMoveLeft();
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
                    game.forceWorkerToMoveRight();
                else if (e.getKeyCode() == KeyEvent.VK_UP)
                    game.forceWorkerToMoveUp();
                else if (e.getKeyCode() == KeyEvent.VK_DOWN)
                    game.forceWorkerToMoveDown();
            }

            @Override
            public void keyReleased(KeyEvent e) {
             
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)
                    game.forceWorkerToStopHorizontalMovement();
                else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN)
                    game.forceWorkerToStopVerticalMovement();
                updateMenuItems();
            }
        });                       
        
        // Creating game's properties change listener
        PropertyChangeListener gamePropertiesChangeListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                if (timeLabel == null || levelInfoLabel == null || movesCountLabel == null || pushesCountLabel == null)
                    return;
                
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                    
                        GameState gameState = game.getGameState();
                        if (gameState == GameState.STOP) {

                            timeLabel.setText(" ");
                            levelInfoLabel.setText(" ");
                            movesCountLabel.setText(" ");
                            pushesCountLabel.setText(" ");
                        }
                        
                        LevelsSet levelsSet = game.getLevelsSet();
                        if (levelsSet == null)
                            return;
                        
                        Level level = levelsSet.getCurrentLevel();
                        if (level == null)
                            return;
                        
                        if (gameState == GameState.PLAY || gameState == GameState.COMPLETED) {
                            
                            if (gameState == GameState.PLAY)
                                timeLabel.setText(String.format("Time: %s", game.getTimeString()));
                            
                            String movesCountString = String.format("Moves Count: %05d", level.getMovesCount());
                            movesCountLabel.setText(movesCountString);

                            String pushesCountString = String.format("Pushes Count: %05d", level.getPushesCount());
                            pushesCountLabel.setText(pushesCountString);
                        }
                        else {
                            
                            timeLabel.setText(" ");
                            movesCountLabel.setText(" ");
                            pushesCountLabel.setText(" ");
                        }
                        
                        String levelsSetName = levelsSet.getName();
                        String levelName = level.getName();
                        int levelNumber = levelsSet.getCurrentLevelIndex() + 1;
                                
                        String levelInfoString = String.format("Level %03d", levelNumber);
                        if (levelsSetName != null && !levelsSetName.isEmpty()) {

                            if (levelName != null && !levelName.isEmpty())
                                levelInfoString += String.format(" (\"%s\" of %s)", levelName, levelsSetName);
                            else
                                levelInfoString += String.format(" (%s)", levelsSetName);
                        }
                        else if (levelName != null && !levelName.isEmpty()) {
                            
                            levelInfoString += String.format(" (\"%s\")", levelName);
                        }
                        
                        levelInfoLabel.setText(levelInfoString);
                    }
                });
            }
        };
        
        game.addPropertyChangeListener(gamePropertiesChangeListener);
        
        // Loading levels' set in Event Dispatch thread to be sure that
        // game's properties change listener will not handle events
        // at the same time.
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                
                // Loading default levels' set
                loadDefaultLevelsSet();

                // Showing introduction image
                game.stop(true);

                updateMenuItems();
                setVisible(true);
            }
        });
    }
    
    /**
     * Creates game's menu bar.
     * 
     * @param applicationProperties
     *      A reference to application's properties.
     * @param windowIconURL 
     *      URL of window's icon.
     */
    protected void createMenuBar(final Properties applicationProperties, final URL windowIconURL) {
        
        // Creating menu bar
        menuBar = new JMenuBar();
        
        // Creating file menu
        menuFile = new JMenu("File");
        
        menuItemStartTheGame = new JMenuItem("Start The Game");
        menuItemStartTheGame.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent ae) {
                
                if (game != null && game.getGameState() != Game.GameState.PLAY && game.getGameState() != Game.GameState.COMPLETED) {
                    
                    game.startLevel(game.getLevelsSet().getCurrentLevelIndex());
                    updateMenuItems();
                }
            }
        });
        menuItemStartTheGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuFile.add(menuItemStartTheGame);
        
        menuItemStopTheGame = new JMenuItem("Stop The Game");
        menuItemStopTheGame.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent ae) {
                
                if (game != null) {
                    
                    game.stop(true);
                    updateMenuItems();
                }
            }
        });
        menuItemStopTheGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        menuFile.add(menuItemStopTheGame);
        
        menuFile.add(new JSeparator());
        
        menuItemLoadLevelsSet = new JMenuItem("Load Levels Set...");
        menuItemLoadLevelsSet.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent ae) {
                
                loadLevelsSet();
            }
        });
        menuItemLoadLevelsSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuFile.add(menuItemLoadLevelsSet);
        
        menuItemLoadDefaultLevelsSet = new JMenuItem("Load Default Levels Set");
        menuItemLoadDefaultLevelsSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                
                loadDefaultLevelsSet();
            }
        });
        menuItemLoadDefaultLevelsSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        menuFile.add(menuItemLoadDefaultLevelsSet);
        
        menuFile.add(new JSeparator());
        
        menuItemExit = new JMenuItem("Exit");
        menuItemExit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                
                closeApplication();
            }
        });
        menuItemExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        menuFile.add(menuItemExit);
        
        menuBar.add(menuFile);
        
        // Creating action menu
        menuAction = new JMenu("Action");
        
        menuItemRestartLevel = new JMenuItem("Restart Level");
        menuItemRestartLevel.addActionListener(new ActionListener() {
        
            @Override
            public void actionPerformed(ActionEvent ae) {
            
                if (game != null && game.getLevelsSet().getCurrentLevel() != null) {
              
                    if (game.getLevelsSet().getCurrentLevel().getMovesCount() == 0) {
                        
                        game.restartLevel();
                        updateMenuItems();
                        return;
                    }
                    
                    int confirmResult = JOptionPane.showConfirmDialog(null,
                            "Are you sure that you want to restart the level?", "Restart Confirmation", JOptionPane.YES_NO_OPTION);
                    
                    if (confirmResult == JOptionPane.YES_OPTION) {
                        
                        game.restartLevel();
                        updateMenuItems();
                    }
                }
            }
        });
        menuItemRestartLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        menuAction.add(menuItemRestartLevel);
        
        menuAction.add(new JSeparator());
        
        menuItemPreviousLevel = new JMenuItem("Previous Level");
        menuItemPreviousLevel.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent ae) {
                
                if (game != null) {
                    
                    game.goToPreviousLevel();
                    updateMenuItems();
                }
            }
        });
        menuItemPreviousLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.ALT_MASK));
        menuAction.add(menuItemPreviousLevel);
        
        menuItemNextLevel = new JMenuItem("Next Level");
        menuItemNextLevel.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                
                if (game != null) {
                    
                    game.goToNextLevel();
                    updateMenuItems();
                }
            }
        });
        menuItemNextLevel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        menuAction.add(menuItemNextLevel);
        
        menuAction.add(new JSeparator());
        
        menuItemRepeatMove = new JMenuItem("Repeat Move");
        menuItemRepeatMove.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent e) {
                
                if (game != null) {
                    
                    game.repeatMove();
                    updateMenuItems();
                }
            }
        });
        menuItemRepeatMove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
        menuAction.add(menuItemRepeatMove);
        
        menuItemTakeBack = new JMenuItem("Take Back");
        menuItemTakeBack.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                if (game != null) {
                    
                    game.takeBack();
                    updateMenuItems();
                }
            }
        });
        menuItemTakeBack.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
        menuAction.add(menuItemTakeBack);
        
        menuItemMovesHistory = new JMenuItem("Moves History...");
        menuItemMovesHistory.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent e) {
                
                DesktopMovesHistoryDialog desktopMovesHistoryDialog =
                        new DesktopMovesHistoryDialog(DesktopGame.this, DesktopGame.this);
                desktopMovesHistoryDialog.setVisible(true);
            }
        });
        menuItemMovesHistory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
        menuAction.add(menuItemMovesHistory);
 
        menuBar.add(menuAction);
        
        // Creating tools menu
        menuTools = new JMenu("Tools");
        
        menuItemOptions = new JMenuItem("Options");
        menuItemOptions.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                DesktopOptionsDialog desktopOptionsDialog = new DesktopOptionsDialog(DesktopGame.this, DesktopGame.this);
                desktopOptionsDialog.setVisible(true);
            }
        });
        menuItemOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuTools.add(menuItemOptions);
        
        menuBar.add(menuTools);
        
        // Creating help menu
        menuHelp = new JMenu("Help");
        
        menuItemAbout = new JMenuItem("About...");
        menuItemAbout.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent ae) {

                try {
                    
                    AboutBoxInformation aboutBoxInformation = new DesktopAboutBoxInformation(applicationProperties);
                    aboutBoxInformation.addInformationLine("Vendor", aboutBoxInformation.getApplicationVendor(),
                            applicationProperties.getProperty("application.vendor.www"));
                    aboutBoxInformation.addInformationLine("Version", aboutBoxInformation.getApplicationVersion());
                    aboutBoxInformation.addInformationLine("Programming", applicationProperties.getProperty("application.author"),
                            String.format("mailto:%s", applicationProperties.getProperty("application.author.email")));
                    aboutBoxInformation.addInformationLine("Graphics", applicationProperties.getProperty("application.designer"),
                            applicationProperties.getProperty("application.designer.www"));
                    aboutBoxInformation.addInformationLine(null, applicationProperties.getProperty("application.author"),
                            String.format("mailto:%s", applicationProperties.getProperty("application.author.email")));
                    AboutBox aboutBox = new AboutBox(aboutBoxInformation);
                    if (windowIconURL != null)
                        aboutBox.setIconImage(new ImageIcon(windowIconURL).getImage());
                    aboutBox.setVisible(true);
                }
                catch (NullPointerException ex) {
                    
                    JOptionPane.showMessageDialog(null, "Unable to create about box due application's properties resource is corrupted.",
                            "Fatal Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menuItemAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuHelp.add(menuItemAbout);
        
        menuBar.add(menuHelp);
    }
    
    /**
     * Creates game's status bar.
     */
    protected void createStatusBar() {
        
        SpringLayout statusBarLayout = new SpringLayout();
        statusBar = new JPanel(statusBarLayout);
        statusBar.setBackground(desktopGameGraphics.getBackground());

        Font labelFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
        
        timeLabel = new JLabel(" ");
        timeLabel.setForeground(new Color(50, 230, 0));
        timeLabel.setFont(labelFont);
        
        levelInfoLabel = new JLabel(" ");
        levelInfoLabel.setForeground(new Color(255, 180, 60));
        levelInfoLabel.setFont(labelFont);
        
        movesCountLabel = new JLabel(" ");
        movesCountLabel.setForeground(new Color(235, 210, 0));
        movesCountLabel.setFont(labelFont);
        
        pushesCountLabel = new JLabel(" ");
        pushesCountLabel.setForeground(new Color(235, 210, 0));
        pushesCountLabel.setFont(labelFont);
        
        statusBar.add(timeLabel);
        statusBar.add(levelInfoLabel);
        statusBar.add(movesCountLabel);
        statusBar.add(pushesCountLabel);
        
        statusBarLayout.putConstraint(SpringLayout.WEST, timeLabel, 10, SpringLayout.WEST, statusBar);
        statusBarLayout.putConstraint(SpringLayout.NORTH, timeLabel, 4, SpringLayout.NORTH, statusBar);
        
        statusBarLayout.putConstraint(SpringLayout.NORTH, movesCountLabel, 0, SpringLayout.NORTH, timeLabel);
        statusBarLayout.putConstraint(SpringLayout.EAST, movesCountLabel, -10, SpringLayout.EAST, statusBar);
        
        statusBarLayout.putConstraint(SpringLayout.WEST, levelInfoLabel, 0, SpringLayout.WEST, timeLabel);
        statusBarLayout.putConstraint(SpringLayout.NORTH, levelInfoLabel, 1, SpringLayout.SOUTH, timeLabel);
        
        statusBarLayout.putConstraint(SpringLayout.NORTH, pushesCountLabel, 0, SpringLayout.NORTH, levelInfoLabel);
        statusBarLayout.putConstraint(SpringLayout.EAST, pushesCountLabel, 0, SpringLayout.EAST, movesCountLabel);
        
        statusBarLayout.putConstraint(SpringLayout.SOUTH, statusBar, 2, SpringLayout.SOUTH, levelInfoLabel);
    }
    
    /**
     * Retrieves a reference to game's instance.
     * 
     * @return 
     *      Reference to game's instance.
     */
    public Game getGameInstance() {
        
        return game;
    }
    
    /**
     * Loads default levels' set.
     */
    protected void loadDefaultLevelsSet() {
        
        GameState currentGameState = game.getGameState();
        
        // Loading default levels' set
        if (!game.loadDefaultLevelsSet()) {

            JOptionPane.showMessageDialog(null, "Unable to load default levels set.",
                    "Open Error", JOptionPane.ERROR_MESSAGE);
            updateMenuItems();
            return;
        }
        
        // Disabling game's window
        setVisible(false);
        
        // Analyzing loaded levels' set (and rebuilding the window)
        if (analyzeLoadedLevelsSet()) {
            
            // Selecting first playable level
            game.getLevelsSet().setCurrentLevelByFirstPlayable();
            
            if (currentGameState == GameState.PLAY)
                game.startLevel(game.getLevelsSet().getCurrentLevelIndex());
            else
                game.stop(true);
        }
        
        // Making game's window visible
        setVisible(true);
    }
    
    /**
     * Provides a dialog to select levels' set to load and loads selected levels' set.
     */
    protected void loadLevelsSet() {
    
        if (game == null)
            return;
                    
        // Browsing for a levels' set files
        CompoundFileFilter levelsSetsFilter = new CompoundFileFilter();
        levelsSetsFilter.add(new FileNameExtensionFilter("Storekeeper levels sets (*.xml)", "xml"));
        levelsSetsFilter.add(new FileNameExtensionFilter("Sokoban levels files (*.sok)", "sok"));
        levelsSetsFilter.add(new FileNameExtensionFilter("All supported levels files (*.xml, *.sok)", "xml", "sok"));
        levelsSetsFilter.setDefaultFileFilterIndex(levelsSetsFilter.getFileFilters().size() - 1);
        File selectedFile = FileBrowser.browseFile(ApplicationPath.getApplicationPath(DesktopGame.class),
                "Please, select levels set file...", levelsSetsFilter);

        // Checking whether levels' set file has been selected
        if (selectedFile == null) {
         
            updateMenuItems();
            return;
        }

        if (!selectedFile.exists() && !selectedFile.isFile()) {

            JOptionPane.showMessageDialog(null, String.format("File \"%s\" doesn't exist.", selectedFile.getAbsolutePath()),
                    "Open Error", JOptionPane.ERROR_MESSAGE);
            updateMenuItems();
            return;
        }
        
        GameState currentGameState = game.getGameState();

        // Retrieving a name of selected levels' file.
        String levelsSetFileName = selectedFile.getAbsolutePath();
        if (!game.loadLevelsSet(levelsSetFileName)) {

            JOptionPane.showMessageDialog(null, String.format("Unable to load levels' set \"%s\".", levelsSetFileName,
                    "Open Error", JOptionPane.ERROR_MESSAGE));
            updateMenuItems();
            return;
        }
        
        // Disabling game's window
        setVisible(false);
        
        // Analyzing loaded levels' set (and rebuilding the window)
        if (analyzeLoadedLevelsSet()) {
            
            // Selecting first playable level
            game.getLevelsSet().setCurrentLevelByFirstPlayable();
            
            if (currentGameState == GameState.PLAY)
                game.startLevel(game.getLevelsSet().getCurrentLevelIndex());
            else
                game.stop(true);
        }
        
        // Making game's window visible
        setVisible(true);
    }
    
    /**
     * Analyzes loaded levels' set and rebuilds game's window on success.
     * 
     * @return 
     *      {@code true} if levels' set is playable, {@code false} otherwise.
     * @see #rebuildGameWindow()
     */
    protected boolean analyzeLoadedLevelsSet() {
        
        // Determining a size of loaded levels' field
        LevelSize maximalLevelSize = game.getLevelsSet().getMaximalLevelSize();
        
        int playableLevelsCount = game.getLevelsSet().getPlayableLevelsCount();
        int levelsCount = game.getLevelsSet().getLevelsCount();
        if (playableLevelsCount == 0) {
        
            if (!game.isDefaultLevelsSetLoaded()) {
                
                JOptionPane.showMessageDialog(null, "Loaded levels' set has no any playable levels.\n"
                        + "Default levels' set will be loaded now.", "Levels' Set Error", JOptionPane.ERROR_MESSAGE);
                loadDefaultLevelsSet();
                return false;
            }
            else {
                
                JOptionPane.showMessageDialog(null, "Default levels' set has no any playable levels.\n",
                        "Levels' Set Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        else if (playableLevelsCount < levelsCount) {
            
            JOptionPane.showMessageDialog(null, String.format("Only %d level(s) of %d levels is(are) playable "
                    + "in loaded levels' set.", playableLevelsCount, levelsCount), "Levels' Set Warning",
                    JOptionPane.WARNING_MESSAGE);
        }               

        // Determining sprite's size
        SpriteSize spriteSize = desktopGameGraphics.determineOptimalSpriteSize(maximalLevelSize,
                0, menuBar.getPreferredSize().height + statusBar.getPreferredSize().height + VERTICAL_PADDING);
        if (spriteSize == null) {

            JOptionPane.showMessageDialog(null, "Your screen resolution is too small to play just loaded levels' set.\n"
                    + "Default levels' set will be loaded now.", "Screen Resolution Warning", JOptionPane.WARNING_MESSAGE);

            loadDefaultLevelsSet();
            return false;
        }

        // Applying sprite's size
        if (!desktopGameGraphics.getSpriteSize().equals(spriteSize))
            desktopGameGraphics.setSpriteSize(spriteSize);

        // Rebuilding game's window if it's required
        rebuildGameWindow();
        return true;
    }
    
    /**
     * Rebuilds game's window according to currently set
     * level's width and height parameters.
     */
    protected void rebuildGameWindow() {
        
        // Stopping the game if it's required
        game.stop(false);
        
        // Hiding game's window
        setVisible(false);
        
        // Retrieving a reference to game graphics instance
        GameGraphics gameGraphics = game.getGameGraphics();
        
        // Retrieving maximal level size of the set
        LevelSize maximalLevelSize = game.getLevelsSet().getMaximalLevelSize();
        
        // Retrieving window's content pane and its layout
        JPanel contentPane = (JPanel)getContentPane();
        SpringLayout contentLayout = (SpringLayout)contentPane.getLayout();
     
        // Applying actual game field's constraints
        boolean isWindowSizeChanged = false;
        Spring currentEastSpring = contentLayout.getConstraint(SpringLayout.EAST, game);
        Spring currentSouthSpring = contentLayout.getConstraint(SpringLayout.SOUTH, game);
        int newGameWidth = gameGraphics.getSpriteDimension().width * maximalLevelSize.getWidth();
        int newGameHeight = gameGraphics.getSpriteDimension().height * maximalLevelSize.getHeight();
        if (currentEastSpring.getValue() != newGameWidth) {
            
            contentLayout.putConstraint(SpringLayout.EAST, game, newGameWidth, SpringLayout.WEST, game);
            contentLayout.putConstraint(SpringLayout.EAST, statusBar, 0, SpringLayout.EAST, game);
            contentLayout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, statusBar);
            isWindowSizeChanged = true;
        }
        if (currentSouthSpring.getValue() != newGameHeight) {
            
            contentLayout.putConstraint(SpringLayout.SOUTH, game, newGameHeight, SpringLayout.NORTH, game);
            contentLayout.putConstraint(SpringLayout.NORTH, statusBar, 0, SpringLayout.SOUTH, game);
            contentLayout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, statusBar);
            isWindowSizeChanged = true;
        }
        
        if (isWindowSizeChanged) {
            
            pack();
            centerTheWindow();
        }               
        
        updateMenuItems();
    }
    
    /**
     * Updates application's menu items' access state.
     */
    protected final void updateMenuItems() {
        
        if (game == null)
            return;
        
        boolean isGameStopped = game.getGameState() == Game.GameState.INTRODUCTION || game.getGameState() == Game.GameState.STOP;
        boolean isLevelsSetLoaded = game.isLevelsSetLoaded();
        Level currentGameLevel = isLevelsSetLoaded ? game.getLevelsSet().getCurrentLevel() : null;
        int playableLevelsCount = isLevelsSetLoaded ? game.getLevelsSet().getPlayableLevelsCount() : 0;
        
        menuItemStartTheGame.setEnabled(isGameStopped && isLevelsSetLoaded && playableLevelsCount > 0);
        menuItemStopTheGame.setEnabled(!isGameStopped && isLevelsSetLoaded && playableLevelsCount > 0);
        menuItemLoadDefaultLevelsSet.setEnabled(!game.isDefaultLevelsSetLoaded());
        
        menuItemRestartLevel.setEnabled(!isGameStopped && isLevelsSetLoaded);
        menuItemPreviousLevel.setEnabled(isLevelsSetLoaded && playableLevelsCount > 1);
        menuItemNextLevel.setEnabled(isLevelsSetLoaded && playableLevelsCount > 1);
        menuItemRepeatMove.setEnabled(currentGameLevel != null ?
                !isGameStopped && currentGameLevel.getMovesHistoryCount() - currentGameLevel.getMovesCount() > 0 : false);
        menuItemTakeBack.setEnabled(currentGameLevel != null ?
                !isGameStopped && currentGameLevel.getMovesCount() > 0 : false);
        menuItemMovesHistory.setEnabled(currentGameLevel != null && currentGameLevel.getMovesCount() > 0);
    }
    
    /**
     * Centers game's window.
     */
    protected void centerTheWindow() {
        
        setLocationRelativeTo(null);
    }
    
    /**
     * Fires application' close event.
     */
    protected void closeApplication() {
        
        WindowListener[] windowListeners = getWindowListeners();
        if (windowListeners.length > 0) {
            
            for (WindowListener windowListener : windowListeners)
                windowListener.windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    /**
     * Implements uninitialization actions on application's closing.
     */
    protected void onCloseApplication() {
        
        if (game != null) {
            
            if (game.getGameState() == Game.GameState.PLAY || game.getGameState() == Game.GameState.COMPLETED) {
         
                if (game.getLevelsSet().getCurrentLevel() != null &&
                        game.getLevelsSet().getCurrentLevel().getMovesCount() > 0) {

                    int confirmResult = JOptionPane.showConfirmDialog(null, "Are you sure that you want to exit the game?",
                            "Exit Confirmation", JOptionPane.YES_NO_OPTION);
                    if (confirmResult != JOptionPane.YES_OPTION)
                        return;
                }

                game.stop();
            }
            
            // Saving game's current settings
            game.getGameConfiguration().save();
        }
        
        dispose();
    }
}