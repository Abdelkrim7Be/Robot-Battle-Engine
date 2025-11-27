package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Location;

/**
 * Represents a bullet fired by a robot.
 * 
 * <p>A bullet has:
 * <ul>
 *   <li>Position (x, y coordinates)</li>
 *   <li>Velocity (direction vector)</li>
 *   <li>Power (damage it will inflict)</li>
 *   <li>Owner (the robot that fired it)</li>
 * </ul>
 * 
 * <p>Bullets travel at a constant speed in a straight line until they:
 * <ul>
 *   <li>Hit a target robot</li>
 *   <li>Reach maximum fire scope distance</li>
 *   <li>Go out of bounds</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class Bullet
{
    // MISSION 5.1: Changed from final to allow object pooling
    private BaseDroid owner; // The robot that fired this bullet
    private int power; // The power/damage of this bullet
    private double heading; // Direction in degrees (0-360)
    private double velocityX; // X component of velocity vector
    private double velocityY; // Y component of velocity vector
    
    private double x; // Current X position (double for smooth movement)
    private double y; // Current Y position (double for smooth movement)
    private double distanceTraveled; // Total distance traveled
    private boolean active; // Whether bullet is still active (not hit/expired)
    
    /**
     * Bullet speed in pixels per game tick.
     * Higher power bullets travel faster.
     */
    private static final double BASE_BULLET_SPEED = 20.0; // pixels per tick
    
    /**
     * Default constructor for object pooling.
     */
    Bullet()
    {
        // Empty constructor for object pool
    }
    
    /**
     * Initialize bullet with values (for reuse from pool).
     * MISSION 5.1: Object pooling support.
     * 
     * @param owner the robot that fired this bullet
     * @param power the bullet power
     * @param startLocation the starting location
     * @param heading the direction in degrees (0-360, where 0 = North)
     */
    void init(BaseDroid owner, int power, Location startLocation, double heading)
    {
        this.owner = owner;
        this.power = power;
        this.heading = Body.normalize(heading);
        this.x = startLocation.getX();
        this.y = startLocation.getY();
        this.distanceTraveled = 0.0;
        this.active = true;
        
        // Calculate velocity vector based on heading
        // Speed increases with power (more powerful bullets travel faster)
        double speed = BASE_BULLET_SPEED + (power * 0.5);
        double radians = Math.toRadians(this.heading);
        this.velocityX = speed * Math.sin(radians);
        this.velocityY = -speed * Math.cos(radians); // Negative because Y increases downward
    }
    
    /**
     * Constructor (for backward compatibility, but uses init internally).
     * 
     * @param owner the robot that fired this bullet
     * @param power the bullet power
     * @param startLocation the starting location
     * @param heading the direction in degrees (0-360, where 0 = North)
     */
    Bullet(BaseDroid owner, int power, Location startLocation, double heading)
    {
        init(owner, power, startLocation, heading);
    }
    
    /**
     * Get the owner of this bullet
     * 
     * @return the robot that fired this bullet
     */
    BaseDroid getOwner()
    {
        return owner;
    }
    
    /**
     * Get the power of this bullet
     * 
     * @return the power value
     */
    int getPower()
    {
        return power;
    }
    
    /**
     * Get the current location of the bullet
     * 
     * @return the current location
     */
    Location getLocation()
    {
        return new Location((int) Math.round(x), (int) Math.round(y));
    }
    
    /**
     * Get the current X coordinate (for rendering).
     * 
     * @return the X coordinate
     */
    double getX()
    {
        return x;
    }
    
    /**
     * Get the current Y coordinate (for rendering).
     * 
     * @return the Y coordinate
     */
    double getY()
    {
        return y;
    }
    
    /**
     * Get the heading/direction of the bullet
     * 
     * @return the heading in degrees (0-360)
     */
    double getHeading()
    {
        return heading;
    }
    
    /**
     * Get the X component of velocity vector.
     * 
     * @return velocity X in pixels per tick
     */
    double getVelocityX()
    {
        return velocityX;
    }
    
    /**
     * Get the Y component of velocity vector.
     * 
     * @return velocity Y in pixels per tick
     */
    double getVelocityY()
    {
        return velocityY;
    }
    
    /**
     * Get the total distance traveled by this bullet
     * 
     * @return the distance in pixels
     */
    double getDistanceTraveled()
    {
        return distanceTraveled;
    }
    
    /**
     * Check if this bullet is still active
     * 
     * @return true if active, false if hit/expired
     */
    boolean isActive()
    {
        return active;
    }
    
    /**
     * Mark this bullet as inactive (hit target or expired)
     */
    void deactivate()
    {
        this.active = false;
    }
    
    /**
     * Update the bullet's position by one game tick.
     * 
     * @return true if bullet is still active and within bounds, false otherwise
     */
    boolean update()
    {
        if (!active) {
            return false;
        }
        
        // Update position
        double oldX = x;
        double oldY = y;
        x += velocityX;
        y += velocityY;
        
        // Update distance traveled
        double dx = x - oldX;
        double dy = y - oldY;
        distanceTraveled += Math.hypot(dx, dy);
        
        // Check if bullet has exceeded maximum fire scope
        if (distanceTraveled > BattleSetup.MAX_FIRE_SCOPE) {
            active = false;
            return false;
        }
        
        // Check if bullet is out of bounds
        if (x < 0 || x > BattleSetup.FIELD_WIDTH || 
            y < 0 || y > BattleSetup.FIELD_HEIGHT) {
            active = false;
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate the damage this bullet will inflict on a target.
     * 
     * <p>Damage formula:
     * <ul>
     *   <li>Base damage: 4 × power</li>
     *   <li>If power > 1: add 2 × (power - 1)</li>
     * </ul>
     * 
     * @return the damage amount
     */
    int calculateDamage()
    {
        int damage = 4 * power;
        if (power > 1) {
            damage += 2 * (power - 1);
        }
        return damage;
    }
    
    /**
     * Calculate the energy recovery for the shooter when this bullet hits.
     * 
     * @return the energy recovery amount (5 × power)
     */
    int calculateLifeSteal()
    {
        return 5 * power;
    }
}

