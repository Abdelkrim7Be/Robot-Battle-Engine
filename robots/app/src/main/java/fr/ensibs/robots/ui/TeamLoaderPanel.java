package fr.ensibs.robots.ui;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.impl.AppLog;
import fr.ensibs.robots.impl.RobotLoader;
import fr.ensibs.robots.impl.TeamInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for loading teams from JAR files and configuring battles.
 *
 * <p>Provides UI for:
 * <ul>
 *   <li>Loading team JAR files</li>
 *   <li>Displaying available teams</li>
 *   <li>Adding teams to battle with color selection</li>
 *   <li>Starting the battle</li>
 * </ul>
 */
public class TeamLoaderPanel extends JPanel implements ActionListener
{
    private static final String LOAD_JAR = "Load Team JAR";
    private static final String ADD_TO_BATTLE = "Add to Battle";
    private static final String REMOVE = "Remove";
    private static final String START_BATTLE = "START BATTLE";

    private final RobotLoader robotLoader;
    private final BattleFactory battleFactory;
    private final RobotTaskFactory taskFactory;

    // UI Components
    private DefaultListModel<String> availableTeamsModel;
    private JList<String> availableTeamsList;
    private DefaultListModel<String> battleTeamsModel;
    private JList<String> battleTeamsList;
    private JComboBox<String> colorComboBox;
    private JButton startBattleButton;

    // Data
    private final List<TeamInfo> availableTeams;
    private final List<TeamInfo> battleTeams;
    private final Color[] availableColors;

    // Callback for starting battle
    private BattleStartCallback battleStartCallback;

    /**
     * Callback interface for when battle starts.
     */
    public interface BattleStartCallback
    {
        /**
         * Called when START BATTLE is clicked.
         *
         * @param teams the list of teams to battle
         */
        void onBattleStart(List<TeamInfo> teams);
    }

    /**
     * Constructor
     *
     * @param robotLoader the robot loader
     * @param battleFactory the battle factory
     * @param taskFactory the task factory
     */
    public TeamLoaderPanel(RobotLoader robotLoader, BattleFactory battleFactory, RobotTaskFactory taskFactory)
    {
        super(new BorderLayout(10, 10));
        this.robotLoader = robotLoader;
        this.battleFactory = battleFactory;
        this.taskFactory = taskFactory;
        this.availableTeams = new ArrayList<>();
        this.battleTeams = new ArrayList<>();

        // Available colors
        this.availableColors = new Color[]{
            new Color(0, 255, 255),    // Cyan
            new Color(255, 0, 0),      // Red
            new Color(255, 100, 0),     // Orange
            new Color(255, 0, 255),     // Magenta
            new Color(0, 255, 0),       // Green
            new Color(255, 255, 0),      // Yellow
            new Color(0, 0, 255),       // Blue
            new Color(255, 192, 203)    // Pink
        };

        initComponents();
        autoLoadDefaultTeams();
    }

    /**
     * Set the callback for when battle starts.
     *
     * @param callback the callback
     */
    public void setBattleStartCallback(BattleStartCallback callback)
    {
        this.battleStartCallback = callback;
    }

