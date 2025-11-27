package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;

import java.util.List;

/**
 * Base robot implementation that adds radar capabilities.
 * 
 * <p>The radar is mounted on the gun and follows the gun's rotation.
 * When the body rotates, both gun and radar follow (cascading dependency).
 */
class RobotImpl extends BaseDroid implements Robot
{
    private final Radar radar;
    private RobotTask<? extends Robot> task;

    RobotImpl(BattlefieldImpl battlefield, Location spawn, int energy, double heading)
    {
        super(battlefield, spawn, energy, heading);
        this.radar = new Radar(getGun(), heading);
    }
    
    Radar getRadar()
    {
        return radar;
    }

    @Override
    public double getRadarHeading()
    {
        return radar.getHeading();
    }

    @Override
    public void turnRobot(double degrees)
    {
        // Track gun heading before body rotation
        double gunHeadingBefore = getGun().getHeading();
        // Body rotates, which automatically updates gun (via onBodyRotated)
        super.turnRobot(degrees);
        // Calculate how much the gun rotated (should be same as body rotation)
        double gunDelta = getGun().getHeading() - gunHeadingBefore;
        // Normalize the delta to handle wraparound
        if (gunDelta > 180) {
            gunDelta -= 360;
        } else if (gunDelta < -180) {
            gunDelta += 360;
        }
        // Radar follows gun rotation
        radar.onGunRotated(gunDelta);
    }

    @Override
    public void turnGun(double degrees)
    {
        // Track gun heading before rotation
        double gunHeadingBefore = getGun().getHeading();
        // Gun rotates independently
        super.turnGun(degrees);
        // Calculate how much the gun rotated
        double gunDelta = getGun().getHeading() - gunHeadingBefore;
        // Normalize the delta to handle wraparound
        if (gunDelta > 180) {
            gunDelta -= 360;
        } else if (gunDelta < -180) {
            gunDelta += 360;
        }
        // Radar follows gun rotation
        radar.onGunRotated(gunDelta);
    }

    @Override
    public void turnRadar(double degrees)
    {
        if (Math.abs(degrees) < 1e-9) {
            return;
        }
        if (!tryConsumeEnergy(BattleSetup.RADAR_TURN_ENERGY)) {
            return;
        }
        // Radar rotates independently
        radar.rotate(degrees);
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


