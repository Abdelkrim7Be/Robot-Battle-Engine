package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;

import java.util.List;

/**
 * Base robot implementation that adds radar capabilities.
 */
class RobotImpl extends BaseDroid implements Robot
{
    private double radarHeading;
    private RobotTask<? extends Robot> task;

    RobotImpl(BattlefieldImpl battlefield, Location spawn, int energy, double heading)
    {
        super(battlefield, spawn, energy, heading);
        this.radarHeading = heading;
    }

    @Override
    public double getRadarHeading()
    {
        return radarHeading;
    }

    @Override
    public void turnRobot(double degrees)
    {
        super.turnRobot(degrees);
        radarHeading = BaseDroid.normalize(radarHeading + degrees);
    }

    @Override
    public void turnGun(double degrees)
    {
        super.turnGun(degrees);
        radarHeading = BaseDroid.normalize(radarHeading + degrees);
    }

    @Override
    public void turnRadar(double degrees)
    {
        radarHeading = BaseDroid.normalize(radarHeading + degrees);
    }

    @Override
    public List<Location> scan() throws ExhaustedException
    {
        return getBattlefield().scan(this);
    }

    RobotTask<? extends Robot> getTask()
    {
        return task;
    }

    void setTask(RobotTask<? extends Robot> task)
    {
        this.task = task;
    }
}


