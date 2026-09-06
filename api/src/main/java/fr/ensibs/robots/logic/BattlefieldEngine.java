package fr.ensibs.robots.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BattlefieldEngine
{
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private final List<RobotTask<? extends Robot>> tasks;
    private final int period;
    private final Battlefield battlefield;
    private ScheduledFuture<?> scheduledFuture;
    private final ScheduledExecutorService scheduler;

    public BattlefieldEngine(Battlefield battlefield, int period)
    {
        this.tasks = new ArrayList<>();
        this.battlefield = battlefield;
        this.period = period;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void addTask(RobotTask<? extends Robot> task)
    {
        this.tasks.add(task);
    }

    public void start()
    {
        if (this.scheduledFuture == null) {
            scheduledFuture = scheduler.scheduleAtFixedRate(this::runTasks, 0, period, MILLISECONDS);
        }
    }

    public void stop()
    {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

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
