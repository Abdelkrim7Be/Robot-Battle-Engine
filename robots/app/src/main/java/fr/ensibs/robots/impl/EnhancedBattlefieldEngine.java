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
    
    private GamePhaseManager phaseManager;
    private long battleStartTime = 0;
    
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
        
        this.phaseManager = new GamePhaseManager();
        battlefieldImpl.setPhaseManager(phaseManager);
    }
    
    /**
     * Add a new task to be executed periodically
     * 
     * @param task the new task
     */
    public void addTask(RobotTask<? extends Robot> task)
    {
        this.tasks.add(task);
    }
    
    /**
     * Start running the game periodically
     */
    public void start()
    {
        if (this.scheduledFuture == null) {
            battleStartTime = System.currentTimeMillis();
            scheduledFuture = scheduler.scheduleAtFixedRate(this::runGameLoop, 0, period, MILLISECONDS);
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
        }
    }
    
    /**
     * Clear all tasks from the engine.
     */
    public void clearTasks()
    {
        tasks.clear();
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
        phaseManager.update();
        battlefieldImpl.applyPassiveRegeneration();
        battlefieldImpl.detectCollisions();
        battlefieldImpl.decreaseGunHeats();
        battlefieldImpl.updateBattleZone();
        if (phaseManager.getBleedDamagePerTick() > 0) {
            battlefieldImpl.applyBleedDamage(phaseManager.getBleedDamagePerTick());
        }
        battlefieldImpl.updateEnergyCapsules();
        battlefieldImpl.removeDeadRobots();
        tasks.removeIf(task -> {
            Robot robot = task.getRobot();
            return robot.getEnergy() <= 0 || !battlefieldImpl.isRobotRegistered(robot);
        });
        battlefieldImpl.processTeamMessages();

        Collections.shuffle(tasks, RANDOM);
        int tasksExecuted = 0;

        for (RobotTask<? extends Robot> task : tasks) {
            Robot robot = task.getRobot();
            String robotName = robot.getClass().getSimpleName();
            String taskName = task.getClass().getSimpleName();

            if (robot.getEnergy() > 0 && battlefieldImpl.isRobotRegistered(robot)) {
                try {
                    task.run();
                    tasksExecuted++;
                } catch (Exception e) {
                    System.err.println("[TASK] Error executing " + taskName + " for " + robotName + ": " + e.getMessage());
                }
            }
        }

        if (tasksExecuted == 0 && tasks.size() > 0) {
            System.err.println("WARNING: No tasks executed! Tasks in queue: " + tasks.size());
        }

        battlefieldImpl.processPassiveRegeneration();

        int finalActiveCount = battlefieldImpl.getActiveRobotCount();
        long battleDuration = System.currentTimeMillis() - battleStartTime;
        boolean timeout = battleDuration > 300000;

        if (finalActiveCount <= 1 || timeout) {
            stop();
            if (timeout) {
                System.err.println("Battle timeout reached after " + (battleDuration / 1000) + "s.");
            }
        }
    }
    
    /**
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
    
    private boolean winnerDeclared = false;
    
    /**
     * Check if winner has been declared (prevents duplicate announcements).
     * 
     * @return true if winner was already declared
     */
    public boolean isWinnerDeclared() {
        return winnerDeclared;
    }
    
    /**
     * Mark that winner has been declared.
     */
    public void setWinnerDeclared(boolean declared) {
        this.winnerDeclared = declared;
    }
    
    /**
     * Reset winner declaration flag for new battle.
     */
    public void resetWinnerDeclaration() {
        this.winnerDeclared = false;
    }
}
