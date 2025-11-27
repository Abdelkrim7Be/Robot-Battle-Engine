package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Enhanced battlefield engine with collision detection and death state handling.
 * 
 * <p>This engine extends the base BattlefieldEngine functionality with:
 * <ul>
 *   <li>Bullet updates and collision detection each tick</li>
 *   <li>Removal of dead robots (energy <= 0)</li>
 *   <li>Comprehensive game loop with all interactions</li>
 * </ul>
 * 
 * <p>The game loop executes in this order each tick:
 * <ol>
 *   <li>Update bullets and detect bullet vs robot collisions</li>
 *   <li>Decrease gun heats</li>
 *   <li>Remove dead robots</li>
 *   <li>Execute robot tasks (only for alive robots)</li>
 * </ol>
 * 
 * @author Robot Wars Team
 */
public class EnhancedBattlefieldEngine
{
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    
    private final List<RobotTask<? extends Robot>> tasks;
    private final int period;
    private final BattlefieldImpl battlefieldImpl;
    private ScheduledFuture<?> scheduledFuture;
    private final ScheduledExecutorService scheduler;
    
    // MISSION B: Game phase management
    private GamePhaseManager phaseManager;
    
    // NUCLEAR OPTION: Debug output
    private int tickCount = 0;
    private int lastDebugOutput = 0;
    
    /**
     * Constructor
     * 
     * @param battlefield the battlefield (must be BattlefieldImpl instance)
     * @param period the period between game ticks in ms
     */
    public EnhancedBattlefieldEngine(Battlefield battlefield, int period)
    {
        if (!(battlefield instanceof BattlefieldImpl)) {
            throw new IllegalArgumentException("Battlefield must be BattlefieldImpl instance");
        }
        this.tasks = new ArrayList<>();
        this.battlefieldImpl = (BattlefieldImpl) battlefield;
        this.period = period;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // MISSION B: Initialize phase manager
        this.phaseManager = new GamePhaseManager();
        
        // Set up phase transition callbacks
        phaseManager.setOnDeploymentEnd(() -> {
            System.out.println(">>> COMBAT PHASE BEGINS! <<<");
        });
        phaseManager.setOnSkirmishEnd(() -> {
            System.out.println(">>> ZONE BEGINS SHRINKING! <<<");
        });
        phaseManager.setOnPressureEnd(() -> {
            System.out.println(">>> SUDDEN DEATH! <<<");
        });
        
        // Pass phase manager to battlefield
        battlefieldImpl.setPhaseManager(phaseManager);
    }
    
    /**
     * Add a new task to be executed periodically
     * 
     * @param task the new task
     */
    public void addTask(RobotTask<? extends Robot> task)
    {
        Robot robot = task.getRobot();
        System.out.println("[ENGINE] Adding task " + task.getClass().getSimpleName() + 
                          " for robot " + robot.getClass().getSimpleName() + 
                          " (Energy: " + robot.getEnergy() + ")");
        this.tasks.add(task);
        System.out.println("[ENGINE] Total tasks now: " + tasks.size());
    }
    
    /**
     * Start running the game periodically
     */
    public void start()
    {
        if (this.scheduledFuture == null) {
            System.out.println("[ENGINE] Starting EnhancedBattlefieldEngine with period " + period + "ms (~" + (1000/period) + " ticks/sec)");
            scheduledFuture = scheduler.scheduleAtFixedRate(this::runGameLoop, 0, period, MILLISECONDS);
            System.out.println("[ENGINE] Engine started! Game loop will run every " + period + "ms");
        } else {
            System.out.println("[ENGINE] Engine already running!");
        }
    }
    
