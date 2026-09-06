package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Location;

/**
 * Battle zone system for shrinking safe area (Battle Royale mechanic).
 * 
 * <p>MISSION 3.1: The "Shrinking Zone" (Sudden Death)
 * After T=60 seconds, a red border appears and shrinks by 10 pixels every second.
 * Robots outside the zone take 1 damage/tick.
 * 
 * <p>Forces campers into the center for a chaotic final melee.
 * 
 * @author Robot Wars Team
 */
public class BattleZone
{
    private static final int ZONE_START_TIME = 60; // seconds before zone starts
    private static final double ZONE_SHRINK_RATE = 10.0; // pixels per second
    private static final int ZONE_DAMAGE_PER_TICK = 1; // damage to robots outside zone
    
    private long battleStartTime; // milliseconds
    private double currentZoneRadius; // current safe zone radius (from center)
    private boolean zoneActive;
    
    /**
     * Constructor
     */
    public BattleZone()
    {
        this.battleStartTime = System.currentTimeMillis();
        this.currentZoneRadius = calculateInitialRadius();
        this.zoneActive = false;
    }
    
    /**
     * Calculate initial zone radius (covers entire field).
     * 
     * @return the initial radius in pixels
     */
    private double calculateInitialRadius()
    {
        // Zone is centered on battlefield, radius = distance to corner
        double centerX = BattleSetup.FIELD_WIDTH / 2.0;
        double centerY = BattleSetup.FIELD_HEIGHT / 2.0;
        double cornerX = BattleSetup.FIELD_WIDTH;
        double cornerY = BattleSetup.FIELD_HEIGHT;
        return Math.hypot(cornerX - centerX, cornerY - centerY);
    }
    
    /**
     * Update the battle zone (call each game tick).
     * 
     * @param currentTimeMillis current time in milliseconds
     */
    public void update(long currentTimeMillis)
    {
        long elapsedSeconds = (currentTimeMillis - battleStartTime) / 1000;
        
        if (elapsedSeconds >= ZONE_START_TIME) {
            zoneActive = true;
            // Calculate how many seconds the zone has been shrinking
            long shrinkingSeconds = elapsedSeconds - ZONE_START_TIME;
            // Shrink zone by 10 pixels per second
            double shrinkAmount = shrinkingSeconds * ZONE_SHRINK_RATE;
            currentZoneRadius = Math.max(50.0, calculateInitialRadius() - shrinkAmount); // Minimum 50px radius
        }
    }
    
    /**
     * MISSION B: Shrink the zone by a specific amount (phase-based).
     * 
     * @param shrinkRate pixels to shrink per tick
     */
    public void shrink(double shrinkRate) {
        if (!zoneActive) {
            zoneActive = true;
        }
        currentZoneRadius = Math.max(50.0, currentZoneRadius - shrinkRate);
    }
    
    /**
     * Check if a location is inside the safe zone.
     * 
     * @param location the location to check
     * @return true if inside safe zone, false if outside
     */
    public boolean isInsideZone(Location location)
    {
        if (!zoneActive) {
            return true; // Zone not active yet, everyone is safe
        }
        
        double centerX = BattleSetup.FIELD_WIDTH / 2.0;
        double centerY = BattleSetup.FIELD_HEIGHT / 2.0;
        double dx = location.getX() - centerX;
        double dy = location.getY() - centerY;
        double distance = Math.hypot(dx, dy);
        
        return distance <= currentZoneRadius;
    }
    
    /**
     * Get the damage a robot should take if outside the zone.
     * 
     * @param location the robot's location
     * @return damage amount (0 if inside zone, ZONE_DAMAGE_PER_TICK if outside)
     */
    public int getZoneDamage(Location location)
    {
        if (!zoneActive) {
            return 0; // Zone not active yet
        }
        
        return isInsideZone(location) ? 0 : ZONE_DAMAGE_PER_TICK;
    }
    
    /**
     * Get the current zone center location.
     * 
     * @return the center location
     */
    public Location getZoneCenter()
    {
        return new Location(
            BattleSetup.FIELD_WIDTH / 2,
            BattleSetup.FIELD_HEIGHT / 2
        );
    }
    
    /**
     * Get the current zone radius.
     * 
     * @return the radius in pixels
     */
    public double getZoneRadius()
    {
        return currentZoneRadius;
    }
    
    /**
     * Check if the zone is currently active.
     * 
     * @return true if zone is active and shrinking
     */
    public boolean isZoneActive()
    {
        return zoneActive;
    }
    
    /**
     * Get the time until zone starts (in seconds).
     * 
     * @param currentTimeMillis current time in milliseconds
     * @return seconds until zone starts (0 or negative if already started)
     */
    public long getTimeUntilZoneStarts(long currentTimeMillis)
    {
        long elapsedSeconds = (currentTimeMillis - battleStartTime) / 1000;
        return Math.max(0, ZONE_START_TIME - elapsedSeconds);
    }
    
    /**
     * Reset the battle zone for a new battle.
     */
    public void reset()
    {
        this.battleStartTime = System.currentTimeMillis();
        this.currentZoneRadius = calculateInitialRadius();
        this.zoneActive = false;
    }
}

