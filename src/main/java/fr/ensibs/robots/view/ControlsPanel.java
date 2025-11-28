package fr.ensibs.robots.view;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.*;
import fr.ensibs.robots.logic.Robot;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.NB_TEAMMATES;

/**
 * Panel that contains buttons to control battle actions (load and start/stop)
 * and information about the battle state
 *
 * @author Pascale Launay
 */
public class ControlsPanel extends JPanel implements ActionListener
{
    // actions names
    private static final String START = "Start", STOP = "Stop";

    private final Battlefield battlefield;      // the battlefield instance
    private final BattlefieldEngine engine;     // the engine that runs the robots tasks periodically
    private final List<DroidView<? extends Droid>> views; // the views of the robots engaged in the battlefield (SHARED LIST)
    private final BattleFactory factory;        // factory to make robots
    private final RobotTaskFactory taskFactory; // factory to make tasks

    private JButton startButton;    // start/stop button
    public JTable robotsTable;                 // table that displays the robots states (public for external updates)
    private RobotsTableModel tableModel;        // Store reference to model for updates
    private java.util.Map<Droid, Integer> robotKillsMap = new java.util.HashMap<>(); // Kills per robot (by instance)
    private java.util.Map<String, Integer> robotKillsByNameMap = new java.util.HashMap<>(); // Kills per robot (by name) - fallback

    /**
     * Constructor
     *
     * @param views       the views of the robots engaged in the battlefield
     * @param factory     the factory to make robots
     * @param taskFactory the factory to make tasks
     */
    public ControlsPanel(List<DroidView<? extends Droid>> views, BattleFactory factory, RobotTaskFactory taskFactory)
    {
        super(new BorderLayout(5, 5));
        setBackground(new Color(26, 26, 26)); // #1A1A1A Dark Grey
        setBorder(BorderFactory.createLineBorder(new Color(51, 51, 51), 1)); // #333333 border

        this.views = views;
        this.factory = factory;
        this.taskFactory = taskFactory;
        this.battlefield = factory.makeBattlefield();
        this.engine = new BattlefieldEngine(battlefield, 100);

        initComponents();
        // Auto-load robots after UI is ready
        SwingUtilities.invokeLater(this::autoLoadRobots);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        robotsTable.revalidate();
    }

    /**
     * Method invoked when a button is clicked
     *
     * @param e the button event
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch (e.getActionCommand()) {
            case START:
            case STOP: // start/stop button clicked
                startStop();
                break;
        }
    }

    /**
     * Make a team from the given leader task class having the given color and
     * add the leader and the teammates to the battlefield
     *
     * @param clazz the class of the leader task
     * @param color the color of the team
     */
    private void makeTeam(Class<? extends RobotTask<TeamLeader>> clazz, Color color)
    {
        List<Droid> teammates = new ArrayList<>();
        for (int i = 0; i < NB_TEAMMATES; i++) {
            Droid mate = factory.makeDroid();
            teammates.add(mate);
            this.views.add(factory.makeRobotView(mate, clazz.getSimpleName() + " mate", color));
        }
        RobotTask<TeamLeader> task = taskFactory.makeLeaderTask(clazz);
        TeamLeader leader = factory.makeTeamLeader(teammates);
        task.setRobot(leader);
        this.engine.addTask(task);
        this.views.add(factory.makeRobotView(leader, clazz.getSimpleName() + " leader", color));
    }

    /**
     * Make a robot from the given task having the given color and add it to the
     * battlefield
     *
     * @param clazz the class of the robot task
     * @param color the color of the robot
     */
    private void makeRobot(Class<? extends RobotTask<Robot>> clazz, Color color)
    {
        RobotTask<Robot> task = taskFactory.makeRobotTask(clazz);
        Robot robot = factory.makeRobot();
        task.setRobot(robot);
        this.engine.addTask(task);
        this.views.add(factory.makeRobotView(robot, clazz.getSimpleName(), color));
    }

