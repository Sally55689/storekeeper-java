package org.ezze.games.storekeeper.desktop;

import javax.swing.UnsupportedLookAndFeelException;
import org.ezze.utils.application.ApplicationPath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import org.ezze.games.storekeeper.Game;
import org.ezze.games.storekeeper.Game.LevelResult;
import org.ezze.games.storekeeper.Configuration;
import org.ezze.games.storekeeper.Game.GameState;
import org.ezze.games.storekeeper.GameGraphics;
import org.ezze.games.storekeeper.GameGraphics.SpriteSize;
import org.ezze.games.storekeeper.Level;
import org.ezze.games.storekeeper.Level.LevelState;
import org.ezze.games.storekeeper.LevelCompletionListener;
import org.ezze.utils.ui.FileBrowser;
import org.ezze.utils.ui.aboutbox.AboutBox;
import org.ezze.utils.ui.aboutbox.AboutBoxInformation;

/**
 * Desktop version of the game.
 * 
 * @author Dmitriy Pushkov
 * @version 0.0.3
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
     * Action menu item to take game level's position back by one move.
     */
    private JMenuItem menuItemTakeBack = null;
    
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
        String configurationFileName = String.format("%s/storekeeperConfig.xml", ApplicationPath.getApplicationPath(DesktopGame.class));
        Configuration gameConfiguration = new Configuration(configurationFileName);
        
        // Creating game graphics' instance
        DesktopGameGraphics desktopGameGraphics = new DesktopGameGraphics();
        
        // Retrieving maximal width (columns) and height (rows) of game level field (in level items)
        int maximalLevelWidth = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
        int maximalLevelHeight = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        
        // Retrieving sprite size option and determining
        String spriteSizeOption = (String)gameConfiguration.getOption(Configuration.OPTION_SPRITE_SIZE,
                Configuration.DEFAULT_OPTION_SPRITE_SIZE);
        
        // Determining sprite size
        SpriteSize spriteSize = null;
        if (spriteSizeOption.equals(Configuration.OPTION_SPRITE_SIZE_OPTIMAL))
            spriteSize = desktopGameGraphics.determineOptimalSpriteSize(maximalLevelWidth, maximalLevelHeight);
        else if (spriteSizeOption.equals(Configuration.OPTION_SPRITE_SIZE_LARGE))
            spriteSize = SpriteSize.LARGE;
        else if (spriteSizeOption.equals(Configuration.OPTION_SPRITE_SIZE_MEDIUM))
            spriteSize = SpriteSize.MEDIUM;
        else
            spriteSize = SpriteSize.SMALL;

        // Creating game instance
        desktopGameGraphics.setSpriteSize(spriteSize != null ? spriteSize : SpriteSize.SMALL);
        game = new Game(gameConfiguration, desktopGameGraphics, new LevelCompletionListener() {
                
            @Override
            public void levelCompleted(Level gameLevel) {
                
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
                game.getGameGraphics().getSpriteDimension().width * maximalLevelWidth, SpringLayout.WEST, game);
        contentLayout.putConstraint(SpringLayout.SOUTH, game,
                (game.getGameGraphics().getSpriteDimension().height) * maximalLevelHeight, SpringLayout.NORTH, game);
        
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
                    
                    game.startLevel(game.getLevelsSet().getCurrentLevelIndex());
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
                        game.startLevel(game.getLevelsSet().getCurrentLevelIndex());
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
        
        menuAction.add(new JSeparator());
        
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

        // Loading default levels' set
        LevelResult loadLevelResult = game.loadDefaultLevelsSet();
        showLoadLevelResultMessage(loadLevelResult, maximalLevelWidth, maximalLevelHeight);
        
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
        Configuration gameConfiguration = game.getGameConfiguration();
        
        // Retrieving a reference to game graphics instance
        GameGraphics gameGraphics = game.getGameGraphics();
        
        // Retrieving window's content pane and its layout
        JPanel contentPane = (JPanel)getContentPane();
        SpringLayout contentLayout = (SpringLayout)contentPane.getLayout();
        
        // Retrieving actual game field's parameters
        int maximalLevelWidth = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_WIDTH,
                Configuration.DEFAULT_OPTION_LEVEL_WIDTH);
        int maximalLevelHeight = (Integer)gameConfiguration.getOption(Configuration.OPTION_LEVEL_HEIGHT,
                Configuration.DEFAULT_OPTION_LEVEL_HEIGHT);
        String spriteSizeOption = (String)gameConfiguration.getOption(Configuration.OPTION_SPRITE_SIZE,
                Configuration.DEFAULT_OPTION_SPRITE_SIZE);
        
        // Determining sprite size
        SpriteSize optimalSpriteSize = gameGraphics.determineOptimalSpriteSize(maximalLevelWidth, maximalLevelHeight);
        SpriteSize spriteSize = null;
        if (!spriteSizeOption.equals(Configuration.OPTION_SPRITE_SIZE_OPTIMAL)) {
            
            if (spriteSizeOption.equals(Configuration.OPTION_SPRITE_SIZE_LARGE))
                spriteSize = SpriteSize.LARGE;
            else if (spriteSizeOption.equals(Configuration.OPTION_SPRITE_SIZE_MEDIUM))
                spriteSize = SpriteSize.MEDIUM;
            else
                spriteSize = SpriteSize.SMALL;
            
            // Checking whether determined size is no more than optimal one
            SpriteSize[] spriteSizeValues = SpriteSize.values();
            int optimalSpriteSizeIndex = -1;
            int selectedSpriteSizeIndex = -1;
            int spriteSizeIndex = 0;
            while ((optimalSpriteSizeIndex < 0 || selectedSpriteSizeIndex < 0) && spriteSizeIndex < spriteSizeValues.length) {
                
                if (spriteSizeValues[spriteSizeIndex] == optimalSpriteSize)
                    optimalSpriteSizeIndex = spriteSizeIndex;
                if (spriteSizeValues[spriteSizeIndex] == spriteSize)
                    selectedSpriteSizeIndex = spriteSizeIndex;
                spriteSizeIndex++;
            }
            
            if (selectedSpriteSizeIndex < optimalSpriteSizeIndex) {
                
                int spriteSizeConfirmation = JOptionPane.showConfirmDialog(null,
                        "Selected maximal level's width and height and sprite size cannot provide game's window\n"
                        + "to be gone in the screen with currently set resolution. Do you want an optimal sprite\n"
                        + "size to be applied?", "Sprite size confirmation", JOptionPane.YES_NO_OPTION);
                if (spriteSizeConfirmation == JOptionPane.YES_OPTION) {
                    
                    spriteSize = optimalSpriteSize;
                    gameConfiguration.setOption(Configuration.OPTION_SPRITE_SIZE, Configuration.OPTION_SPRITE_SIZE_OPTIMAL);
                }
            }
        }
        else {
            
            spriteSize = optimalSpriteSize;
        }
        
        // Applying sprite size
        if (!gameGraphics.getSpriteSize().equals(spriteSize))
            gameGraphics.setSpriteSize(spriteSize);
     
        // Applying actual game field's constraints
        boolean isWindowSizeChanged = false;
        Spring currentEastSpring = contentLayout.getConstraint(SpringLayout.EAST, game);
        Spring currentSouthSpring = contentLayout.getConstraint(SpringLayout.SOUTH, game);
        int newGameWidth = gameGraphics.getSpriteDimension().width * maximalLevelWidth;
        int newGameHeight = gameGraphics.getSpriteDimension().height * maximalLevelHeight;
        if (currentEastSpring.getValue() != newGameWidth) {
            
            contentLayout.putConstraint(SpringLayout.EAST, game, newGameWidth, SpringLayout.WEST, game);
            contentLayout.putConstraint(SpringLayout.EAST, contentPane, 0, SpringLayout.EAST, game);
            isWindowSizeChanged = true;
        }
        if (currentSouthSpring.getValue() != newGameHeight) {
            
            contentLayout.putConstraint(SpringLayout.SOUTH, game, newGameHeight, SpringLayout.NORTH, game);
            contentLayout.putConstraint(SpringLayout.SOUTH, contentPane, 0, SpringLayout.SOUTH, game);
            isWindowSizeChanged = true;
        }
        
        if (isWindowSizeChanged) {
            
            pack();
            centerTheWindow();
        }
        
        // Reinitializing the levels
        LevelResult reinitializationResult = game.reinitializeLevels();
        showLoadLevelResultMessage(reinitializationResult, maximalLevelWidth, maximalLevelHeight);
        
        updateMenuItems();
        setVisible(true);
    }
    
    /**
     * Shows a result message of levels' load or reinitialization result.
     * 
     * @param loadLevelResult
     *      Result identifier.
     * @param maximalLevelWidth
     *      Level's maximal width.
     * @param maximalLevelHeight
     *      Level's maximal height.
     */
    private void showLoadLevelResultMessage(LevelResult loadLevelResult, int maximalLevelWidth, int maximalLevelHeight) {
        
        if (loadLevelResult == LevelResult.ERROR) {
            
            JOptionPane.showMessageDialog(null, "Unable to load default levels set.", "Open Error", JOptionPane.ERROR_MESSAGE);
        }
        else if (loadLevelResult == LevelResult.WARNING) {
            
            int playableLevelsCount = game.getLevelsSet().getPlayableLevelsCount();
            int outOfBoundsLevelsCount = game.getLevelsSet().getLevelsCountByState(LevelState.OUT_OF_BOUNDS);
            int levelsCount = game.getLevelsSet().getLevelsCount();
            
            String warningMessage = "";
            if (playableLevelsCount == 0)
                warningMessage += String.format("No one of %d levels is playable", levelsCount);
            else
                warningMessage += String.format("Only %d of %d levels %s playable", playableLevelsCount,
                        levelsCount, playableLevelsCount > 1 ? "are" : "is");
            warningMessage += String.format(" with currently selected level's\n"
                    + "maximal size (%d rows x %d columns). %d of non-playable levels are out of level's bounds\n"
                    + "so you can play them if you will increase level's maximal allowed size in options dialog.",
                    maximalLevelHeight, maximalLevelWidth, outOfBoundsLevelsCount);
            JOptionPane.showMessageDialog(null, warningMessage, "Open Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Updates application's menu items' access state.
     */
    private void updateMenuItems() {
        
        if (game == null)
            return;
        
        boolean isGameStopped = game.getGameState() == Game.GameState.INTRODUCTION || game.getGameState() == Game.GameState.STOP;
        boolean isLevelsSetLoaded = game.isLevelsSetLoaded();
        Level currentGameLevel = game.getLevelsSet().getCurrentLevel();
        int playableLevelsCount = game.getLevelsSet().getPlayableLevelsCount();
        
        menuItemStartTheGame.setEnabled(isGameStopped && isLevelsSetLoaded && playableLevelsCount > 0);
        menuItemStopTheGame.setEnabled(!isGameStopped && isLevelsSetLoaded && playableLevelsCount > 0);
        menuItemLoadDefaultLevelsSet.setEnabled(!game.isDefaultLevelsSetLoaded());
        
        menuItemRestartLevel.setEnabled(!isGameStopped && isLevelsSetLoaded);
        menuItemPreviousLevel.setEnabled(isLevelsSetLoaded && playableLevelsCount > 1);
        menuItemNextLevel.setEnabled(isLevelsSetLoaded && playableLevelsCount > 1);
        menuItemTakeBack.setEnabled(currentGameLevel != null ? !isGameStopped && currentGameLevel.getMovesCount() > 0 : false);
    }
    
    /**
     * Centers game's window.
     */
    private void centerTheWindow() {
        
        setLocationRelativeTo(null);
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