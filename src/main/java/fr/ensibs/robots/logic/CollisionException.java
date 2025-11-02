package fr.ensibs.robots.logic;

/**
 * Exception thrown when a collision occurs: a robot collides another robot or
 * goes out of the battlefield. The robot will not move any further: the exception
 * records the location where the robot is blocked after the collision.
 *
 * @author Pascale Launay
 */
public class CollisionException extends Exception
{
    private final Location location; // the location where the robot is blocked

    /**
     * Constructor
     *
     * @param location the location where the robot is blocked after the collision
     */
    public CollisionException(Location location)
    {
        super("Collision at " + location);
        this.location = location;
    }

    /**
     * Give the location where the collision is blocked after the collision
     *
     * @return the location where the collision is blocked
     */
    public Location getLocation()
    {
        return this.location;
    }
}
