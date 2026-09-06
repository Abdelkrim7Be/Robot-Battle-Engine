package fr.ensibs.robots.impl;

/**
 * Represents the gun component of a robot.
 * 
 * <p>The gun is mounted on the body and can rotate independently.
 * When the body rotates, the gun follows automatically (hierarchical dependency).
 * 
 * <p>The gun is responsible for:
 * <ul>
 *   <li>Gun heading (direction the gun is facing)</li>
 *   <li>Gun heat management</li>
 *   <li>Tracking its relative angle to the body</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class Gun
{
    private final Body body; // Reference to the body this gun is mounted on
    private double heading; // Absolute heading in degrees (0-360)
    private int heat; // Current gun heat
    
    /**
     * Constructor
     * 
     * @param body the body this gun is mounted on
     * @param initialHeading the initial heading in degrees
     */
    Gun(Body body, double initialHeading)
    {
        this.body = body;
        this.heading = Body.normalize(initialHeading);
        this.heat = 0;
    }
    
    /**
     * Get the current heading of the gun in degrees
     * 
     * @return the heading (0-360, where 0 = North)
     */
    double getHeading()
    {
        return heading;
    }
    
    /**
     * Get the current heat of the gun
     * 
     * @return the heat value
     */
    int getHeat()
    {
        return heat;
    }
    
    /**
     * Set the gun heat
     * 
     * @param heat the new heat value (will be clamped to >= 0)
     */
    void setHeat(int heat)
    {
        this.heat = Math.max(0, heat);
    }
    
    /**
     * Increase the gun heat by the given amount
     * 
     * @param delta the amount to increase heat by
     */
    void increaseHeat(int delta)
    {
        this.heat += delta;
    }
    
    /**
     * Decrease the gun heat by the given amount
     * 
     * @param delta the amount to decrease heat by (will be clamped to >= 0)
     */
    void decreaseHeat(int delta)
    {
        this.heat = Math.max(0, this.heat - delta);
    }
    
    /**
     * Rotate the gun independently of the body.
     * 
     * <p>A positive value rotates right (clockwise), negative rotates left (counter-clockwise).
     * The angle is normalized to the range ]-360, 360[.
     * 
     * @param degrees the rotation angle in degrees
     */
    void rotate(double degrees)
    {
        heading = Body.normalize(heading + degrees);
    }
    
    /**
     * Update the gun's heading when the body rotates.
     * This is called automatically when the body rotates to maintain
     * the hierarchical relationship (gun follows body).
     * 
     * @param bodyDelta the change in body heading (in degrees)
     */
    void onBodyRotated(double bodyDelta)
    {
        heading = Body.normalize(heading + bodyDelta);
    }
    
    /**
     * Get the relative angle of the gun compared to the body.
     * 
     * @return the relative angle in degrees (-180 to 180)
     */
    double getRelativeAngleToBody()
    {
        double relative = heading - body.getHeading();
        // Normalize to [-180, 180]
        if (relative > 180) {
            relative -= 360;
        } else if (relative < -180) {
            relative += 360;
        }
        return relative;
    }
}

