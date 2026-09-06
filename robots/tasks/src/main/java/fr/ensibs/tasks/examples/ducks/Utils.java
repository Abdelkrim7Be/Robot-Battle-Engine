package fr.ensibs.tasks.examples.ducks;

/**
 * Utility methods for robot calculations.
 * 
 * @author Robot Wars Team
 */
public class Utils
{
    /**
     * Normalize relative angle to range [-PI, PI] (shortest path).
     * Uses atan2(sin, cos) for precise normalization.
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

