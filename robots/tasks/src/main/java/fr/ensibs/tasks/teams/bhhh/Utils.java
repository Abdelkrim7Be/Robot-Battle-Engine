package fr.ensibs.tasks.teams.bhhh;

// Utility methods for robot calculations
public class Utils
{
    // Normalize relative angle to range [-PI, PI]
    public static double normalRelativeAngle(double angle)
    {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }
    
    private Utils() {}
}
