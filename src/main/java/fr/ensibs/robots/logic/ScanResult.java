package fr.ensibs.robots.logic;

public class ScanResult
{
    private final double distance;
    private final double bearing;
    
    public ScanResult(double distance, double bearing)
    {
        this.distance = distance;
        this.bearing = normalizeBearing(bearing);
    }
    
    public double getDistance()
    {
        return distance;
    }
    
    public double getBearing()
    {
        return bearing;
    }
    
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

