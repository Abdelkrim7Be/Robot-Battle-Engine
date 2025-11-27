package fr.ensibs.tasks.teams.abdelkrim;

/**
 * Utility methods for robot calculations.
 * 
 * @author AbdelkrimS Team
 */
public class Utils
{
    /**
     * Normalize relative angle to range [-PI, PI] (shortest path).
     * 
     * @param angle the angle in radians
     * @return normalized angle in radians
     */
    public static double normalRelativeAngle(double angle)
    {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }
    
    private Utils()
    {
        // Utility class - no instantiation
    }
}

