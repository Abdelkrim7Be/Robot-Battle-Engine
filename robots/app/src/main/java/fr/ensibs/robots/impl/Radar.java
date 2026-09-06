package fr.ensibs.robots.impl;

/**
 * Represents the radar component of a robot.
 * 
 * <p>The radar is mounted on the gun and can rotate independently.
 * When the gun rotates, the radar follows automatically (hierarchical dependency).
 * When the body rotates, both gun and radar follow (cascading dependency).
 * 
 * <p>The radar is responsible for:
 * <ul>
 *   <li>Radar heading (direction the radar is facing)</li>
 *   <li>Tracking its relative angle to the gun</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class Radar
{
    private final Gun gun; // Reference to the gun this radar is mounted on
    private double heading; // Absolute heading in degrees (0-360)
    
    /**
     * Constructor
     * 
     * @param gun the gun this radar is mounted on
     * @param initialHeading the initial heading in degrees
     */
    Radar(Gun gun, double initialHeading)
    {
        this.gun = gun;
        this.heading = Body.normalize(initialHeading);
    }
    
    /**
     * Get the current heading of the radar in degrees
     * 
     * @return the heading (0-360, where 0 = North)
     */
    double getHeading()
    {
        return heading;
    }
    
    /**
     * Rotate the radar independently of the gun.
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
     * Update the radar's heading when the gun rotates.
     * This is called automatically when the gun rotates to maintain
     * the hierarchical relationship (radar follows gun).
     * 
     * @param gunDelta the change in gun heading (in degrees)
     */
    void onGunRotated(double gunDelta)
    {
        heading = Body.normalize(heading + gunDelta);
    }
    
    /**
     * Get the relative angle of the radar compared to the gun.
     * 
     * @return the relative angle in degrees (-180 to 180)
     */
    double getRelativeAngleToGun()
    {
        double relative = heading - gun.getHeading();
        // Normalize to [-180, 180]
        if (relative > 180) {
            relative -= 360;
        } else if (relative < -180) {
            relative += 360;
        }
        return relative;
    }
}

