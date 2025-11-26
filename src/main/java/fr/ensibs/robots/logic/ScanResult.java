package fr.ensibs.robots.logic;

/**
 * Represents the result of a radar scan, containing relative information
 * about detected robots.
 * 
 * <p>This class provides only relative information (distance and bearing angle)
 * to prevent cheating - robots cannot access the actual enemy object or absolute
 * location directly.
 * 
 * @author Robot Wars Team
 */
public class ScanResult
{
    private final double distance; // Distance to the scanned robot in pixels
    private final double bearing;  // Bearing angle in degrees (0-360, relative to scanner)
    
    /**
     * Constructor
     * 
     * @param distance the distance to the scanned robot in pixels
     * @param bearing the bearing angle in degrees (0-360, where 0 = North)
     */
    public ScanResult(double distance, double bearing)
    {
        this.distance = distance;
        this.bearing = normalizeBearing(bearing);
    }
    
    /**
     * Get the distance to the scanned robot
     * 
     * @return the distance in pixels
     */
    public double getDistance()
    {
        return distance;
    }
    
    /**
     * Get the bearing angle to the scanned robot
     * 
     * @return the bearing in degrees (0-360, where 0 = North)
     */
    public double getBearing()
    {
        return bearing;
    }
    
    /**
     * Normalize a bearing angle to the range [0, 360[
     * 
     * @param angle the angle to normalize
     * @return the normalized angle
     */
    private static double normalizeBearing(double angle)
    {
        double result = angle % 360.0;
        if (result < 0) {
            result += 360.0;
        }
        return result;
    }
    
    @Override
    public String toString()
    {
        return String.format("ScanResult{distance=%.2f, bearing=%.2f°}", distance, bearing);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof ScanResult)) return false;
        ScanResult that = (ScanResult) o;
        return Double.compare(that.distance, distance) == 0 &&
               Double.compare(that.bearing, bearing) == 0;
    }
    
    @Override
    public int hashCode()
    {
        return Double.hashCode(distance) * 31 + Double.hashCode(bearing);
    }
}

