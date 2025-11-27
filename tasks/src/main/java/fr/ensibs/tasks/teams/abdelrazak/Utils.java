package fr.ensibs.tasks.teams.abdelrazak;

/**
 * Utility methods for robot calculations.
 */
public class Utils
{
    public static double normalRelativeAngle(double angle)
    {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }
    
    private Utils() {}
}