    /**
     * Method invoked when the START/STOP button is clicked. Start or stop the
     * battlefield engine and enable/disable buttons accordingly
     * 
     * NOTE: This method is kept for compatibility but the button is removed.
     * Battle control is now handled by TeamLoaderPanel's START BATTLE button.
     */
    private void startStop()
    {
        // This method is no longer used - battle is controlled by TeamLoaderPanel
        // But keep it for compatibility in case it's called from elsewhere
            if (views.isEmpty()) {
            return; // Don't start if no robots
        }
        // Just toggle engine state
        if (engine != null) {
            // Check if engine is running (would need reflection or a flag)
            // For now, just start it if robots exist
            engine.start();
        }
    }

    /**
     * Initialize the components in the panel. Invoked in the constructor
     */
    private void initComponents()
    {
        // REMOVED: Start/Stop button - only START BATTLE in TeamLoaderPanel should be used
        // The battle is controlled by the TeamLoaderPanel's START BATTLE button

        // table that displays the robots states
        tableModel = new RobotsTableModel(views); // Use shared views list
        robotsTable = new JTable(tableModel);
        robotsTable.setRowSelectionAllowed(false);
        robotsTable.setCellSelectionEnabled(false);
        robotsTable.setDefaultRenderer(String.class, new RobotCellRenderer(views));
        
        // FIX 2: TOTAL BLACKOUT - Entire hierarchy must be dark
        // Table
        robotsTable.setBackground(new Color(30, 30, 30)); // Charcoal
        robotsTable.setForeground(Color.WHITE); // White text (changed from green)
        robotsTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        robotsTable.setGridColor(new Color(51, 51, 51)); // #333333
        robotsTable.setSelectionBackground(new Color(51, 51, 51));
        robotsTable.setSelectionForeground(Color.WHITE); // White when selected
        
        // Table header - BLACK background, GREEN text
        robotsTable.getTableHeader().setBackground(Color.BLACK);
        robotsTable.getTableHeader().setForeground(Color.WHITE); // White text (changed from green)
        robotsTable.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 11));
        
        // Enable horizontal and vertical scrolling
        robotsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        TableColumnModel columnModel = robotsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(200); // ROBOT (wider to show full names like "AbdelkrimS Leader")
        columnModel.getColumn(1).setPreferredWidth(250); // TEAM (even wider to show full team names like "AbdelkrimS", "AbdelrazakS", "NassimS")
        columnModel.getColumn(2).setPreferredWidth(70);  // TYPE
        columnModel.getColumn(3).setPreferredWidth(100); // ENERGY (wider)
        columnModel.getColumn(4).setPreferredWidth(60);  // KILLS
        columnModel.getColumn(5).setPreferredWidth(80);  // STATUS
        
        // Set minimum widths to ensure columns are always visible
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setMinWidth(50);
        }
        
        // SAFE MODE: TOTAL BLACKOUT - Style ENTIRE hierarchy
        JScrollPane scrollPane = new JScrollPane(robotsTable);
        scrollPane.setPreferredSize(new Dimension(750, 500)); // Wider to show all columns including full team names
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Container
        scrollPane.setBackground(new Color(30, 30, 30));
        
        // Viewport - CRITICAL: Must be dark
        JViewport viewport = scrollPane.getViewport();
        viewport.setBackground(new Color(30, 30, 30));
        viewport.setOpaque(true);
        
        // Border
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(51, 51, 51), 1),
            "UNIT_STATUS",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 12),
            new Color(0, 255, 0))); // Terminal Green
        
        // Create kill feed area
        killFeedArea = new JTextArea();
        killFeedArea.setEditable(false);
        killFeedArea.setBackground(new Color(20, 20, 20));
        killFeedArea.setForeground(new Color(0, 255, 0));
        killFeedArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        killFeedArea.setRows(8);
        JScrollPane killFeedScroll = new JScrollPane(killFeedArea);
        killFeedScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(51, 51, 51), 1),
            "KILL_FEED // LIVE",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 12),
            new Color(0, 255, 0))); // Terminal Green
        killFeedScroll.setPreferredSize(new Dimension(400, 150));
        
        // Layout: table on top, kill feed on bottom
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBackground(new Color(26, 26, 26));
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(killFeedScroll, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Class that manages the elements in the robot table. The table has 2 columns (robots
     * names and energy) and 1 row for each robot
     */
    public class RobotsTableModel extends AbstractTableModel
    {
        private final List<DroidView<? extends Droid>> views;  // the views of the robots engaged in the battle

        /**
         * Constructor
         *
         * @param views the views of the robots engaged in the battle
         */
        public RobotsTableModel(List<DroidView<? extends Droid>> views)
        {
            this.views = views;
        }
        
        /**
         * Get the views list (for external access).
         * 
         * @return the views list
         */
        public List<DroidView<? extends Droid>> getViews() {
            return views;
        }

        @Override
        public int getColumnCount()
        {
            return 6; // ROBOT, TEAM, TYPE, ENERGY, KILLS, STATUS
        }

        @Override
        public int getRowCount()
        {
            return views.size();
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            switch (columnIndex) {
                case 0: return "ROBOT";
                case 1: return "TEAM";
                case 2: return "TYPE";
                case 3: return "ENERGY";
                case 4: return "KILLS";
                case 5: return "STATUS";
                default: return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (rowIndex >= views.size()) {
                return "";
            }
            
            DroidView<? extends Droid> view = views.get(rowIndex);
            Droid robot = view.getRobot();
            String name = view.getName();
            
            // Calculate max energy once for use in multiple columns
            int maxEnergy = robot instanceof fr.ensibs.robots.logic.Robot 
                ? fr.ensibs.robots.logic.BattleSetup.ROBOT_INITIAL_ENERGY 
                : fr.ensibs.robots.logic.BattleSetup.DROID_INITIAL_ENERGY;
            int energy = robot.getEnergy();
            
            switch (columnIndex) {
                case 0: // ROBOT
                    return name;
                case 1: // TEAM
                    // Extract team name from robot name (e.g., "AbdelkrimS Leader" -> "AbdelkrimS")
                    if (name.contains(" ")) {
                        return name.substring(0, name.indexOf(" "));
                    }
                    return name;
                case 2: // TYPE
                    if (robot instanceof TeamLeader) {
                        return "★ LEADER"; // Star icon to distinguish Leaders
                    } else if (name.contains("Droid")) {
                        return "Droid";
                    } else {
                        return "Robot";
                    }
                case 3: // ENERGY
                    return energy + "/" + maxEnergy;
                case 4: // KILLS - Get from stored kills map
                    // Kills are updated via updateKillsMap() method from EnhancedBattlefieldFrame
                    // Try by instance first
                    Integer kills = robotKillsMap.get(robot);
                    if (kills == null) {
                        // Fallback: try by name (extract from view name)
                        String robotName = name; // Use the name from the view
                        kills = robotKillsByNameMap.get(robotName);
                    }
                    if (kills != null) {
                        return Integer.toString(kills);
                    }
                    return "0";
                case 5: // STATUS
                    // Use MOTION_ENERGY to determine if robot can move (is "alive")
                    if (energy < fr.ensibs.robots.logic.BattleSetup.MOTION_ENERGY) {
                        return "DEAD";
                    } else if (energy < maxEnergy * 0.3) {
                        return "LOW";
                    } else {
                        return "ALIVE";
                    }
                default:
                    return "";
            }
        }
    }

    /**
     * Class that manages how the elements in the robot table are displayed
     */
    class RobotCellRenderer extends JLabel implements TableCellRenderer
    {
        private final List<DroidView<? extends Droid>> views;  // the views of the robots engaged in the battle

        /**
         * Constructor
         *
         * @param views the views of the robots engaged in the battle
         */
        public RobotCellRenderer(List<DroidView<? extends Droid>> views)
        {
            this.views = views;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
        {
            setText((String) value);

            // FIX 2: TOTAL BLACKOUT - Charcoal background, GREEN text
            if (isSelected) {
                setBackground(new Color(51, 51, 51)); // Dark gray when selected
            } else {
                setBackground(new Color(30, 30, 30)); // Charcoal
            }
            // Color coding based on column
            if (col == 0) {
                // Robot name column - use team color
                Color teamColor = determineTeamColor(views.get(row));
                setForeground(teamColor);
            } else if (col == 3) {
                // Energy column - color gradient based on percentage
                DroidView<? extends Droid> view = views.get(row);
                Droid robot = view.getRobot();
                int energy = robot.getEnergy();
                int maxEnergy = robot instanceof fr.ensibs.robots.logic.Robot 
                    ? fr.ensibs.robots.logic.BattleSetup.ROBOT_INITIAL_ENERGY 
                    : fr.ensibs.robots.logic.BattleSetup.DROID_INITIAL_ENERGY;
                double percent = (double) energy / maxEnergy;
                if (percent > 0.6) {
                    setForeground(new Color(0, 255, 0)); // Green
                } else if (percent > 0.3) {
                    setForeground(new Color(255, 255, 0)); // Yellow
                } else {
                    setForeground(new Color(255, 0, 0)); // Red
                }
            } else if (col == 2) {
                // Type column - special styling for Leaders
                String type = (String) value;
                if (type != null && type.contains("LEADER")) {
                    setForeground(new Color(255, 215, 0)); // Gold for leaders
                } else if (type != null && type.contains("Robot")) {
                    setForeground(new Color(0, 200, 255)); // Cyan for robots
                } else {
                    setForeground(new Color(200, 200, 200)); // Light gray for droids
                }
            } else if (col == 4) {
                // Kills column - green when > 0, gold for high kills
                DroidView<? extends Droid> view = views.get(row);
                Integer kills = robotKillsMap.get(view.getRobot());
                if (kills != null) {
                    if (kills >= 3) {
                        setForeground(new Color(255, 215, 0)); // Gold for high kills
                    } else if (kills > 0) {
                        setForeground(new Color(0, 255, 0)); // Green for kills
                    } else {
                        setForeground(Color.WHITE);
                    }
                } else {
                    setForeground(Color.WHITE);
                }
            } else if (col == 5) {
                // Status column - color based on status
                String status = (String) value;
                if ("DEAD".equals(status)) {
                    setForeground(new Color(255, 0, 0)); // Red
                } else if ("LOW".equals(status)) {
                    setForeground(new Color(255, 255, 0)); // Yellow
                } else {
                    setForeground(new Color(0, 255, 0)); // Green
                }
            } else {
                // Other columns - white text
                setForeground(Color.WHITE);
            }
            
            // Use bold font for leaders, plain for others
            if (col == 2 && value != null && value.toString().contains("LEADER")) {
                setFont(new Font("Monospaced", Font.BOLD, 11));
            } else {
                setFont(new Font("Monospaced", Font.PLAIN, 11));
            }
            
            // Add padding between rows (via border)
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            // define the text alignment (left for names and center for energy values)
            setHorizontalAlignment(col == 1 ? JLabel.CENTER : JLabel.LEFT);
            return this;
        }
        
        /**
         * MISSION 4: Determine team color based on robot name.
         * Duck -> CYAN (Blue), Snail -> RED, fallback to assigned color.
         */
        private Color determineTeamColor(DroidView<? extends Droid> view)
        {
            String name = view.getName().toLowerCase();
            if (name.contains("duck")) {
                return Color.CYAN;
            } else if (name.contains("snail")) {
                return Color.RED;
            }
            // Fallback to assigned color
            return view.getColor();
        }
    }
    
    /**
     * Automatically load all robots from the examples.jar file.
     * Creates one instance of each robot class and one team leader with teammates.
     */
    private void autoLoadRobots()
    {
        try {
            // Try to find examples.jar in common locations
            java.io.File jarFile = findExamplesJar();
            if (jarFile == null || !jarFile.exists()) {
                System.err.println("Warning: examples.jar not found. No robots will be loaded automatically.");
                return;
            }

            // Load the JAR file
            taskFactory.loadJar(jarFile);

            // Predefined colors for robots
            Color[] colors = {
                Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.MAGENTA,
                Color.CYAN, Color.PINK, Color.YELLOW, new Color(128, 0, 128), // Purple
                new Color(255, 165, 0), // Orange
                new Color(0, 128, 128), // Teal
                new Color(255, 192, 203) // Pink
            };

            int colorIndex = 0;

            // Load all robot classes
            List<Class<? extends RobotTask<Robot>>> robotClasses = taskFactory.listRobotClasses();
            for (Class<? extends RobotTask<Robot>> robotClass : robotClasses) {
                Color color = colors[colorIndex % colors.length];
                makeRobot(robotClass, color);
                colorIndex++;
            }

            // Load all team leader classes
            List<Class<? extends RobotTask<TeamLeader>>> leaderClasses = taskFactory.listLeaderClasses();
            for (Class<? extends RobotTask<TeamLeader>> leaderClass : leaderClasses) {
                Color color = colors[colorIndex % colors.length];
                makeTeam(leaderClass, color);
                colorIndex++;
            }

            // Update the table to show loaded robots
            if (robotsTable != null) {
                SwingUtilities.invokeLater(() -> {
                    ((RobotsTableModel) robotsTable.getModel()).fireTableDataChanged();
                });
            }

            System.out.println("Auto-loaded " + (robotClasses.size() + leaderClasses.size()) + " robot/team types");
        } catch (Exception e) {
            System.err.println("Error auto-loading robots: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find the examples.jar file in common locations.
     * 
     * @return the examples.jar file, or null if not found
     */
    private java.io.File findExamplesJar()
    {
        // Try multiple possible locations relative to project root
        String[] possiblePaths = {
            "libs/examples.jar",
            "app/src/test/resources/examples.jar",
            "app/build/resources/test/examples.jar",
            "../libs/examples.jar",
            "../tasks/build/libs/examples.jar",
            "tasks/build/libs/examples.jar"
        };

        // First try relative to current working directory
        String userDir = System.getProperty("user.dir");
        for (String path : possiblePaths) {
            java.io.File file = new java.io.File(userDir, path);
            if (file.exists() && file.isFile()) {
                return file.getAbsoluteFile();
            }
        }

        // Try absolute paths
        for (String path : possiblePaths) {
            java.io.File file = new java.io.File(path);
            if (file.exists() && file.isFile()) {
                return file.getAbsoluteFile();
            }
        }

        // Try to find it in the classpath (if running from IDE or packaged)
        try {
            java.net.URL resource = getClass().getClassLoader().getResource("examples.jar");
            if (resource != null && "file".equals(resource.getProtocol())) {
                java.io.File file = new java.io.File(resource.toURI());
                if (file.exists()) {
                    return file;
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        return null;
    }
    
    // Kill feed support - optimized and dynamic
    private JTextArea killFeedArea;
    private java.util.List<KillFeedEntry> killFeedEntries = new java.util.ArrayList<>();
    private static final int MAX_KILL_FEED_LINES = 15;
    private long battleStartTime = 0;
    
    /**
     * Kill feed entry with timestamp and formatting.
     */
    private static class KillFeedEntry {
        final String message;
        final long timestamp;
        final int killStreak;
        
        KillFeedEntry(String msg, long time, int streak) {
            message = msg;
            timestamp = time;
            killStreak = streak;
        }
    }
    
    /**
     * Add a kill event to the kill feed.
     * 
     * @param killerName the name of the killer
     * @param victimName the name of the victim
     * @param killStreak the current kill streak
     */
    public void addKillEvent(String killerName, String victimName, int killStreak) {
        if (killerName == null || victimName == null) {
            return;
        }
        
        if (battleStartTime == 0) {
            battleStartTime = System.currentTimeMillis();
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - battleStartTime) / 1000;
        long minutes = elapsedSeconds / 60;
        long seconds = elapsedSeconds % 60;
        String timeStr = String.format("%d:%02d", minutes, seconds);
        
        // Format kill message with streak indicator
        // Format: "[TIME] KillerName eliminated VictimName"
        String message;
        if (killStreak >= 5) {
            message = String.format("[%s] %s eliminated %s [PENTA KILL!]", timeStr, killerName, victimName);
        } else if (killStreak >= 4) {
            message = String.format("[%s] %s eliminated %s [QUADRA KILL!]", timeStr, killerName, victimName);
        } else if (killStreak >= 3) {
            message = String.format("[%s] %s eliminated %s [TRIPLE KILL!]", timeStr, killerName, victimName);
        } else if (killStreak >= 2) {
            message = String.format("[%s] %s eliminated %s [DOUBLE KILL!]", timeStr, killerName, victimName);
        } else {
            message = String.format("[%s] %s eliminated %s", timeStr, killerName, victimName);
        }
        
        killFeedEntries.add(new KillFeedEntry(message, currentTime, killStreak));
        
        // Limit size - keep most recent
        if (killFeedEntries.size() > MAX_KILL_FEED_LINES) {
            killFeedEntries.remove(0);
        }
        
        updateKillFeedDisplay();
    }
    
    /**
     * Add a simple message to the kill feed (for battle start/end).
     * 
     * @param message the message
     */
    public void addKillFeed(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        
        // Prevent duplicates
        if (!killFeedEntries.isEmpty()) {
            KillFeedEntry last = killFeedEntries.get(killFeedEntries.size() - 1);
            if (last.message.equals(message)) {
                return;
            }
        }
        
        long currentTime = System.currentTimeMillis();
        if (battleStartTime == 0) {
            battleStartTime = currentTime;
        }
        
        killFeedEntries.add(new KillFeedEntry(message, currentTime, 0));
        
        // Limit size
        if (killFeedEntries.size() > MAX_KILL_FEED_LINES) {
            killFeedEntries.remove(0);
        }
        
        updateKillFeedDisplay();
    }
    
    /**
     * Update the kill feed display area.
     */
    private void updateKillFeedDisplay() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("DEBUG: updateKillFeedDisplay() called - kill feed has " + killFeedEntries.size() + " entries");
            if (killFeedArea != null) {
                StringBuilder sb = new StringBuilder();
                for (KillFeedEntry entry : killFeedEntries) {
                    sb.append(entry.message).append("\n");
                }
                String newText = sb.toString();
                killFeedArea.setText(newText);
                // Auto-scroll to bottom
                killFeedArea.setCaretPosition(killFeedArea.getDocument().getLength());
                // Force repaint to ensure visual update
                killFeedArea.repaint();
                // Also repaint the scroll pane to ensure it updates
                if (killFeedArea.getParent() != null) {
                    killFeedArea.getParent().repaint();
                }
            }
        });
    }
    
    /**
     * Update kills map for robots.
     * 
     * @param killsMap map of robot to kill count
     */
    public void updateKillsMap(java.util.Map<Droid, Integer> killsMap) {
        this.robotKillsMap = killsMap != null ? new java.util.HashMap<>(killsMap) : new java.util.HashMap<>();
        // Also create a name-based map as fallback
        this.robotKillsByNameMap.clear();
        if (killsMap != null && views != null) {
            for (DroidView<? extends Droid> view : views) {
                if (view != null && view.getRobot() != null) {
                    Droid robot = view.getRobot();
                    Integer kills = killsMap.get(robot);
                    if (kills != null) {
                        robotKillsByNameMap.put(view.getName(), kills);
                    }
                }
            }
        }
    }
    
    /**
     * Update kills map by robot name (fallback method).
     * 
     * @param nameKillsMap map of robot name to kill count
     */
    public void updateKillsByNameMap(java.util.Map<String, Integer> nameKillsMap) {
        if (nameKillsMap != null) {
            for (java.util.Map.Entry<String, Integer> entry : nameKillsMap.entrySet()) {
                robotKillsByNameMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Reset battle state (clear kill feed, etc.).
     */
    public void resetBattle() {
        killFeedEntries.clear();
        battleStartTime = 0;
        robotKillsMap.clear();
        if (killFeedArea != null) {
            killFeedArea.setText("");
        }
    }
    
    /**
     * Update the table with live data.
     * This method should be called regularly to refresh the display.
     * The table model uses the shared 'views' list, so it will automatically show current data.
     */
    public void updateTable() {
        if (robotsTable != null && tableModel != null) {
            SwingUtilities.invokeLater(() -> {
                // Force table to refresh - the model uses the shared views list
                tableModel.fireTableDataChanged();
                robotsTable.repaint();
                // Debug: Log if views is empty
                if (views.isEmpty()) {
                    System.out.println("[ControlsPanel] WARNING: views list is empty!");
                } else {
                    System.out.println("[ControlsPanel] Updating table with " + views.size() + " robots");
                }
            });
        }
    }
}