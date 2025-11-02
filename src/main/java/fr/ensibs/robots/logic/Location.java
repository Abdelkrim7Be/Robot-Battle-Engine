package fr.ensibs.robots.logic;

/**
 * A location on a 2D plan represented by its x and y coordinates in pixels
 *
 * @author Pascale Launay
 */
public class Location
{
    private final int x, y; // the location coordinates

    /**
     * Constructor
     *
     * @param x the x coordinate of the location
     * @param y the y coordinate of the location
     */
    public Location(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Give the x coordinate of the location
     *
     * @return the x coordinate of the location
     */
    public int getX()
    {
        return x;
    }

    /**
     * Give the y coordinate of the location
     *
     * @return the y coordinate of the location
     */
    public int getY()
    {
        return y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Location) {
            Location p = (Location) o;
            return x == p.x && y == p.y;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "(" + x + "," + y + ")";
    }
}
