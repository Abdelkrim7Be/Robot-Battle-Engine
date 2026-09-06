package fr.ensibs.robots.logic;

public class CollisionException extends Exception
{
    private final Location location;

    public CollisionException(Location location)
    {
        super("Collision at " + location);
        this.location = location;
    }

    public Location getLocation()
    {
        return this.location;
    }
}
