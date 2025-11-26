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
        // Step 1: Update bullets and detect bullet vs robot collisions
        battlefieldImpl.detectCollisions();
        
        // Step 2: Decrease gun heats
        battlefieldImpl.decreaseGunHeats();
        
        // Step 3: Remove dead robots (energy <= 0)
        battlefieldImpl.removeDeadRobots();
        
        // Step 4: Execute robot tasks in random order (only for alive robots)
        Collections.shuffle(tasks, RANDOM);
        for (RobotTask<? extends Robot> task : tasks) {
            if (task.getRobot().getEnergy() > 0) {
                task.run();
            }
        }
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

