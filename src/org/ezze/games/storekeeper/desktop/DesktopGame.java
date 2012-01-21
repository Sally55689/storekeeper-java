package org.ezze.games.storekeeper.desktop;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.ezze.utils.application.ApplicationPath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import org.ezze.games.storekeeper.Game;
import org.ezze.games.storekeeper.Game.LevelResult;
import org.ezze.games.storekeeper.GameConfiguration;
import org.ezze.games.storekeeper.GameGraphics.SpriteSize;
import org.ezze.games.storekeeper.GameLevel;
import org.ezze.games.storekeeper.GameLevelCompletionListener;
import org.ezze.utils.ui.FileBrowser;
import org.ezze.utils.ui.aboutbox.AboutBox;
import org.ezze.utils.ui.aboutbox.AboutBoxInformation;

/**
 * @author Dmitriy Pushkov
 * @version 0.0.2
 */
public class DesktopGame extends JFrame {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        new DesktopGame();
    }
    
    /**
     * Self-referencing.
     */
    private DesktopGame desktopGame = this;
    
    /**
     * Game instance.
     */
    private Game game = null;
    
    /**
     * File menu instance.
     */
    private JMenu menuFile = null;
    
    /**
     * File menu item to load levels' set.
     */
    private JMenuItem menuItemLoadLevelsSet = null;
    
    /**
     * File menu item to load default levels' set.
     */
    private JMenuItem menuItemLoadDefaultLevelsSet = null;
    
    /**
     * File menu item to exit the game.
     */
    private JMenuItem menuItemExit = null;
    
    /**
     * Action menu instance.
     */
    private JMenu menuAction = null;
    
    /**
     * Action menu item to start the game.
     */
    private JMenuItem menuItemStartTheGame = null;
    
    /**
     * Action menu item to stop the game.
     */
    private JMenuItem menuItemStopTheGame = null;
    
    /**
     * Action menu item to restart game's current level.
     */
    private JMenuItem menuItemRestartLevel = null;
    
    /**
     * Action menu item to jump to game's previous level.
     */
    private JMenuItem menuItemPreviousLevel = null;
    
    /**
     * Action menu item to jump to game's next level.
     */
    private JMenuItem menuItemNextLevel = null;
    
    /**
     * Tools menu instance.
     */
    private JMenu menuTools = null;
    
    /**
     * Tools menu item to show options dialog.
     */
    private JMenuItem menuItemOptions = null;
    
    /**
     * Help menu instance.
     */
    private JMenu menuHelp = null;
    
    /**
     * Help menu item to show about box.
     */
    private JMenuItem menuItemAbout = null;
    
    /**
     * Storekeeper game's desktop implementation main class.
     * 
     * This class is used as main one to build the desktop version of the game.
     */
    public DesktopGame() {
        
        try {
            
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
        String configurationFileName = String.format("%s/storekeeperConfig.xml", ApplicationPath.getApplicationPath(DesktopGame.class));
        GameConfiguration gameConfiguration = new GameConfiguration(configurationFileName);
        
        // Retrieving maximal width (columns) and height (rows) of game level field (in level items)
        int levelFieldColumnsCount = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_WIDTH,
                GameConfiguration.DEFAULT_OPTION_LEVEL_WIDTH);
        int levelFieldRowsCount = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_HEIGHT,
                GameConfiguration.DEFAULT_OPTION_LEVEL_HEIGHT);

        // Creating game instance
        DesktopGameGraphics desktopGameGraphics = new DesktopGameGraphics();
        SpriteSize optimalSpriteSize = desktopGameGraphics.determineOptimalSpriteSize(levelFieldColumnsCount, levelFieldRowsCount);
        desktopGameGraphics.setSpriteSize(optimalSpriteSize != null ? optimalSpriteSize : SpriteSize.SMALL);
        game = new Game(gameConfiguration, desktopGameGraphics, new GameLevelCompletionListener() {
                
            @Override
            public void levelCompleted(GameLevel gameLevel) {
                
                // Informing the user that the level has been successfully completed
                JOptionPane.showMessageDialog(null, "Level has been successfully completed!", "Congratulations", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JPanel contentPane = new JPanel();
        SpringLayout contentLayout = new SpringLayout();
        contentPane.setLayout(contentLayout);
        
        contentPane.add(game);
        contentLayout.putConstraint(SpringLayout.WEST, game, 0, SpringLayout.WEST, contentPane);
        contentLayout.putConstraint(SpringLayout.NORTH, game, 0, SpringLayout.NORTH, contentPane);
        contentLayout.putConstraint(SpringLayout.EAST, game,
                game.getGameGraphics().getSpriteDimension().width * levelFieldColumnsCount, SpringLayout.WEST, game);
        contentLayout.putConstraint(SpringLayout.SOUTH, game,
                (game.getGameGraphics().getSpriteDimension().height) * levelFieldRowsCount, SpringLayout.NORTH, game);
        
        contentLayout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, game);
        contentLayout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, game);
                
        setContentPane(contentPane);
        setResizable(false);
        
        // Creating menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // Creating file menu
        menuFile = new JMenu("File");
        
        menuItemStartTheGame = new JMenuItem("Start The Game");
        menuItemStartTheGame.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent ae) {
                
                if (game != null && game.getGameState() != Game.GameState.PLAY && game.getGameState() != Game.GameState.COMPLETED) {
                    
                    game.startLevel(game.getCurrentGameLevelIndex());
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
                
                if (game != null) {
                    
                    // Browsing for a levels' set files
                    File selectedFile = FileBrowser.browseFile(ApplicationPath.getApplicationPath(DesktopGame.class),
                            "Please, select levels set file...", new FileFilter() {

                        @Override
                        public boolean accept(File file) {
                            
                            return file.isFile() && file.getName().toLowerCase().endsWith(".xml");
                        }

                        @Override
                        public String getDescription() {
                            
                            return "Levels set files (*.xml)";
                        }
                    });
                    
                    // Checking whether levels' set file has been selected
                    if (selectedFile == null)
                        return;
                    
                    if (!selectedFile.exists() && !selectedFile.isFile()) {
                        
                        JOptionPane.showMessageDialog(null, String.format("File \"%s\" doesn't exist.", selectedFile.getAbsolutePath()),
                                "Open Error", JOptionPane.ERROR_MESSAGE);
                        updateMenuItems();
                        return;
                    }
                    
                    String levelsSetFileName = selectedFile.getAbsolutePath();
                    Game.LevelResult loadResult = game.loadLevelsSet(levelsSetFileName);
                    if (loadResult == Game.LevelResult.ERROR) {
                        
                        JOptionPane.showMessageDialog(null, String.format("Unable to parse \"%s\" as levels set file.", levelsSetFileName),
                                "Open Error", JOptionPane.ERROR_MESSAGE);
                        updateMenuItems();
                        return;
                    }
                    else if (loadResult == Game.LevelResult.WARNING) {
                        
                        JOptionPane.showMessageDialog(null, String.format("At least one level of set \"%s\" cannot be initialized.", levelsSetFileName),
                                "Open Warning", JOptionPane.WARNING_MESSAGE);
                    }
                    
                    game.startLevel(game.getCurrentGameLevelIndex());
                    updateMenuItems();
                }
            }
        });
        menuItemLoadLevelsSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuFile.add(menuItemLoadLevelsSet);
        
        menuItemLoadDefaultLevelsSet = new JMenuItem("Load Default Levels Set");
        menuItemLoadDefaultLevelsSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                
                if (game != null) {
                    
                    if (game.loadDefaultLevelsSet() == Game.LevelResult.ERROR)
                        JOptionPane.showMessageDialog(null, "Unable to load default levels set.", "Open Error", JOptionPane.ERROR_MESSAGE);
                    else
                        game.startLevel(game.getCurrentGameLevelIndex());
                    updateMenuItems();
                }
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
            
                if (game != null) {
                    
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
 
        menuBar.add(menuAction);
        
        // Creating tools menu
        menuTools = new JMenu("Tools");
        
        menuItemOptions = new JMenuItem("Options");
        menuItemOptions.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                DesktopOptionsDialog desktopOptionsDialog = new DesktopOptionsDialog(desktopGame, desktopGame);
                desktopOptionsDialog.setVisible(true);
                if (desktopOptionsDialog.isGameWindowRebuildRequired())
                    rebuildGameWindow();
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
        
        setJMenuBar(menuBar);
        
        pack();
        setLocationRelativeTo(null);
        
        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {

                if (game.getGameState() == Game.GameState.PLAY) {

                    if (e.getID() == KeyEvent.KEY_PRESSED) {

                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {

                            game.forceWorkerToMoveLeft();
                            return true;
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

                            game.forceWorkerToMoveRight();
                            return true;
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_UP) {

                            game.forceWorkerToMoveUp();
                            return true;
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {

                            game.forceWorkerToMoveDown();
                            return true;
                        }
                    }
                    else if (e.getID() == KeyEvent.KEY_RELEASED) {

                        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            
                            game.forceWorkerToStopHorizontalMovement();
                            return true;
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                            
                            game.forceWorkerToStopVerticalMovement();
                            return true;
                        }
                    }
                }

                return false;
            }
        });
        
        if (game.loadDefaultLevelsSet() == Game.LevelResult.ERROR)
            JOptionPane.showMessageDialog(null, "Unable to load default levels set.", "Open Error", JOptionPane.ERROR_MESSAGE);
        
        updateMenuItems();
        
        setVisible(true);
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
     * Rebuilds game's window according to currently set
     * level's width and height parameters.
     */
    private void rebuildGameWindow() {
        
        // Stopping the game if it's required
        game.stop(false);
        
        // Hiding game's window
        setVisible(false);
        
        // Retrieving a reference to game configuration instance
        GameConfiguration gameConfiguration = game.getGameConfiguration();
        
        // Retrieving window's content pane and its layout
        JPanel contentPane = (JPanel)getContentPane();
        SpringLayout contentLayout = (SpringLayout)contentPane.getLayout();
        
        // Applying actual game field's constraints
        int levelFieldColumnsCount = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_WIDTH,
                GameConfiguration.DEFAULT_OPTION_LEVEL_WIDTH);
        int levelFieldRowsCount = (Integer)gameConfiguration.getOption(GameConfiguration.OPTION_LEVEL_HEIGHT,
                GameConfiguration.DEFAULT_OPTION_LEVEL_HEIGHT);
        contentLayout.putConstraint(SpringLayout.EAST, game,
                game.getGameGraphics().getSpriteDimension().width * levelFieldColumnsCount, SpringLayout.WEST, game);
        contentLayout.putConstraint(SpringLayout.SOUTH, game,
                (game.getGameGraphics().getSpriteDimension().height) * levelFieldRowsCount, SpringLayout.NORTH, game);
        
        // Adjusting size of the content pane
        contentLayout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, game);
        contentLayout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, game);
        
        pack();
        setLocationRelativeTo(null);
        
        // Reinitializing the levels
        LevelResult reinitializationResult = game.reinitializeLevels();
        if (reinitializationResult == LevelResult.ERROR) {
            
            JOptionPane.showMessageDialog(null, "Unable to reinitialize all levels of currently loaded levels set. "
                    + "Please be sure that selected maximal size of a level is enough to make all levels fit.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        else if (reinitializationResult == LevelResult.WARNING) {
            
            JOptionPane.showMessageDialog(null, "At least one level of currently loaded levels set cannot be reinitialized. "
                    + "Please be sure that selected maximal size of a level is enough to make all levels fit.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
        
        updateMenuItems();
        setVisible(true);
    }
    
    /**
     * Updates application's menu items' access state.
     */
    private void updateMenuItems() {
        
        if (game == null)
            return;
        
        boolean isGameStopped = game.getGameState() == Game.GameState.INTRODUCTION || game.getGameState() == Game.GameState.STOP;
        boolean isLevelsSetLoaded = game.isLevelsSetLoaded();
        
        menuItemStartTheGame.setEnabled(isGameStopped && isLevelsSetLoaded);
        menuItemStopTheGame.setEnabled(!isGameStopped && isLevelsSetLoaded);
        menuItemLoadDefaultLevelsSet.setEnabled(!game.isDefaultLevelsSetLoaded());
        
        menuItemRestartLevel.setEnabled(!isGameStopped && isLevelsSetLoaded);
        menuItemPreviousLevel.setEnabled(isLevelsSetLoaded && game.getLevelsCount() > 1);
        menuItemNextLevel.setEnabled(isLevelsSetLoaded && game.getLevelsCount() > 1);
    }
    
    /**
     * Fires application' close event.
     */
    private void closeApplication() {
        
        WindowListener[] windowListeners = getWindowListeners();
        if (windowListeners.length > 0) {
            
            for (WindowListener windowListener : windowListeners)
                windowListener.windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    /**
     * Implements uninitialization actions on application's closing.
     */
    private void onCloseApplication() {
        
        if (game != null) {
         
            if (game.getGameState() == Game.GameState.PLAY || game.getGameState() == Game.GameState.COMPLETED) {
                
                int confirmResult = JOptionPane.showConfirmDialog(null, "Are you sure that you want to exit the game?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirmResult != JOptionPane.YES_OPTION)
                    return;
                
                game.stop();
            }
            
        }
        
        // Saving game's current settings
        game.getGameConfiguration().save();
        
        dispose();
    }
}