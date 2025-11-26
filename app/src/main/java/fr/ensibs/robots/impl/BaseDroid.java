package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.GunOverheatedException;
import fr.ensibs.robots.logic.Location;

/**
 * Base implementation shared by droids and robots.
 * 
 * <p>Manages core droid functionality including:
 * <ul>
 *   <li>Location and movement tracking</li>
 *   <li>Energy management (consumption and recovery)</li>
 *   <li>Gun heat tracking</li>
 *   <li>Body and gun heading management</li>
 * </ul>
 * 
 * <p>When the body turns, the gun automatically follows. The gun can also
 * turn independently of the body.
 */
class BaseDroid implements Droid
{
    private final BattlefieldImpl battlefield;
    private Location location;
    private int energy;
    private int gunHeat;
    private double heading;
    private double gunHeading;

    BaseDroid(BattlefieldImpl battlefield, Location spawn, int initialEnergy, double initialHeading)
    {
        this.battlefield = battlefield;
        this.location = spawn;
        this.energy = initialEnergy;
        this.heading = normalize(initialHeading);
        this.gunHeading = this.heading;
        this.gunHeat = 0;
    }

    BattlefieldImpl getBattlefield()
    {
        return battlefield;
    }

    Location getLocationInternal()
    {
        return location;
    }

    void setLocation(Location location)
    {
        this.location = location;
    }

    void adjustEnergy(int delta)
    {
        this.energy = Math.max(0, this.energy + delta);
    }

    void setGunHeat(int gunHeat)
    {
        this.gunHeat = Math.max(0, gunHeat);
    }

    void increaseGunHeat(int delta)
    {
        this.gunHeat += delta;
    }

    void requireEnergy(int amount) throws ExhaustedException
    {
        if (energy < amount) {
            throw new ExhaustedException(energy);
        }
    }

    void consumeEnergy(int amount) throws ExhaustedException
    {
        requireEnergy(amount);
        adjustEnergy(-amount);
    }

    static double normalize(double value)
    {
        double result = value % 360.0;
        if (result < 0) {
            result += 360.0;
        }
        return result;
    }

    @Override
    public Location getLocation()
    {
        return location;
    }

    @Override
    public int getEnergy()
    {
        return energy;
    }

    @Override
    public int getGunHeat()
    {
        return gunHeat;
    }

    @Override
    public double getHeading()
    {
        return heading;
    }

    double getGunHeadingInternal()
    {
        return gunHeading;
    }

    @Override
    public double getGunHeading()
    {
        return gunHeading;
    }

    @Override
    public void fire(int firePower) throws GunOverheatedException, ExhaustedException
    {
        battlefield.fire(this, firePower);
    }

    @Override
    public void move(double distance) throws CollisionException, ExhaustedException
    {
        battlefield.move(this, distance);
    }

    @Override
    public void turnRobot(double degrees)
    {
        heading = normalize(heading + degrees);
        gunHeading = normalize(gunHeading + degrees);
    }

    @Override
    public void turnGun(double degrees)
    {
        gunHeading = normalize(gunHeading + degrees);
    }
}