    /**
     * Stop the game
     */
    public void stop()
    {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
            System.out.println("[ENGINE] Engine stopped");
        }
        // Don't clear tasks here - let the caller decide when to clear
    }
    
    /**
     * Clear all tasks from the engine.
     */
    public void clearTasks()
    {
        int count = tasks.size();
        tasks.clear();
        System.out.println("[ENGINE] Cleared " + count + " tasks");
    }
    
    /**
     * Enhanced game loop that includes collision detection and death handling.
     * Executes in this order:
     * <ol>
     *   <li>Update bullets and detect bullet vs robot collisions</li>
     *   <li>Decrease gun heats</li>
     *   <li>Remove dead robots</li>
     *   <li>Execute robot tasks (only for alive robots)</li>
     * </ol>
     */
    private void runGameLoop()
    {
        tickCount++;
        
        // CRITICAL DEBUG: Print EVERY tick for first 10 ticks
        if (tickCount <= 10) {
            System.out.println("\n=== TICK " + tickCount + " ===");
            System.out.println("Tasks in queue: " + tasks.size());
            System.out.println("Alive robots on battlefield: " + battlefieldImpl.getActiveRobotCount());
        }
        
        // NUCLEAR OPTION: Debug output every 60 ticks (1 second at 60 FPS)
        if (tickCount - lastDebugOutput > 60) {
            int aliveCount = battlefieldImpl.getActiveRobotCount();
            int bulletCount = battlefieldImpl.getBulletCount();
            int tasksExecuted = 0;
            for (RobotTask<? extends Robot> task : tasks) {
                if (task.getRobot().getEnergy() > 0) {
                    tasksExecuted++;
                }
            }
            System.out.println("=== TICK " + tickCount + " ===");
            System.out.println("Phase: " + phaseManager.getCurrentPhase() + " (Ticks in phase: " + phaseManager.getTicksInPhase() + ")");
            System.out.println("Alive robots: " + aliveCount);
            System.out.println("Active bullets: " + bulletCount);
            System.out.println("Tasks in queue: " + tasks.size() + " (will execute: " + tasksExecuted + ")");
            lastDebugOutput = tickCount;
        }
        
        // Step 0: MISSION B - Update game phase manager
        phaseManager.update();
        
        // Step 0.5: MISSION A.4 - Reset movement/firing flags for passive regeneration tracking
        battlefieldImpl.applyPassiveRegeneration();
        
        // Step 1: Update bullets and detect bullet vs robot collisions
        battlefieldImpl.detectCollisions();
        
        // Step 2: Decrease gun heats
        battlefieldImpl.decreaseGunHeats();
        
        // Step 3: MISSION B - Update battle zone with phase-based shrinking
        battlefieldImpl.updateBattleZone();
        
        // Step 4: MISSION B - Apply bleed damage in sudden death
        if (phaseManager.getBleedDamagePerTick() > 0) {
            battlefieldImpl.applyBleedDamage(phaseManager.getBleedDamagePerTick());
        }
        
        // Step 5: MISSION 3.2 - Update energy capsules and check for collection
        battlefieldImpl.updateEnergyCapsules();
        
        // Step 6: Remove dead robots (energy <= 0)
        battlefieldImpl.removeDeadRobots();
        
        // Step 6.5: CRITICAL FIX - Remove tasks for dead/removed robots
        // This prevents "Unknown robot instance" errors when tasks try to execute on removed robots
        tasks.removeIf(task -> {
            Robot robot = task.getRobot();
            // Remove task if robot is dead or no longer registered on battlefield
            return robot.getEnergy() <= 0 || !battlefieldImpl.isRobotRegistered(robot);
        });
        
        // Step 7: Process team messages (droids react to leader commands)
        battlefieldImpl.processTeamMessages();
        
        // Step 8: Execute robot tasks in random order (only for alive robots)
        // CRITICAL: Robots can move and fire immediately - no deployment phase
        Collections.shuffle(tasks, RANDOM);
        int tasksExecuted = 0;
        int tasksSkipped = 0;
        
        // CRITICAL DEBUG: Print task list EVERY tick for first 10 ticks
        if (tickCount <= 10) {
            System.out.println("[TASK] About to execute " + tasks.size() + " tasks");
            for (int i = 0; i < tasks.size(); i++) {
                RobotTask<? extends Robot> t = tasks.get(i);
                Robot r = t.getRobot();
                System.out.println("  Task " + i + ": " + t.getClass().getSimpleName() + 
                                  " -> " + r.getClass().getSimpleName() + 
                                  " (Energy: " + r.getEnergy() + ", Registered: " + 
                                  battlefieldImpl.isRobotRegistered(r) + ")");
            }
        }
        
        for (RobotTask<? extends Robot> task : tasks) {
            Robot robot = task.getRobot();
            String robotName = robot.getClass().getSimpleName();
            String taskName = task.getClass().getSimpleName();
            
            // DEBUG: Log every task execution attempt (ALWAYS for first 10 ticks)
            if (tickCount <= 10) {
                System.out.println("[TASK] Attempting to execute task " + taskName + " for robot " + robotName);
                System.out.println("[TASK]   Robot energy: " + robot.getEnergy());
                System.out.println("[TASK]   Robot registered: " + battlefieldImpl.isRobotRegistered(robot));
            }
            
            // Double-check robot is still valid before executing
            if (robot.getEnergy() > 0 && battlefieldImpl.isRobotRegistered(robot)) {
                try {
                    // DEBUG: Log before calling run() (ALWAYS for first 10 ticks)
                    if (tickCount <= 10) {
                        System.out.println("[TASK] >>> CALLING run() on " + taskName + " for " + robotName + " <<<");
                    }
                    task.run(); // Execute task - robots can move, scan, etc. during deployment
                    tasksExecuted++;
                    if (tickCount <= 10) {
                        System.out.println("[TASK] ✓ Task " + taskName + " completed successfully");
                    }
                } catch (Exception e) {
                    // Log but don't crash - continue with other tasks
                    System.err.println("[TASK] ✗ ERROR executing task " + taskName + " for " + robotName + ": " + e.getMessage());
                    if (tickCount <= 10) {
                        e.printStackTrace(); // Full stack trace for first few ticks
                    }
                }
            } else {
                tasksSkipped++;
                if (tickCount <= 10) {
                    System.out.println("[TASK] ✗ Skipping task " + taskName + " - energy: " + robot.getEnergy() + ", registered: " + battlefieldImpl.isRobotRegistered(robot));
                }
            }
        }
        
        // DEBUG: Log execution summary (ALWAYS for first 10 ticks)
        if (tickCount <= 10) {
            System.out.println("[TASK] Execution summary: " + tasksExecuted + " executed, " + tasksSkipped + " skipped, " + tasks.size() + " total tasks");
        }
        
        // Debug: Verify tasks are running
        if (tickCount % 60 == 0 && tasksExecuted == 0 && tasks.size() > 0) {
            System.err.println("WARNING: No tasks executed! Tasks in queue: " + tasks.size());
        }
        
        // Step 9: MISSION A.4 - Apply passive regeneration after all robot actions
        battlefieldImpl.processPassiveRegeneration();
    }
    
    /**
     * MISSION B: Get the phase manager for UI access.
     * 
     * @return the phase manager
     */
    public GamePhaseManager getPhaseManager() {
        return phaseManager;
    }
    
    /**
     * Get the number of active (alive) robots on the battlefield.
     * 
     * @return the count of robots with energy > 0
     */
    public int getActiveRobotCount()
    {
        return battlefieldImpl.getActiveRobotCount();
    }
    
    /**
     * Check if the battle is over (only one or zero robots remaining).
     * 
     * @return true if battle is over, false otherwise
     */
    public boolean isBattleOver()
    {
        return battlefieldImpl.getActiveRobotCount() <= 1;
    }
}

