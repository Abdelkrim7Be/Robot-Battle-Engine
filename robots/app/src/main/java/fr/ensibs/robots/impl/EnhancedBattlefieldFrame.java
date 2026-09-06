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
    private boolean battleActive;

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

        // dashboard = new BattleDashboard();
        // dashboard.setPreferredSize(new Dimension(250, 0));
        // mainPanel.add(dashboard, BorderLayout.WEST);

        // the controls on the right - made wider and more informative
        controlsPanel = new ControlsPanel(views, factory, taskFactory);
        controlsPanel.setPreferredSize(new Dimension(400, 0)); // Wider panel
        mainPanel.add(controlsPanel, BorderLayout.EAST);

        // Create EnhancedBattlefieldEngine which handles full game loop (bullets, collisions, etc.)
        // This engine updates bullets, detects collisions, and runs robot tasks
        // Use 16ms period (~60 ticks/second) to match GamePhaseManager's assumptions
        if (battlefield instanceof BattlefieldImpl) {
            this.engine = new EnhancedBattlefieldEngine(battlefield, 16); // 16ms = ~60 FPS
            AppLog.debug("Created EnhancedBattlefieldEngine for EnhancedBattlefieldFrame (16ms period = ~60 ticks/sec)");
        } else {
            System.err.println("ERROR: Battlefield must be BattlefieldImpl for EnhancedBattlefieldEngine!");
            System.err.println("Battlefield type: " + battlefield.getClass().getName());
            throw new IllegalStateException("Battlefield must be BattlefieldImpl instance");
        }

        pack();
        setLocationRelativeTo(null);

        // Start graphic engine at 60 FPS (16ms period)
        // Note: dashboard is null now (removed per user request)
        graphicEngine = new EnhancedGraphicEngine(neonPanel, null, battlefield, views, engine, controlsPanel);
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
        battleActive = true;

        if (controlsPanel != null) {
            controlsPanel.resetBattle();
            AppLog.debug("[BATTLE] Kill feed cleared for new battle");
        }
        if (engine != null) {
            engine.resetWinnerDeclaration();
            AppLog.debug("[BATTLE] Winner declaration reset for new battle");
        }

        if (battlefield instanceof BattlefieldImpl) {
            BattlefieldImpl impl = (BattlefieldImpl) battlefield;
            DamageTracker tracker = impl.getDamageTracker();
            tracker.reset();
            AppLog.debug("[BATTLE] Damage tracker reset for new battle");
        }

        AppLog.debug("\n========================================");
        AppLog.debug("=== STARTING BATTLE WITH " + teams.size() + " TEAMS ===");
        AppLog.debug("========================================\n");

        // The taskFactory.loadJar() clears previous classes, so we need to reload each JAR
        // to get all the classes we need
        AppLog.debug("Loading all team JARs...");
        for (TeamInfo team : teams) {
            try {
                taskFactory.loadJar(team.getJarFile());
                AppLog.debug("  Loaded: " + team.getJarFile().getName());
            } catch (Exception e) {
                System.err.println("  Failed to load " + team.getJarFile().getName() + ": " + e.getMessage());
            }
        }

        // Create teams and spawn them
        List<List<BaseDroid>> teamRobotLists = new ArrayList<>();

        for (int teamIndex = 0; teamIndex < teams.size(); teamIndex++) {
            TeamInfo team = teams.get(teamIndex);

            AppLog.debug(">>> Processing Team " + (teamIndex + 1) + ": " + team.getName());
            AppLog.debug("    Color: " + team.getColor());
            AppLog.debug("    JAR: " + team.getJarFile().getName());

            // Reload this team's JAR to get its classes
            try {
                taskFactory.loadJar(team.getJarFile());
                AppLog.debug("    OK JAR loaded successfully");
            } catch (Exception e) {
                System.err.println("    OKOKERROR Failed to reload JAR: " + e.getMessage());
                System.err.println(e.getMessage());
                continue; // Skip this team if JAR fails
            }

            List<BaseDroid> teamRobots = new ArrayList<>();
            List<Droid> teammates = new ArrayList<>();

            // Create droids for this team (as Robot instances so they can have tasks)
            AppLog.debug("    Creating 2 droids...");
            for (int i = 0; i < 2; i++) { // 2 droids per team
                fr.ensibs.robots.logic.Robot droid = factory.makeRobot(); // Create as Robot so it can have tasks
                teammates.add(droid);
                teamRobots.add((BaseDroid) droid);

                // Create view with team color
                Color teamColor = team.getColor();
                AppLog.debug("      Creating droid view with color: " + teamColor + " (RGB: " +
                                  teamColor.getRed() + "," + teamColor.getGreen() + "," + teamColor.getBlue() + ")");
                DroidView<?> view = factory.makeRobotView(droid, team.getName() + " Droid " + (i + 1), teamColor);
                views.add(view);
                AppLog.debug("      OK Created Droid " + (i + 1) + " (Energy: " + droid.getEnergy() +
                                  ", View color: " + view.getColor() + ")");
            }

            // Create leader
            AppLog.debug("    Creating leader...");
            TeamLeader leader = factory.makeTeamLeader(teammates);
            teamRobots.add((BaseDroid) leader);
            AppLog.debug("      OK Created Leader (Energy: " + leader.getEnergy() + ")");

            // Create leader task
            try {
                AppLog.debug("    Creating leader task from class: " + team.getLeaderClass().getSimpleName());
                RobotTask<TeamLeader> leaderTask = taskFactory.makeLeaderTask(team.getLeaderClass());
                leaderTask.setRobot(leader);
                if (engine != null) {
                    engine.addTask(leaderTask);
                } else {
                    System.err.println("      OKOKERROR ERROR: Engine is null! Cannot add leader task!");
                }
                AppLog.debug("      OK Leader task created and added to engine");
            } catch (Exception e) {
                System.err.println("      OKOKERROR Failed to create leader task: " + e.getMessage());
                System.err.println(e.getMessage());
            }

            // Create droid tasks
            List<Class<? extends RobotTask<fr.ensibs.robots.logic.Robot>>> droidClasses = team.getDroidClasses();
            int droidTaskCount = 0;

            AppLog.debug("    Found " + droidClasses.size() + " droid classes");

            if (droidClasses.isEmpty()) {
                System.err.println("    OKWARNINGWARNING WARNING: No droid classes found!");
                System.err.println("    Attempting to reload JAR: " + team.getJarFile());

                try {
                    // Reload the JAR to find droid classes
                    taskFactory.loadJar(team.getJarFile());
                    List<Class<? extends RobotTask<fr.ensibs.robots.logic.Robot>>> reloadedDroidClasses = taskFactory.listRobotClasses();
                    AppLog.debug("      After reload: Found " + reloadedDroidClasses.size() + " robot classes");

                    // Filter to find droid classes (exclude leader)
                    String leaderClassName = team.getLeaderClass().getSimpleName();
                    for (Class<? extends RobotTask<fr.ensibs.robots.logic.Robot>> robotClass : reloadedDroidClasses) {
                        String className = robotClass.getSimpleName();
                        if (!className.contains("Leader") &&
                            !className.equals(leaderClassName) &&
                            (className.contains("Droid") || className.contains(team.getName()))) {
                            droidClasses.add(robotClass);
                            AppLog.debug("      OK Added droid class: " + className);
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
                            System.err.println("      OKOKERROR ERROR: Engine is null! Cannot add droid task!");
                        }
                        droidTaskCount++;
                        AppLog.debug("    Created droid task " + (i+1) + " using " + droidClass.getSimpleName());
                    } catch (Exception e) {
                        System.err.println("    Failed to create droid task " + (i+1) + ": " + e.getMessage());
                        System.err.println(e.getMessage());
                    }
                }
            }

            AppLog.debug("  Team " + team.getName() + ": " + teamRobots.size() + " robots total, " + droidTaskCount + " droid tasks created");

            // Create leader view
            Color leaderColor = team.getColor();
            AppLog.debug("    Creating leader view with color: " + leaderColor + " (RGB: " +
                              leaderColor.getRed() + "," + leaderColor.getGreen() + "," + leaderColor.getBlue() + ")");
            DroidView<?> leaderView = factory.makeRobotView(leader, team.getName() + " Leader", leaderColor);
            views.add(leaderView);
            AppLog.debug("    OK Leader view created (View color: " + leaderView.getColor() + ")");

            teamRobotLists.add(teamRobots);
        }

        // Spawn teams in appropriate positions based on team count
        AppLog.debug(">>> Spawning teams on battlefield...");
        BattlefieldImpl battlefieldImpl = (BattlefieldImpl) battlefield;

        // Clear battlefield using reflection (robots and bullets are private)
        try {
            java.lang.reflect.Field robotsField = BattlefieldImpl.class.getDeclaredField("robots");
            robotsField.setAccessible(true);
            ((List<?>) robotsField.get(battlefieldImpl)).clear();

            java.lang.reflect.Field bulletsField = BattlefieldImpl.class.getDeclaredField("bullets");
            bulletsField.setAccessible(true);
            ((List<?>) bulletsField.get(battlefieldImpl)).clear();
            AppLog.debug("    OK Cleared existing robots and bullets");
        } catch (Exception e) {
            System.err.println("    OKWARNINGWARNING Failed to clear battlefield: " + e.getMessage());
        }

        int totalRobotsToSpawn = 0;
        for (List<BaseDroid> teamRobots : teamRobotLists) {
            totalRobotsToSpawn += teamRobots.size();
        }
        AppLog.debug("    Total robots to spawn: " + totalRobotsToSpawn);

        spawnTeamsAtPositions(battlefieldImpl, teamRobotLists, teams.size());

        // Verify robots were added to battlefield using reflection
        int robotCount = 0;
        try {
            java.lang.reflect.Field robotsField = BattlefieldImpl.class.getDeclaredField("robots");
            robotsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<BaseDroid> robotsOnField = (List<BaseDroid>) robotsField.get(battlefieldImpl);
            robotCount = robotsOnField.size();
            AppLog.debug("    Robots on battlefield after spawn: " + robotCount);
            if (robotCount != totalRobotsToSpawn) {
                System.err.println("    OKWARNINGWARNING WARNING: Robot count mismatch! Expected " + totalRobotsToSpawn + ", got " + robotCount);
            }
        } catch (Exception e) {
            System.err.println("    OKWARNINGWARNING Could not verify robot count: " + e.getMessage());
        }

        AppLog.debug("\n>>> Starting battlefield engine...");
        if (engine == null) {
            System.err.println("    OKOKERROR ERROR: Engine is null! Cannot start battle!");
            return;
        }

        // Count tasks before starting
        try {
            java.lang.reflect.Field tasksField = EnhancedBattlefieldEngine.class.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<?> tasks = (List<?>) tasksField.get(engine);
            int taskCount = tasks != null ? tasks.size() : 0;
            AppLog.debug("    Tasks in engine: " + taskCount);
            if (taskCount == 0) {
                System.err.println("    OKWARNINGWARNING WARNING: No tasks in engine! Robots will not move!");
            }
        } catch (Exception e) {
            System.err.println("    Could not check task count: " + e.getMessage());
        }

        try {
            engine.start();
            AppLog.debug("    OK Engine started successfully");
            AppLog.debug("    Engine type: " + engine.getClass().getSimpleName());
            AppLog.debug("    Deployment countdown: 3 seconds");
            AppLog.debug("    After countdown, robots will start fighting!");
        } catch (Exception e) {
            System.err.println("    OKOKERROR ERROR: Failed to start engine: " + e.getMessage());
            System.err.println(e.getMessage());
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

        AppLog.debug("\n========================================");
        AppLog.debug("=== BATTLE STARTED SUCCESSFULLY ===");
        AppLog.debug("Teams: " + teams.size());
        AppLog.debug("Total robots: " + views.size());
        AppLog.debug("Robots on battlefield: " + robotCount);
        AppLog.debug("Engine running: " + (engine != null));
        AppLog.debug("========================================\n");
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

        AppLog.debug("    Battlefield size: " + fieldWidth + "x" + fieldHeight);

        for (int teamIndex = 0; teamIndex < teamRobotLists.size(); teamIndex++) {
            List<BaseDroid> teamRobots = teamRobotLists.get(teamIndex);
            if (teamRobots.isEmpty()) {
                System.err.println("    OKWARNINGWARNING Team " + (teamIndex + 1) + " has no robots, skipping");
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
            AppLog.debug("    Spawning team at position (" + teamX + ", " + teamY + ") with heading " + heading);
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

                // The robots list was cleared, so we need to add them back
                try {
                    java.lang.reflect.Field robotsField = BattlefieldImpl.class.getDeclaredField("robots");
                    robotsField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<BaseDroid> robotsList = (List<BaseDroid>) robotsField.get(battlefieldImpl);
                    if (!robotsList.contains(robot)) {
                        robotsList.add(robot);
                        AppLog.debug("      OK Registered robot " + (i + 1) + " to battlefield");
                    }
                } catch (Exception e) {
                    System.err.println("      OKOKERROR Failed to register robot: " + e.getMessage());
                }

                AppLog.debug("      OK Spawned robot " + (i + 1) + " at (" + spawnX + ", " + spawnY + ") with heading " + heading);
            }
            AppLog.debug("    OK Team " + (teamIndex + 1) + " fully spawned (" + teamRobots.size() + " robots)");
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
    private class EnhancedGraphicEngine extends GraphicEngine
    {
        private final NeonBattlefieldPanel panel;
        private final BattleDashboard dashboard;
        private final fr.ensibs.robots.logic.Battlefield battlefield;
        private final List<DroidView<? extends Droid>> views;
        private final EnhancedBattlefieldEngine engine;
        private final ControlsPanel controlsPanel;

        EnhancedGraphicEngine(NeonBattlefieldPanel panel, BattleDashboard dashboard,
                             fr.ensibs.robots.logic.Battlefield battlefield,
                             List<DroidView<? extends Droid>> views,
                             EnhancedBattlefieldEngine engine,
                             ControlsPanel controlsPanel)
        {
            super(panel);
            this.panel = panel;
            this.dashboard = dashboard;
            this.battlefield = battlefield;
            this.views = views;
            this.engine = engine;
            this.controlsPanel = controlsPanel;
        }

        @Override
        public void start(int period)
        {
            if (getTimer() == null) {
                javax.swing.Timer timer = new javax.swing.Timer(period, (e) -> {
                    // Update kills map and kill feed with damage events from battlefield
                    if (controlsPanel != null && !views.isEmpty() && battlefield instanceof BattlefieldImpl) {
                        BattlefieldImpl impl = (BattlefieldImpl) battlefield;
                        DamageTracker tracker = impl.getDamageTracker();

                        // Update kill feed with damage events FIRST (before panel clears them)
                        List<BattlefieldImpl.DamageEvent> damageEvents = impl.getAndClearRecentDamageEvents();

                        if (!damageEvents.isEmpty()) {
                            AppLog.debug("[KILL FEED] ========================================");
                            AppLog.debug("[KILL FEED] Processing " + damageEvents.size() + " damage events");
                        }

                        for (BattlefieldImpl.DamageEvent event : damageEvents) {
                            if (event.isDeath) {
                                AppLog.debug("[KILL FEED] Death event: killer=" + (event.killer != null ? event.killer.getClass().getSimpleName() : "null") +
                                                  ", victim=" + (event.victim != null ? event.victim.getClass().getSimpleName() : "null"));

                                // Process both killer-based deaths and natural deaths
                                if (event.killer != null) {
                                    // Find killer name from views
                                    String killerName = findRobotName(views, event.killer);

                                    if (killerName != null) {
                                        // Get kill streak
                                        int killStreak = impl.getKillStreak(event.killer);

                                        // Extract victim name from event if available
                                        String victimName = "Enemy";
                                        if (event.victim != null) {
                                            String foundVictim = findRobotName(views, event.victim);
                                            if (foundVictim != null) {
                                                victimName = foundVictim;
                                            } else {
                                                // Fallback: use class name if view not found
                                                victimName = event.victim.getClass().getSimpleName();
                                            }
                                        }

                                        AppLog.debug("[KILL FEED] Adding kill: " + killerName + " -> " + victimName + " (streak: " + killStreak + ")");
                                        AppLog.debug("Kill feed adding message: [" + killerName + " eliminated " + victimName + "]");
                                        controlsPanel.addKillEvent(killerName, victimName, killStreak);
                                        AppLog.debug("Kill feed message added successfully");

                                        // Check for team elimination after this kill
                                        checkTeamElimination(views, event.victim);
                                    } else {
                                        AppLog.debug("[KILL FEED] WARNING: Killer not found in views: " + event.killer.getClass().getSimpleName());
                                        // Try to add anyway with class name
                                        String fallbackKillerName = event.killer.getClass().getSimpleName();
                                        String fallbackVictimName = event.victim != null ?
                                            (findRobotName(views, event.victim) != null ? findRobotName(views, event.victim) : event.victim.getClass().getSimpleName()) : "Enemy";
                                        int killStreak = impl.getKillStreak(event.killer);
                                        controlsPanel.addKillEvent(fallbackKillerName, fallbackVictimName, killStreak);
                                    }
                                } else if (event.victim != null) {
                                    // Natural death (no killer) - still log it
                                    String victimName = findRobotName(views, event.victim);
                                    if (victimName == null) {
                                        victimName = event.victim.getClass().getSimpleName();
                                    }
                                    AppLog.debug("[KILL FEED] " + victimName + " died (natural death - no killer)");
                                    // Don't add to kill feed for natural deaths (no killer)

                                    // Still check for team elimination even for natural deaths
                                    checkTeamElimination(views, event.victim);
                                }
                            }
                        }

                        // Build kills map for ALL robots (including dead ones from views)
                        // This ensures kills are displayed even if robots are removed from battlefield
                        java.util.Map<Droid, Integer> killsMap = new java.util.HashMap<>();
                        int totalKills = 0;
                        int robotsChecked = 0;
                        int robotsWithKills = 0;

                        // The DamageTracker stores kills by robot instance, so we need to match
                        // the robot instances from views to the ones in the battlefield's robots list
                        // The views may have different instances, so we need to find the matching robot
                        // in the battlefield's robots list by comparing identity or location

                        // Get robots from battlefield (these are the actual instances used in DamageTracker)
                        List<BaseDroid> battlefieldRobots = impl.getRobots();

                        for (DroidView<? extends Droid> view : views) {
                            if (view != null && view.getRobot() != null) {
                                Droid viewRobot = view.getRobot();
                                robotsChecked++;

                                // Find matching robot in battlefield's robots list
                                // Try identity match first (same instance)
                                BaseDroid battlefieldRobot = null;
                                for (BaseDroid bfRobot : battlefieldRobots) {
                                    if (bfRobot == viewRobot) {
                                        battlefieldRobot = bfRobot;
                                        break;
                                    }
                                }

                                // If not found by identity, try to match by location and class
                                if (battlefieldRobot == null) {
                                    fr.ensibs.robots.logic.Location viewLoc = viewRobot.getLocation();
                                    for (BaseDroid bfRobot : battlefieldRobots) {
                                        if (bfRobot.getClass() == viewRobot.getClass()) {
                                            fr.ensibs.robots.logic.Location bfLoc = bfRobot.getLocation();
                                            if (bfLoc != null && viewLoc != null &&
                                                Math.abs(bfLoc.getX() - viewLoc.getX()) < 5 &&
                                                Math.abs(bfLoc.getY() - viewLoc.getY()) < 5) {
                                                battlefieldRobot = bfRobot;
                                                break;
                                            }
                                        }
                                    }
                                }

                                // Use battlefield robot if found, otherwise fall back to view robot
                                Droid robotToQuery = (battlefieldRobot != null) ? battlefieldRobot : viewRobot;

                                // Get kills from tracker using the correct robot instance
                                int kills = impl.getKills(robotToQuery);

                                // Also try direct tracker access as fallback
                                if (kills == 0) {
                                    kills = tracker.getKills(robotToQuery);
                                }

                                // This handles cases where instance matching fails
                                if (kills == 0 && !battlefieldRobots.isEmpty()) {
                                    // Try to find robot by class name and approximate location
                                    String viewRobotClassName = viewRobot.getClass().getSimpleName();
                                    fr.ensibs.robots.logic.Location viewLoc = viewRobot.getLocation();

                                    for (BaseDroid bfRobot : battlefieldRobots) {
                                        if (bfRobot.getClass().getSimpleName().equals(viewRobotClassName)) {
                                            fr.ensibs.robots.logic.Location bfLoc = bfRobot.getLocation();
                                            if (bfLoc != null && viewLoc != null) {
                                                // Check if locations are close (within 50 pixels)
                                                double dist = Math.hypot(bfLoc.getX() - viewLoc.getX(), bfLoc.getY() - viewLoc.getY());
                                                if (dist < 50) {
                                                    int bfKills = tracker.getKills(bfRobot);
                                                    if (bfKills > 0) {
                                                        kills = bfKills;
                                                        AppLog.debug("[KILLS UPDATE] Found kills by location match: " + view.getName() + " -> " + kills + " kills");
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Store kills using view robot as key (for UI display)
                                killsMap.put(viewRobot, kills);

                                // Also store by name as fallback for UI lookup
                                String robotName = view.getName();
                                if (robotName != null && kills > 0) {
                                    // Update name-based map in controlsPanel
                                    if (controlsPanel != null) {
                                        java.util.Map<String, Integer> nameMap = new java.util.HashMap<>();
                                        nameMap.put(robotName, kills);
                                        controlsPanel.updateKillsByNameMap(nameMap);
                                    }
                                }

                                if (kills > 0) {
                                    robotsWithKills++;
                                    totalKills += kills;
                                    AppLog.debug("[KILLS UPDATE] OK " + view.getName() + " has " + kills + " kills (view hash: " + System.identityHashCode(viewRobot) + ", battlefield hash: " + (battlefieldRobot != null ? System.identityHashCode(battlefieldRobot) : "null") + ")");
                                }
                            }
                        }

                        // Debug: Log summary every 60 frames (1 second)
                        if (System.currentTimeMillis() % 1000 < 16) {
                            AppLog.debug("[KILLS UPDATE] Summary: Checked " + robotsChecked + " robots, " + robotsWithKills + " with kills, total kills: " + totalKills);
                        }

                        controlsPanel.updateKillsMap(killsMap);

                        // Force table refresh to show updated kills
                        if (controlsPanel.robotsTable != null) {
                            // Fire table data changed to force refresh
                            controlsPanel.updateTable(); // Use public method instead
                            controlsPanel.robotsTable.repaint();
                        }
                    }

                    // Update visual effects
                    // NOTE: We've already processed damage events for kill feed above
                    // The panel's syncBullets() will try to get events for camera shake, but they're already cleared
                    // This is OK - camera shake can work without events, or we could pass events to panel
                    panel.syncBullets(battlefield);
                    panel.updateParticles();

                    // Check for win condition (only once) - even if engine is stopped
                    boolean battleOver = false;
                    int activeCount = -1;
                    boolean engineStopped = (engine == null || engine.isBattleOver());

                    if (!battleActive || views.isEmpty()) {
                        panel.repaint();
                        return;
                    }

                    if (battlefield instanceof BattlefieldImpl) {
                        BattlefieldImpl impl = (BattlefieldImpl) battlefield;
                        activeCount = impl.getActiveRobotCount();
                        // Battle is over when only 1 or 0 teams have alive robots
                        // Check teams, not just robot count
                        java.util.Map<String, Integer> teamAliveCount = new java.util.HashMap<>();
                        for (DroidView<? extends Droid> view : views) {
                            if (view == null || view.getRobot() == null) continue;
                            Droid robot = view.getRobot();
                            int energy = (int) robot.getEnergy();
                            int motionEnergy = fr.ensibs.robots.logic.BattleSetup.MOTION_ENERGY;
                            if (energy >= motionEnergy) {
                                String teamName = extractTeamName(view.getName());
                                teamAliveCount.put(teamName, teamAliveCount.getOrDefault(teamName, 0) + 1);
                            }
                        }
                        // Battle is over when 0 or 1 teams have alive robots
                        int teamsWithAliveRobots = teamAliveCount.size();

                        // Debug: Log team counts periodically
                        if (System.currentTimeMillis() % 1000 < 16) { // Roughly once per second
                            AppLog.debug("[UI] Teams with alive robots: " + teamsWithAliveRobots + ", Active robots: " + activeCount + ", Engine stopped: " + engineStopped);
                        }

                        // Battle is over if only 1 team has alive robots (that team wins)
                        // OR if 0 teams have alive robots (draw)
                        // Don't trigger during active combat when robots might be temporarily below MOTION_ENERGY
                        if (teamsWithAliveRobots == 1 && activeCount > 0) {
                            // Only one team left with alive robots AND there are active robots - battle is over, that team wins
                            battleOver = true;
                            // Stop engine if still running
                            if (!engineStopped && engine != null && !engine.isBattleOver()) {
                                engine.stop();
                                engineStopped = true; // Update flag after stopping
                            }
                        } else if (teamsWithAliveRobots == 0) {
                            // All teams eliminated - battle is over (draw or winner by energy)
                            battleOver = true;
                            // Stop engine immediately when all teams are eliminated
                            if (!engineStopped && engine != null) {
                                engine.stop();
                                engineStopped = true; // Update flag after stopping
                            }
                        } else {
                            // Multiple teams still alive - battle continues
                            battleOver = false;
                        }
                    } else if (engine != null) {
                        battleOver = engineStopped && engine.isBattleOver();
                        activeCount = engine.getActiveRobotCount();
                    }

                    // Check if engine stopped unexpectedly (no winner declared yet)
                    boolean engineStoppedUnexpectedly = engineStopped && (engine == null || !engine.isWinnerDeclared());

                    // Also check if battle should be over (all teams exhausted or only one team left)
                    boolean shouldBeOver = battleOver || (activeCount <= 1 && engineStopped);

                    if ((shouldBeOver || engineStoppedUnexpectedly) && (engine == null || !engine.isWinnerDeclared())) {
                        AppLog.debug("[UI] ========================================");
                        AppLog.debug("[UI] BATTLE OVER DETECTED - Determining winner...");
                        AppLog.debug("[UI] Active robots remaining: " + activeCount);
                        AppLog.debug("[UI] Battle over flag: " + battleOver + ", Engine stopped: " + engineStopped);

                        String winnerTeam = determineWinnerTeam(views);
                        AppLog.debug("[UI] Winner team determined: " + (winnerTeam != null ? winnerTeam : "NONE (DRAW)"));
                        AppLog.debug("[UI] ========================================");

                        // The engine should already be stopped at this point
                        if (engine != null) {
                            engine.setWinnerDeclared(true);
                            if (!engine.isBattleOver()) {
                                engine.stop(); // Ensure engine is stopped
                                AppLog.debug("[UI] Engine stopped");
                            }
                        }

                        // Always show a popup - either winner or draw
                        if (winnerTeam != null && !winnerTeam.isEmpty()) {
                            AppLog.debug("[UI] *** WINNER ANNOUNCED: " + winnerTeam + " ***");

                            // Announce in kill feed
                            if (controlsPanel != null) {
                                controlsPanel.addKillFeed("");
                                controlsPanel.addKillFeed(">>> " + winnerTeam + " WINS! <<<");
                                controlsPanel.addKillFeed("");
                            }

                            // Show winner popup
                            showWinnerPopup(winnerTeam, views);
                        } else {
                            AppLog.debug("[UI] *** DRAW - ALL TEAMS ELIMINATED ***");

                            // Announce in kill feed
                            if (controlsPanel != null) {
                                controlsPanel.addKillFeed("");
                                controlsPanel.addKillFeed(">>> DRAW - ALL TEAMS ELIMINATED <<<");
                                controlsPanel.addKillFeed("");
                            }

                            showDrawPopup(views);
                        }

                        // Stop the UI timer to freeze the display
                        javax.swing.Timer uiTimer = getTimer();
                        if (uiTimer != null && uiTimer.isRunning()) {
                            uiTimer.stop();
                            AppLog.debug("[UI] Timer stopped - game frozen");
                        }
                        battleActive = false;

                        // Final repaint
                        panel.repaint();
                    } else {
                        // Normal repaint during battle
                        panel.repaint();
                    }
                });
                setTimer(timer);
                timer.start();
            }
        }

        /**
         * Determine the winning team from the views.
         *
         * @param views the list of robot views
         * @return the winning team name, or null if draw
         */
        private String determineWinnerTeam(List<DroidView<? extends Droid>> views) {
            java.util.Map<String, Integer> teamAliveCount = new java.util.HashMap<>();

            AppLog.debug("[UI] Determining winner from " + views.size() + " views...");

            // Count only robots that can actually move (energy >= MOTION_ENERGY)
            for (DroidView<? extends Droid> view : views) {
                if (view == null) continue;
                Droid robot = view.getRobot();
                if (robot == null) continue;

                int energy = (int) robot.getEnergy();
                String viewName = view.getName();
                int motionEnergy = fr.ensibs.robots.logic.BattleSetup.MOTION_ENERGY;

                AppLog.debug("[UI]   Checking robot: " + viewName + " (Energy: " + energy + ", MOTION_ENERGY: " + motionEnergy + ")");

                if (energy >= motionEnergy) {
                    String teamName = extractTeamName(viewName);
                    AppLog.debug("[UI]     -> ACTIVE robot from team: " + teamName + " (energy: " + energy + ")");
                    teamAliveCount.put(teamName, teamAliveCount.getOrDefault(teamName, 0) + 1);
                } else {
                    AppLog.debug("[UI]     -> INACTIVE robot: " + viewName + " (energy: " + energy + " < " + motionEnergy + ")");
                }
            }

            AppLog.debug("[UI] Team counts: " + teamAliveCount);

            // Find team with alive robots
            if (teamAliveCount.size() == 1) {
                String winner = teamAliveCount.keySet().iterator().next();
                AppLog.debug("[UI] Winner found: " + winner + " (only team with alive robots)");
                return winner;
            } else if (teamAliveCount.size() == 0) {
                AppLog.debug("[UI] No teams with active robots - checking by kills and energy");

                java.util.Map<String, Integer> teamKills = new java.util.HashMap<>();
                java.util.Map<String, Integer> teamEnergyTotal = new java.util.HashMap<>();

                if (battlefield instanceof BattlefieldImpl) {
                    BattlefieldImpl impl = (BattlefieldImpl) battlefield;
                    DamageTracker tracker = impl.getDamageTracker();

                    for (DroidView<? extends Droid> view : views) {
                        if (view == null || view.getRobot() == null) continue;
                        String teamName = extractTeamName(view.getName());
                        Droid robot = view.getRobot();
                        int energy = (int) robot.getEnergy();

                        int kills = impl.getKills(robot);
                        if (kills == 0) {
                            kills = tracker.getKills(robot);
                        }

                        if (energy >= 0) {
                            teamEnergyTotal.put(teamName, teamEnergyTotal.getOrDefault(teamName, 0) + energy);
                            teamKills.put(teamName, teamKills.getOrDefault(teamName, 0) + kills);
                        }
                    }
                }

                if (teamEnergyTotal.isEmpty()) {
                    AppLog.debug("[UI] No teams found at all - true draw");
                    return null;
                }

                // Winner by: 1) Most kills, 2) Most energy
                String winner = null;
                int maxKills = -1;
                int maxEnergy = Integer.MIN_VALUE;

                for (java.util.Map.Entry<String, Integer> entry : teamKills.entrySet()) {
                    String team = entry.getKey();
                    int kills = entry.getValue();
                    int energy = teamEnergyTotal.getOrDefault(team, 0);

                    if (kills > maxKills || (kills == maxKills && energy > maxEnergy)) {
                        maxKills = kills;
                        maxEnergy = energy;
                        winner = team;
                    }
                }

                if (winner != null && (maxKills > 0 || maxEnergy > 0)) {
                    AppLog.debug("[UI] Winner by score: " + winner + " (Kills: " + maxKills + ", Energy: " + maxEnergy + ")");
                    return winner;
                }

                AppLog.debug("[UI] All teams eliminated - true draw");
                return null;
            }

            // Multiple teams still alive - use kills/scores as tiebreaker
            AppLog.debug("[UI] Multiple teams still alive: " + teamAliveCount.keySet() + " - checking kills/scores");

            // Get kills for each team
            java.util.Map<String, Integer> teamKills = new java.util.HashMap<>();
            java.util.Map<String, Integer> teamEnergy = new java.util.HashMap<>();

            if (battlefield instanceof BattlefieldImpl) {
                BattlefieldImpl impl = (BattlefieldImpl) battlefield;
                DamageTracker tracker = impl.getDamageTracker();

                for (DroidView<? extends Droid> view : views) {
                    if (view == null || view.getRobot() == null) continue;
                    String teamName = extractTeamName(view.getName());
                    Droid robot = view.getRobot();

                    int kills = impl.getKills(robot);
                    if (kills == 0) {
                        kills = tracker.getKills(robot);
                    }

                    teamKills.put(teamName, teamKills.getOrDefault(teamName, 0) + kills);
                    teamEnergy.put(teamName, teamEnergy.getOrDefault(teamName, 0) + (int)robot.getEnergy());
                }
            }

            // Determine winner by: 1) Most alive robots, 2) Most kills, 3) Most energy
            String winner = null;
            int maxAlive = -1;
            int maxKills = -1;
            int maxEnergy = -1;

            for (java.util.Map.Entry<String, Integer> entry : teamAliveCount.entrySet()) {
                String team = entry.getKey();
                int alive = entry.getValue();
                int kills = teamKills.getOrDefault(team, 0);
                int energy = teamEnergy.getOrDefault(team, 0);

                if (alive > maxAlive ||
                    (alive == maxAlive && kills > maxKills) ||
                    (alive == maxAlive && kills == maxKills && energy > maxEnergy)) {
                    maxAlive = alive;
                    maxKills = kills;
                    maxEnergy = energy;
                    winner = team;
                }
            }

            if (winner != null) {
                AppLog.debug("[UI] Winner by score: " + winner + " (Alive: " + maxAlive + ", Kills: " + maxKills + ", Energy: " + maxEnergy + ")");
                return winner;
            }

            return null;
        }

        /**
         * Show winner popup dialog when battle ends.
         *
         * @param winnerTeam the winning team name
         * @param views the list of robot views for stats
         */
        private void showWinnerPopup(String winnerTeam, List<DroidView<? extends Droid>> views) {
            SwingUtilities.invokeLater(() -> {
                // Calculate stats
                int totalKills = 0;
                int survivors = 0;
                if (battlefield instanceof BattlefieldImpl) {
                    BattlefieldImpl impl = (BattlefieldImpl) battlefield;
                    for (DroidView<? extends Droid> view : views) {
                        if (view != null && view.getRobot() != null) {
                            Droid robot = view.getRobot();
                            String teamName = extractTeamName(view.getName());
                            if (teamName.equals(winnerTeam) && robot.getEnergy() > 0) {
                                survivors++;
                            }
                            // Get kills for this robot
                            int kills = impl.getKills(robot);
                            if (teamName.equals(winnerTeam)) {
                                totalKills += kills;
                            }
                        }
                    }
                }

                String message = String.format(
                    "------------------------\n" +
                    "    VICTORY\n" +
                    "    \n" +
                    "Team %s Wins!\n" +
                    "\n" +
                    "Total Kills: %d\n" +
                    "Survivors: %d robots\n" +
                    "------------------------",
                    winnerTeam, totalKills, survivors
                );

                JOptionPane.showMessageDialog(
                    EnhancedBattlefieldFrame.this,
                    message,
                    "Battle Ended - Victory!",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
        }

        /**
         * Show draw popup dialog when battle ends in a draw.
         *
         * @param views the list of robot views
         */
        private void showDrawPopup(List<DroidView<? extends Droid>> views) {
            SwingUtilities.invokeLater(() -> {
                String message =
                    "------------------------\n" +
                    "    DRAW\n" +
                    "    \n" +
                    "No team survived!\n" +
                    "------------------------";

                JOptionPane.showMessageDialog(
                    EnhancedBattlefieldFrame.this,
                    message,
                    "Battle Ended - Draw!",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
        }

        /**
         * Check if a team has been eliminated (all robots dead) and announce it.
         *
         * @param views the list of all robot views
         * @param deadRobot the robot that just died
         */
        private void checkTeamElimination(List<DroidView<? extends Droid>> views, Droid deadRobot) {
            if (deadRobot == null || controlsPanel == null) {
                return;
            }

            // Find the team of the dead robot
            String deadRobotTeam = null;
            for (DroidView<? extends Droid> view : views) {
                if (view != null && view.getRobot() == deadRobot) {
                    deadRobotTeam = extractTeamName(view.getName());
                    break;
                }
            }

            if (deadRobotTeam == null) {
                return; // Couldn't find team
            }

            // Count alive robots for this team
            int aliveCount = 0;
            for (DroidView<? extends Droid> view : views) {
                if (view != null && view.getRobot() != null) {
                    String teamName = extractTeamName(view.getName());
                    if (teamName.equals(deadRobotTeam)) {
                        Droid robot = view.getRobot();
                        int energy = (int) robot.getEnergy();
                        int motionEnergy = fr.ensibs.robots.logic.BattleSetup.MOTION_ENERGY;
                        if (energy >= motionEnergy) {
                            aliveCount++;
                        }
                    }
                }
            }

            // If no alive robots left, team is eliminated
            if (aliveCount == 0) {
                AppLog.debug("[TEAM ELIMINATION] Team " + deadRobotTeam + " has been eliminated!");
                if (controlsPanel != null) {
                    controlsPanel.addKillFeed("");
                    controlsPanel.addKillFeed(">>> TEAM " + deadRobotTeam + " ELIMINATED <<<");
                    controlsPanel.addKillFeed("");
                }
            }
        }

        /**
         * Extract team name from robot view name.
         *
         * @param viewName the view name (e.g., "AbdelkrimS Leader" or "AbdelkrimS Droid 1")
         * @return the team name (e.g., "AbdelkrimS")
         */
        private String extractTeamName(String viewName) {
            if (viewName == null) return "Unknown";

            // Handle truncated names (e.g., ".hakimS Leader" -> "AbdelhakimS")
            // Try to match common patterns
            if (viewName.startsWith(".")) {
                // Try to reconstruct full name from truncated version
                if (viewName.contains("hakim")) return "AbdelhakimS";
                if (viewName.contains("razak")) return "AbdelrazakS";
                if (viewName.contains("krim")) return "AbdelkrimS";
                if (viewName.contains("ssim")) return "NassimS";
            }

            // Normal extraction: "AbdelkrimS Leader" -> "AbdelkrimS"
            if (viewName.contains(" ")) {
                return viewName.substring(0, viewName.indexOf(" "));
            }
            return viewName;
        }

        /**
         * Find robot name from views list given a robot instance.
         *
         * @param views the list of robot views
         * @param robot the robot to find
         * @return the robot name or null if not found
         */
        private String findRobotName(List<DroidView<? extends Droid>> views, BaseDroid robot) {
            if (robot == null || views == null) return null;
            for (DroidView<? extends Droid> view : views) {
                if (view != null && view.getRobot() == robot) {
                    return view.getName();
                }
            }
            // Fallback: use class name
            return robot.getClass().getSimpleName();
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
     * @return the dashboard (may be null if removed)
     */
    public BattleDashboard getDashboard()
    {
        return dashboard;
    }
}

