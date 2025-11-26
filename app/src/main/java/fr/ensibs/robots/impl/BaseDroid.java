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
 *   <li>Location and movement tracking (via Body component)</li>
 *   <li>Energy management (consumption and recovery)</li>
 *   <li>Gun heat tracking (via Gun component)</li>
 *   <li>Body and gun heading management (via Body and Gun components)</li>
 * </ul>
 * 
 * <p>When the body turns, the gun automatically follows. The gun can also
 * turn independently of the body. This is managed through the hierarchical
 * Body -> Gun component relationship.
 */
class BaseDroid implements Droid
{
    private final BattlefieldImpl battlefield;
    private final Body body;
    private final Gun gun;
    private int energy;

    BaseDroid(BattlefieldImpl battlefield, Location spawn, int initialEnergy, double initialHeading)
    {
        this.battlefield = battlefield;
        this.body = new Body(spawn, initialHeading);
        this.gun = new Gun(body, initialHeading);
        this.energy = initialEnergy;
    }

    BattlefieldImpl getBattlefield()
    {
        return battlefield;
    }
    
    Body getBody()
    {
        return body;
    }
    
    Gun getGun()
    {
        return gun;
    }

    Location getLocationInternal()
    {
        return body.getLocation();
    }

    void setLocation(Location location)
    {
        body.setLocation(location);
    }

    void adjustEnergy(int delta)
    {
        this.energy = Math.max(0, this.energy + delta);
    }

    void setGunHeat(int gunHeat)
    {
        gun.setHeat(gunHeat);
    }

    void increaseGunHeat(int delta)
    {
        gun.increaseHeat(delta);
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
        return Body.normalize(value);
    }

    @Override
    public Location getLocation()
    {
        return body.getLocation();
    }

    @Override
    public int getEnergy()
    {
        return energy;
    }

    @Override
    public int getGunHeat()
    {
        return gun.getHeat();
    }

    @Override
    public double getHeading()
    {
        return body.getHeading();
    }

    double getGunHeadingInternal()
    {
        return gun.getHeading();
    }

    @Override
    public double getGunHeading()
    {
        return gun.getHeading();
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
        // Rotate body - this returns the delta that was applied
        double bodyDelta = body.rotate(degrees);
        // Gun automatically follows body rotation
        gun.onBodyRotated(bodyDelta);
    }

    @Override
    public void turnGun(double degrees)
    {
        // Gun rotates independently of body
        gun.rotate(degrees);
    }
}