    /**
     * Initialize UI components.
     */
    private void initComponents()
    {
        setBackground(new Color(13, 13, 13));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(51, 51, 51), 2),
            "BATTLE CONFIGURATION",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 16),
            new Color(0, 255, 0))); // Terminal Green

        // Title section (already in border)

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(13, 13, 13));

        // Teams lists section (two lists side by side)
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listsPanel.setBackground(new Color(13, 13, 13));

        // Left list - Available Teams
        JPanel availablePanel = new JPanel(new BorderLayout(5, 5));
        availablePanel.setBackground(new Color(13, 13, 13));
        availablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(51, 51, 51), 1),
            "AVAILABLE TEAMS",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 12),
            new Color(0, 255, 0)));

        availableTeamsModel = new DefaultListModel<>();
        availableTeamsList = new JList<>(availableTeamsModel);
        availableTeamsList.setBackground(new Color(30, 30, 30));
        availableTeamsList.setForeground(Color.WHITE);
        availableTeamsList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        availableTeamsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane availableScroll = new JScrollPane(availableTeamsList);
        availableScroll.setPreferredSize(new Dimension(200, 150));
        availablePanel.add(availableScroll, BorderLayout.CENTER);
        listsPanel.add(availablePanel);

        // Right list - Battle Teams
        JPanel battlePanel = new JPanel(new BorderLayout(5, 5));
        battlePanel.setBackground(new Color(13, 13, 13));
        battlePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(51, 51, 51), 1),
            "BATTLE TEAMS",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 12),
            new Color(0, 255, 0)));

        battleTeamsModel = new DefaultListModel<>();
        battleTeamsList = new JList<>(battleTeamsModel);
        battleTeamsList.setBackground(new Color(30, 30, 30));
        battleTeamsList.setForeground(Color.WHITE);
        battleTeamsList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        battleTeamsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane battleScroll = new JScrollPane(battleTeamsList);
        battleScroll.setPreferredSize(new Dimension(200, 150));
        battlePanel.add(battleScroll, BorderLayout.CENTER);
        listsPanel.add(battlePanel);

        contentPanel.add(listsPanel, BorderLayout.CENTER);

        // Controls section (buttons and color dropdown)
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setBackground(new Color(13, 13, 13));

        // Load Team JAR button
        JButton loadJarButton = new JButton(LOAD_JAR);
        styleButton(loadJarButton);
        loadJarButton.setActionCommand(LOAD_JAR);
        loadJarButton.addActionListener(this);
        controlsPanel.add(loadJarButton);

        // Color dropdown
        String[] colorNames = {"Cyan", "Red", "Orange", "Magenta", "Green", "Yellow", "Blue", "Pink"};
        colorComboBox = new JComboBox<>(colorNames);
        colorComboBox.setBackground(new Color(30, 30, 30));
        colorComboBox.setForeground(Color.WHITE);
        colorComboBox.setFont(new Font("Monospaced", Font.PLAIN, 11));
        controlsPanel.add(new JLabel("Color:"));
        controlsPanel.add(colorComboBox);

        // Add to Battle button
        JButton addButton = new JButton(ADD_TO_BATTLE);
        styleButton(addButton);
        addButton.setActionCommand(ADD_TO_BATTLE);
        addButton.addActionListener(this);
        controlsPanel.add(addButton);

        // Remove button
        JButton removeButton = new JButton(REMOVE);
        styleButton(removeButton);
        removeButton.setActionCommand(REMOVE);
        removeButton.addActionListener(this);
        controlsPanel.add(removeButton);

        contentPanel.add(controlsPanel, BorderLayout.SOUTH);

        // START BATTLE button (prominent, at bottom)
        startBattleButton = new JButton(START_BATTLE);
        startBattleButton.setFont(new Font("Monospaced", Font.BOLD, 14));
        startBattleButton.setBackground(new Color(0, 150, 0)); // Green
        startBattleButton.setForeground(Color.WHITE);
        startBattleButton.setPreferredSize(new Dimension(300, 40));
        startBattleButton.setActionCommand(START_BATTLE);
        startBattleButton.addActionListener(this);
        startBattleButton.setEnabled(false); // Disabled until 2+ teams added

        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startPanel.setBackground(new Color(13, 13, 13));
        startPanel.add(startBattleButton);

        add(contentPanel, BorderLayout.CENTER);
        add(startPanel, BorderLayout.SOUTH);
    }

    /**
     * Style a button with dark theme.
     */
    private void styleButton(JButton button)
    {
        button.setBackground(new Color(30, 30, 30));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.PLAIN, 11));
        button.setBorder(BorderFactory.createLineBorder(new Color(51, 51, 51), 1));
        button.setFocusPainted(false);
    }

    /**
     * Auto-load default teams from libs/ directory.
     */
    private void autoLoadDefaultTeams()
    {
        File libsDir = new File("libs");
        if (!libsDir.exists()) {
            libsDir = new File("../libs");
        }
        if (!libsDir.exists()) {
            libsDir = new File("../../libs");
        }

        if (libsDir.exists() && libsDir.isDirectory()) {
            File[] jarFiles = libsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                AppLog.debug("[TEAM LOADER] Found " + jarFiles.length + " JAR files in " + libsDir.getAbsolutePath());
                for (File jarFile : jarFiles) {
                    try {
                        TeamInfo team = robotLoader.loadTeamFromJar(jarFile);
                        availableTeams.add(team);
                        availableTeamsModel.addElement(team.getName());
                        AppLog.debug("[TEAM LOADER] Loaded team: " + team.getName());
                    } catch (Exception e) {
                        System.err.println("Failed to load team from " + jarFile + ": " + e.getMessage());
                    }
                }
            }
        } else {
            System.err.println("[TEAM LOADER] libs directory not found. Tried: libs, ../libs, ../../libs");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();

        switch (command) {
            case LOAD_JAR:
                loadTeamJar();
                break;
            case ADD_TO_BATTLE:
                addToBattle();
                break;
            case REMOVE:
                removeFromBattle();
                break;
            case START_BATTLE:
                startBattle();
                break;
        }
    }

    /**
     * Load a team JAR file.
     */
    private void loadTeamJar()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
            }

            @Override
            public String getDescription() {
                return "JAR Files (*.jar)";
            }
        });

        // Default to libs/ directory
        File libsDir = new File("libs");
        if (!libsDir.exists()) {
            libsDir = new File("../libs");
        }
        if (!libsDir.exists()) {
            libsDir = new File("../../libs");
        }
        if (libsDir.exists()) {
            fileChooser.setCurrentDirectory(libsDir);
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File jarFile = fileChooser.getSelectedFile();
            try {
                TeamInfo team = robotLoader.loadTeamFromJar(jarFile);

                // Check if team already loaded
                boolean alreadyExists = false;
                for (TeamInfo existing : availableTeams) {
                    if (existing.getName().equals(team.getName()) && existing.getJarFile().equals(team.getJarFile())) {
                        alreadyExists = true;
                        break;
                    }
                }

                if (!alreadyExists) {
                    availableTeams.add(team);
                    availableTeamsModel.addElement(team.getName());
                    JOptionPane.showMessageDialog(this,
                        "Team loaded successfully: " + team.getName(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Team already loaded: " + team.getName(),
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to load team: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Add selected team to battle.
     */
    private void addToBattle()
    {
        AppLog.debug("\n>>> Add to Battle clicked");

        int selectedIndex = availableTeamsList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= availableTeams.size()) {
            AppLog.debug("No team selected");
            JOptionPane.showMessageDialog(this,
                "Please select a team from Available Teams",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        TeamInfo team = availableTeams.get(selectedIndex);
        AppLog.debug("    Selected team: " + team.getName());

        // Check if already in battle
        for (TeamInfo battleTeam : battleTeams) {
            if (battleTeam.getName().equals(team.getName())) {
                AppLog.debug("Team already in battle list");
                JOptionPane.showMessageDialog(this,
                    "Team already in battle: " + team.getName(),
                    "Already Added",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Get selected color
        int colorIndex = colorComboBox.getSelectedIndex();
        if (colorIndex < 0 || colorIndex >= availableColors.length) {
            colorIndex = 0;
        }
        Color selectedColor = availableColors[colorIndex];
        team.setColor(selectedColor);
        AppLog.debug("    Color assigned: " + colorComboBox.getSelectedItem() + " (RGB: " +
                          selectedColor.getRed() + "," + selectedColor.getGreen() + "," + selectedColor.getBlue() + ")");
        AppLog.debug("    Team color after set: " + team.getColor() + " (RGB: " +
                          team.getColor().getRed() + "," + team.getColor().getGreen() + "," + team.getColor().getBlue() + ")");

        // Add to battle
        battleTeams.add(team);
        battleTeamsModel.addElement(team.getName() + " [" + colorComboBox.getSelectedItem() + "]");
        AppLog.debug("    OK Team added to battle list");
        AppLog.debug("    Battle teams count: " + battleTeams.size());

        // Auto-advance to next color
        colorComboBox.setSelectedIndex((colorIndex + 1) % availableColors.length);

        // Enable START BATTLE if 2+ teams
        boolean canStart = battleTeams.size() >= 2;
        startBattleButton.setEnabled(canStart);
        AppLog.debug("    START BATTLE button enabled: " + canStart);
    }

    /**
     * Remove selected team from battle.
     */
    private void removeFromBattle()
    {
        int selectedIndex = battleTeamsList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= battleTeams.size()) {
            JOptionPane.showMessageDialog(this,
                "Please select a team from Battle Teams",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        battleTeams.remove(selectedIndex);
        battleTeamsModel.remove(selectedIndex);

        // Disable START BATTLE if less than 2 teams
        startBattleButton.setEnabled(battleTeams.size() >= 2);
    }

    /**
     * Start the battle.
     */
    private void startBattle()
    {
        if (battleTeams.size() < 2) {
            JOptionPane.showMessageDialog(this,
                "At least 2 teams are required to start a battle",
                "Not Enough Teams",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (battleStartCallback != null) {
            battleStartCallback.onBattleStart(new ArrayList<>(battleTeams));
        }
    }

    /**
     * Clear all teams from battle (reset).
     */
    public void clearBattleTeams()
    {
        battleTeams.clear();
        battleTeamsModel.clear();
        startBattleButton.setEnabled(false);
    }
}
