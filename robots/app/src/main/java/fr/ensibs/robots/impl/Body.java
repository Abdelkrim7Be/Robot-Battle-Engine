package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;

/**
 * Represents the body component of a robot.
 * 
 * <p>The body is responsible for:
 * <ul>
 *   <li>Position on the battlefield</li>
 *   <li>Body heading (direction the body is facing)</li>
 *   <li>Rotation of the body</li>
 * </ul>
 * 
 * <p>When the body rotates, it notifies attached components (like Gun)
 * to update their relative positions.
 * 
 * @author Robot Wars Team
 */
class Body
{
    private Location location;
    private double heading; // in degrees, 0-360 (0 = North, 90 = East, 180 = South, 270 = West)
    
    /**
     * Constructor
     * 
     * @param location the initial location of the body
     * @param heading the initial heading in degrees
     */
    Body(Location location, double heading)
    {
        this.location = location;
        this.heading = normalize(heading);
    }
    
    /**
     * Get the current location of the body
     * 
     * @return the location
     */
    Location getLocation()
    {
        return location;
    }
    
    /**
     * Set the location of the body
     * 
     * @param location the new location
     */
    void setLocation(Location location)
    {
        this.location = location;
    }
    
    /**
     * Get the current heading of the body in degrees
     * 
     * @return the heading (0-360, where 0 = North)
     */
    double getHeading()
    {
        return heading;
    }
    
    /**
     * Set the heading of the body directly.
     * 
     * @param heading the new heading in degrees
     */
    void setHeading(double heading)
    {
        this.heading = normalize(heading);
    }
    
    /**
     * Rotate the body by the given angle in degrees.
     * 
     * <p>A positive value rotates right (clockwise), negative rotates left (counter-clockwise).
     * The angle is normalized to the range ]-360, 360[.
     * 
     * @param degrees the rotation angle in degrees
     * @return the change in heading (delta) that was applied
     */
    double rotate(double degrees)
    {
        double delta = normalize(degrees);
        double oldHeading = heading;
        heading = normalize(heading + delta);
        return normalize(heading - oldHeading);
    }
    
    /**
     * Calculate the movement vector based on the body's heading and distance.
     * Uses trigonometry to compute the x and y components.
     * 
     * <p>Coordinate system: 0° = North (negative Y), 90° = East (positive X)
     * 
     * @param distance the distance to move (positive = forward, negative = backward)
     * @return a 2-element array [deltaX, deltaY] representing the movement vector
     */
    double[] calculateMovementVector(double distance)
    {
        double radians = Math.toRadians(heading);
        double dx = distance * Math.sin(radians);
        double dy = -distance * Math.cos(radians); // Negative because Y increases downward
        return new double[] { dx, dy };
    }
    
    /**
     * Normalize an angle to the range [0, 360[
     * 
     * @param degrees the angle to normalize
     * @return the normalized angle
     */
    static double normalize(double degrees)
    {
        double result = degrees % 360.0;
        if (result < 0) {
            result += 360.0;
        }
        return result;
    }
}

