package fr.ensibs.tasks.teams.abdelhak;

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

