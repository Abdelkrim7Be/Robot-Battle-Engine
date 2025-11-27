package fr.ensibs.robots.impl;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.*;
import fr.ensibs.robots.ui.TeamLoaderPanel;
import fr.ensibs.robots.view.ControlsPanel;
import fr.ensibs.robots.view.DroidView;
import fr.ensibs.robots.view.GraphicEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced battlefield frame with improved GUI and UX.
 * 
 * <p>Features:
 * <ul>
 *   <li>Enhanced battlefield panel with grid, HUD, and effects</li>
 *   <li>Better visual styling</li>
 *   <li>Improved layout and controls</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
public class EnhancedBattlefieldFrame extends JFrame
{
    private NeonBattlefieldPanel neonPanel;
    private BattleDashboard dashboard;
    private GraphicEngine graphicEngine;
    private TeamLoaderPanel teamLoaderPanel;
    private ControlsPanel controlsPanel;
    private EnhancedBattlefieldEngine engine;
    private List<DroidView<? extends Droid>> views;
    private BattleFactory factory;
    private RobotTaskFactory taskFactory;
    private Battlefield battlefield;
    
    /**
     * Constructor
     */
    public EnhancedBattlefieldFrame(BattleFactory factory, RobotTaskFactory taskFactory)
    {
        super("BATTLEFIELD_TERMINAL // LIVE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.factory = factory;
        this.taskFactory = taskFactory;
        this.battlefield = factory.makeBattlefield();
        // Don't create our own engine - we'll use ControlsPanel's engine via reflection
        this.engine = null; // Will be set after ControlsPanel is created
        
        // Dark terminal theme
        getContentPane().setBackground(new Color(13, 13, 13));
        
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        setContentPane(mainPanel);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setBackground(new Color(13, 13, 13));

        // shared list of robots views
        views = new ArrayList<>();

        // Team loader panel at the top
        RobotLoader robotLoader = new RobotLoader(taskFactory);
        teamLoaderPanel = new TeamLoaderPanel(robotLoader, factory, taskFactory);
        teamLoaderPanel.setBattleStartCallback(this::startBattleWithTeams);
        teamLoaderPanel.setPreferredSize(new Dimension(0, 250));
        mainPanel.add(teamLoaderPanel, BorderLayout.NORTH);

        // the neon battlefield at the center
        neonPanel = new NeonBattlefieldPanel(views);
        mainPanel.add(neonPanel, BorderLayout.CENTER);

        // Dashboard on the left
        dashboard = new BattleDashboard();
        dashboard.setPreferredSize(new Dimension(250, 0));
        mainPanel.add(dashboard, BorderLayout.WEST);

        // the controls on the right
        controlsPanel = new ControlsPanel(views, factory, taskFactory);
        mainPanel.add(controlsPanel, BorderLayout.EAST);
        
        // Create EnhancedBattlefieldEngine which handles full game loop (bullets, collisions, etc.)
        // This engine updates bullets, detects collisions, and runs robot tasks
        // Use 16ms period (~60 ticks/second) to match GamePhaseManager's assumptions
        if (battlefield instanceof BattlefieldImpl) {
            this.engine = new EnhancedBattlefieldEngine(battlefield, 16); // 16ms = ~60 FPS
            System.out.println("Created EnhancedBattlefieldEngine for EnhancedBattlefieldFrame (16ms period = ~60 ticks/sec)");
        } else {
            System.err.println("ERROR: Battlefield must be BattlefieldImpl for EnhancedBattlefieldEngine!");
            System.err.println("Battlefield type: " + battlefield.getClass().getName());
            throw new IllegalStateException("Battlefield must be BattlefieldImpl instance");
        }
        
        pack();
        setLocationRelativeTo(null);
        
        // Start graphic engine at 60 FPS (16ms period)
        graphicEngine = new EnhancedGraphicEngine(neonPanel, dashboard, battlefield, views);
        graphicEngine.start(16); // ~60 FPS for smooth rendering
    }
    
    /**
     * Start battle with the given teams.
     * 
     * @param teams the teams to battle
     */
    private void startBattleWithTeams(List<TeamInfo> teams)
    {
        // Clear existing robots
        clearBattlefield();
        
        System.out.println("\n========================================");
        System.out.println("=== STARTING BATTLE WITH " + teams.size() + " TEAMS ===");
        System.out.println("========================================\n");
        
        // CRITICAL: Load all team JARs first to ensure all classes are available
        // The taskFactory.loadJar() clears previous classes, so we need to reload each JAR
        // to get all the classes we need
        System.out.println("Loading all team JARs...");
        for (TeamInfo team : teams) {
            try {
                taskFactory.loadJar(team.getJarFile());
                System.out.println("  Loaded: " + team.getJarFile().getName());
            } catch (Exception e) {
                System.err.println("  Failed to load " + team.getJarFile().getName() + ": " + e.getMessage());
            }
        }
        
        // Create teams and spawn them
        List<List<BaseDroid>> teamRobotLists = new ArrayList<>();
        
        for (int teamIndex = 0; teamIndex < teams.size(); teamIndex++) {
            TeamInfo team = teams.get(teamIndex);
            
            System.out.println(">>> Processing Team " + (teamIndex + 1) + ": " + team.getName());
            System.out.println("    Color: " + team.getColor());
            System.out.println("    JAR: " + team.getJarFile().getName());
            
            // Reload this team's JAR to get its classes
            try {
                taskFactory.loadJar(team.getJarFile());
                System.out.println("    ✓ JAR loaded successfully");
            } catch (Exception e) {
                System.err.println("    ✗ Failed to reload JAR: " + e.getMessage());
                e.printStackTrace();
                continue; // Skip this team if JAR fails
            }
            
            List<BaseDroid> teamRobots = new ArrayList<>();
            List<Droid> teammates = new ArrayList<>();
            
            // Create droids for this team (as Robot instances so they can have tasks)
            System.out.println("    Creating 2 droids...");
            for (int i = 0; i < 2; i++) { // 2 droids per team
                fr.ensibs.robots.logic.Robot droid = factory.makeRobot(); // Create as Robot so it can have tasks
                teammates.add(droid);
                teamRobots.add((BaseDroid) droid);
                
                // Create view with team color
                Color teamColor = team.getColor();
                System.out.println("      Creating droid view with color: " + teamColor + " (RGB: " + 
                                  teamColor.getRed() + "," + teamColor.getGreen() + "," + teamColor.getBlue() + ")");
                DroidView<?> view = factory.makeRobotView(droid, team.getName() + " Droid " + (i + 1), teamColor);
                views.add(view);
                System.out.println("      ✓ Created Droid " + (i + 1) + " (Energy: " + droid.getEnergy() + 
                                  ", View color: " + view.getColor() + ")");
            }
            
            // Create leader
            System.out.println("    Creating leader...");
            TeamLeader leader = factory.makeTeamLeader(teammates);
            teamRobots.add((BaseDroid) leader);
            System.out.println("      ✓ Created Leader (Energy: " + leader.getEnergy() + ")");
            
            // Create leader task
            try {
                System.out.println("    Creating leader task from class: " + team.getLeaderClass().getSimpleName());
                RobotTask<TeamLeader> leaderTask = taskFactory.makeLeaderTask(team.getLeaderClass());
                leaderTask.setRobot(leader);
                if (engine != null) {
                    engine.addTask(leaderTask);
                } else {
                    System.err.println("      ✗ ERROR: Engine is null! Cannot add leader task!");
                }
                System.out.println("      ✓ Leader task created and added to engine");
            } catch (Exception e) {
                System.err.println("      ✗ Failed to create leader task: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Create droid tasks
            List<Class<? extends RobotTask<fr.ensibs.robots.logic.Robot>>> droidClasses = team.getDroidClasses();
            int droidTaskCount = 0;
            
            System.out.println("    Found " + droidClasses.size() + " droid classes");
            
            if (droidClasses.isEmpty()) {
                // CRITICAL: If no droid classes found, reload the JAR to find them
                System.err.println("    ⚠ WARNING: No droid classes found!");
                System.err.println("    Attempting to reload JAR: " + team.getJarFile());
                
                try {
                    // Reload the JAR to find droid classes
                    taskFactory.loadJar(team.getJarFile());
                    List<Class<? extends RobotTask<fr.ensibs.robots.logic.Robot>>> reloadedDroidClasses = taskFactory.listRobotClasses();
                    System.out.println("      After reload: Found " + reloadedDroidClasses.size() + " robot classes");
                    
                    // Filter to find droid classes (exclude leader)
                    String leaderClassName = team.getLeaderClass().getSimpleName();
                    for (Class<? extends RobotTask<fr.ensibs.robots.logic.Robot>> robotClass : reloadedDroidClasses) {
                        String className = robotClass.getSimpleName();
                        if (!className.contains("Leader") && 
                            !className.equals(leaderClassName) &&
                            (className.contains("Droid") || className.contains(team.getName()))) {
                            droidClasses.add(robotClass);
                            System.out.println("      ✓ Added droid class: " + className);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("  Failed to reload JAR: " + e.getMessage());
                }
            }
            
            if (droidClasses.isEmpty()) {
                System.err.println("  ERROR: Still no droid classes found! Droids will not have tasks!");
            } else {
                // Assign droid tasks to all teammates
                for (int i = 0; i < teammates.size(); i++) {
                    try {
                        // Cycle through available droid classes
                        Class<? extends RobotTask<fr.ensibs.robots.logic.Robot>> droidClass = droidClasses.get(i % droidClasses.size());
                        RobotTask<fr.ensibs.robots.logic.Robot> droidTask = taskFactory.makeRobotTask(droidClass);
                        droidTask.setRobot((fr.ensibs.robots.logic.Robot) teammates.get(i));
                        if (engine != null) {
                            engine.addTask(droidTask);
                        } else {
                            System.err.println("      ✗ ERROR: Engine is null! Cannot add droid task!");
                        }
                        droidTaskCount++;
                        System.out.println("    Created droid task " + (i+1) + " using " + droidClass.getSimpleName());
                    } catch (Exception e) {
                        System.err.println("    Failed to create droid task " + (i+1) + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            System.out.println("  Team " + team.getName() + ": " + teamRobots.size() + " robots total, " + droidTaskCount + " droid tasks created");
            
            // Create leader view
            Color leaderColor = team.getColor();
            System.out.println("    Creating leader view with color: " + leaderColor + " (RGB: " + 
                              leaderColor.getRed() + "," + leaderColor.getGreen() + "," + leaderColor.getBlue() + ")");
            DroidView<?> leaderView = factory.makeRobotView(leader, team.getName() + " Leader", leaderColor);
            views.add(leaderView);
            System.out.println("    ✓ Leader view created (View color: " + leaderView.getColor() + ")");
            
            teamRobotLists.add(teamRobots);
        }
        
        // Spawn teams in appropriate positions based on team count
        System.out.println(">>> Spawning teams on battlefield...");
        BattlefieldImpl battlefieldImpl = (BattlefieldImpl) battlefield;
        
        // Clear battlefield using reflection (robots and bullets are private)
        try {
            java.lang.reflect.Field robotsField = BattlefieldImpl.class.getDeclaredField("robots");
            robotsField.setAccessible(true);
            ((List<?>) robotsField.get(battlefieldImpl)).clear();
            
            java.lang.reflect.Field bulletsField = BattlefieldImpl.class.getDeclaredField("bullets");
            bulletsField.setAccessible(true);
            ((List<?>) bulletsField.get(battlefieldImpl)).clear();
            System.out.println("    ✓ Cleared existing robots and bullets");
        } catch (Exception e) {
            System.err.println("    ⚠ Failed to clear battlefield: " + e.getMessage());
        }
        
        int totalRobotsToSpawn = 0;
        for (List<BaseDroid> teamRobots : teamRobotLists) {
            totalRobotsToSpawn += teamRobots.size();
        }
        System.out.println("    Total robots to spawn: " + totalRobotsToSpawn);
        
        spawnTeamsAtPositions(battlefieldImpl, teamRobotLists, teams.size());
        
        // Verify robots were added to battlefield using reflection
        int robotCount = 0;
        try {
            java.lang.reflect.Field robotsField = BattlefieldImpl.class.getDeclaredField("robots");
            robotsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<BaseDroid> robotsOnField = (List<BaseDroid>) robotsField.get(battlefieldImpl);
            robotCount = robotsOnField.size();
            System.out.println("    Robots on battlefield after spawn: " + robotCount);
            if (robotCount != totalRobotsToSpawn) {
                System.err.println("    ⚠ WARNING: Robot count mismatch! Expected " + totalRobotsToSpawn + ", got " + robotCount);
            }
        } catch (Exception e) {
            System.err.println("    ⚠ Could not verify robot count: " + e.getMessage());
        }
        
        // CRITICAL: Start the engine - this makes robots actually move and fight!
        System.out.println("\n>>> Starting battlefield engine...");
        if (engine == null) {
            System.err.println("    ✗ ERROR: Engine is null! Cannot start battle!");
            return;
        }
        
        // Count tasks before starting
        try {
            java.lang.reflect.Field tasksField = EnhancedBattlefieldEngine.class.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<?> tasks = (List<?>) tasksField.get(engine);
            int taskCount = tasks != null ? tasks.size() : 0;
            System.out.println("    Tasks in engine: " + taskCount);
            if (taskCount == 0) {
                System.err.println("    ⚠ WARNING: No tasks in engine! Robots will not move!");
            }
        } catch (Exception e) {
            System.err.println("    Could not check task count: " + e.getMessage());
        }
        
        try {
            engine.start();
            System.out.println("    ✓ Engine started successfully");
            System.out.println("    Engine type: " + engine.getClass().getSimpleName());
            System.out.println("    Deployment countdown: 3 seconds");
            System.out.println("    After countdown, robots will start fighting!");
        } catch (Exception e) {
            System.err.println("    ✗ ERROR: Failed to start engine: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Refresh the UNIT_STATUS table to show all robots
        SwingUtilities.invokeLater(() -> {
            try {
                java.lang.reflect.Field tableField = ControlsPanel.class.getDeclaredField("robotsTable");
                tableField.setAccessible(true);
                JTable table = (JTable) tableField.get(controlsPanel);
                if (table != null) {
                    javax.swing.table.TableModel model = table.getModel();
                    if (model instanceof javax.swing.table.AbstractTableModel) {
                        ((javax.swing.table.AbstractTableModel) model).fireTableDataChanged();
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to refresh table: " + e.getMessage());
            }
        });
        
        System.out.println("\n========================================");
        System.out.println("=== BATTLE STARTED SUCCESSFULLY ===");
        System.out.println("Teams: " + teams.size());
        System.out.println("Total robots: " + views.size());
        System.out.println("Robots on battlefield: " + robotCount);
        System.out.println("Engine running: " + (engine != null));
        System.out.println("========================================\n");
    }
    
    /**
     * Spawn teams at appropriate positions based on team count.
     * 
     * @param battlefieldImpl the battlefield implementation
     * @param teamRobotLists list of robot lists for each team
     * @param totalTeams total number of teams
     */
    private void spawnTeamsAtPositions(BattlefieldImpl battlefieldImpl, 
                                       List<List<BaseDroid>> teamRobotLists, 
                                       int totalTeams)
    {
        int fieldWidth = fr.ensibs.robots.logic.BattleSetup.FIELD_WIDTH;
        int fieldHeight = fr.ensibs.robots.logic.BattleSetup.FIELD_HEIGHT;
        int margin = 100;
        
        System.out.println("    Battlefield size: " + fieldWidth + "x" + fieldHeight);
        
        for (int teamIndex = 0; teamIndex < teamRobotLists.size(); teamIndex++) {
            List<BaseDroid> teamRobots = teamRobotLists.get(teamIndex);
            if (teamRobots.isEmpty()) {
                System.err.println("    ⚠ Team " + (teamIndex + 1) + " has no robots, skipping");
                continue;
            }
            
            int teamX, teamY;
            double heading;
            
            if (totalTeams == 2) {
                // Left vs Right
                if (teamIndex == 0) {
                    teamX = margin + 50;
                    teamY = fieldHeight / 2;
                    heading = 0; // Face right (East)
                } else {
                    teamX = fieldWidth - margin - 50;
                    teamY = fieldHeight / 2;
                    heading = 180; // Face left (West)
                }
            } else if (totalTeams == 3) {
                // Triangle formation
                if (teamIndex == 0) {
                    teamX = margin + 50;
                    teamY = margin + 100;
                    heading = 45; // Face center
                } else if (teamIndex == 1) {
                    teamX = fieldWidth - margin - 50;
                    teamY = margin + 100;
                    heading = 135; // Face center
                } else {
                    teamX = fieldWidth / 2;
                    teamY = fieldHeight - margin - 100;
                    heading = 270; // Face center (North)
                }
            } else {
                // 4 teams - corners
                if (teamIndex == 0) {
                    teamX = margin + 50;
                    teamY = margin + 100;
                    heading = 45; // Top-left, face center
                } else if (teamIndex == 1) {
                    teamX = fieldWidth - margin - 50;
                    teamY = margin + 100;
                    heading = 135; // Top-right, face center
                } else if (teamIndex == 2) {
                    teamX = margin + 50;
                    teamY = fieldHeight - margin - 100;
                    heading = 315; // Bottom-left, face center
                } else {
                    teamX = fieldWidth - margin - 50;
                    teamY = fieldHeight - margin - 100;
                    heading = 225; // Bottom-right, face center
                }
            }
            
            // Distribute robots vertically around team position
            System.out.println("    Spawning team at position (" + teamX + ", " + teamY + ") with heading " + heading);
            int spacing = 80;
            for (int i = 0; i < teamRobots.size(); i++) {
                BaseDroid robot = teamRobots.get(i);
                int offsetY = (i - teamRobots.size() / 2) * spacing;
                int spawnX = teamX;
                int spawnY = teamY + offsetY;
                fr.ensibs.robots.logic.Location spawnLoc = new fr.ensibs.robots.logic.Location(spawnX, spawnY);
                robot.setLocation(spawnLoc);
                
                // Set heading
                double currentHeading = robot.getHeading();
                double delta = heading - currentHeading;
                // Normalize to shortest rotation
                if (delta > 180) delta -= 360;
                if (delta < -180) delta += 360;
                robot.turnRobot(delta);
                
                // CRITICAL FIX: Re-register robot after clearing battlefield
                // The robots list was cleared, so we need to add them back
                try {
                    java.lang.reflect.Field robotsField = BattlefieldImpl.class.getDeclaredField("robots");
                    robotsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<BaseDroid> robotsList = (List<BaseDroid>) robotsField.get(battlefieldImpl);
                    if (!robotsList.contains(robot)) {
                        robotsList.add(robot);
                        System.out.println("      ✓ Registered robot " + (i + 1) + " to battlefield");
                    }
                } catch (Exception e) {
                    System.err.println("      ✗ Failed to register robot: " + e.getMessage());
                }
                
                System.out.println("      ✓ Spawned robot " + (i + 1) + " at (" + spawnX + ", " + spawnY + ") with heading " + heading);
            }
            System.out.println("    ✓ Team " + (teamIndex + 1) + " fully spawned (" + teamRobots.size() + " robots)");
        }
    }
    
    /**
     * Clear the battlefield of all robots.
     */
    private void clearBattlefield()
    {
        if (engine != null) {
            engine.stop();
            engine.clearTasks(); // Clear all tasks from previous battle
        }
        views.clear();
        // Note: BattlefieldImpl manages its own robot list internally
        // New robots will be registered fresh when created
    }
    
    /**
     * Enhanced graphic engine that also updates particles, bullets, and dashboard.
     */
    private static class EnhancedGraphicEngine extends GraphicEngine
    {
        private final NeonBattlefieldPanel panel;
        private final BattleDashboard dashboard;
        private final fr.ensibs.robots.logic.Battlefield battlefield;
        private final List<DroidView<? extends Droid>> views;
        
        EnhancedGraphicEngine(NeonBattlefieldPanel panel, BattleDashboard dashboard,
                             fr.ensibs.robots.logic.Battlefield battlefield,
                             List<DroidView<? extends Droid>> views)
        {
            super(panel);
            this.panel = panel;
            this.dashboard = dashboard;
            this.battlefield = battlefield;
            this.views = views;
        }
        
        @Override
        public void start(int period)
        {
            if (getTimer() == null) {
                javax.swing.Timer timer = new javax.swing.Timer(period, (e) -> {
                    // Update visual effects
                    panel.syncBullets(battlefield);
                    panel.updateParticles();
                    
                    // MISSION 4.2: Check for kill streak announcements
                    String announcement = panel.getAndClearLastKillAnnouncement();
                    if (announcement != null) {
                        dashboard.addKillStreakAnnouncement(announcement);
                    }
                    
                    // Update dashboard
                    dashboard.update(views);
                    
                    // Repaint
                    panel.repaint();
                });
                setTimer(timer);
                timer.start();
            }
        }
        
        // Accessor methods using reflection since timer is private
        private javax.swing.Timer getTimer()
        {
            try {
                java.lang.reflect.Field field = GraphicEngine.class.getDeclaredField("timer");
                field.setAccessible(true);
                return (javax.swing.Timer) field.get(this);
            } catch (Exception e) {
                return null;
            }
        }
        
        private void setTimer(javax.swing.Timer timer)
        {
            try {
                java.lang.reflect.Field field = GraphicEngine.class.getDeclaredField("timer");
                field.setAccessible(true);
                field.set(this, timer);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    /**
     * Get the neon panel for external access.
     * 
     * @return the neon panel
     */
    public NeonBattlefieldPanel getNeonPanel()
    {
        return neonPanel;
    }
    
    /**
     * Get the dashboard for external access.
     * 
     * @return the dashboard
     */
    public BattleDashboard getDashboard()
    {
        return dashboard;
    }
}

