package fr.ensibs.robots.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Engine that runs a battlefield game by invoking its {@link RobotTask#run()} methods
 * periodically. At each step, all gun heats are decreased, and all tasks from robots
 * still alive (i.e. having a positive energy) are executed in a random order.
 *
 * @author Pascale Launay
 */
public class BattlefieldEngine
{
    // Random instance to shuffle tasks at each step
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private final List<RobotTask<? extends Robot>> tasks; // the tasks to be executed
    private final int period;                             // the period in ms
    private final Battlefield battlefield;                //  the battlefield
    private ScheduledFuture<?> scheduledFuture;           // future representing the periodical task
    private final ScheduledExecutorService scheduler;     // service used to schedule tasks

    /**
     * Constructor
     *
     * @param battlefield the battlefield
     * @param period      the period between tasks executions in ms
     */
    public BattlefieldEngine(Battlefield battlefield, int period)
    {
        this.tasks = new ArrayList<>();
        this.battlefield = battlefield;
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
            scheduledFuture = scheduler.scheduleAtFixedRate(this::runTasks, 0, period, MILLISECONDS);
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
     * Run the tasks in a random order
     */
    private void runTasks()
    {
        battlefield.decreaseGunHeats();
        Collections.shuffle(tasks, RANDOM);
        for (RobotTask<? extends Robot> task : tasks) {
            if (task.getRobot().getEnergy() > 0) {
                task.run();
            }
        }
    }
}
