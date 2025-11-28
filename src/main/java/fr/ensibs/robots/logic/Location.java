package fr.ensibs.robots.logic;

public class Location
{
    private final int x, y;

    public Location(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

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
