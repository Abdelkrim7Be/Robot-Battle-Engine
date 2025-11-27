package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;

import java.awt.*;
import java.util.Random;

/**
 * Energy capsule that spawns when a robot dies.
 * 
 * <p>MISSION 3.2: Energy Drops (Risk/Reward)
 * When a robot dies, spawn an EnergyCapsule at its location.
 * Any robot driving over it gains +50 Energy.
 * 
 * <p>Creates "Hotspots" on the map that robots fight over.
 * 
 * @author Robot Wars Team
 */
class EnergyCapsule
{
    private static final int ENERGY_VALUE = 50; // Energy gained when collected
    private static final int COLLECTION_RADIUS = 20; // Pixels - robot must be within this distance
    private static final int PULSE_DURATION = 60; // Frames for pulse animation
    
    private final Location location;
    private final Random random;
    private int age; // Frames since spawn
    private boolean collected;
    
    /**
     * Constructor
     * 
     * @param location the location where the capsule spawns (robot death location)
     */
    EnergyCapsule(Location location)
    {
        this.location = location;
        this.random = new Random();
        this.age = 0;
        this.collected = false;
    }
    
    /**
     * Update the capsule (for animation).
     */
    void update()
    {
        if (!collected) {
            age++;
        }
    }
    
    /**
     * Check if a robot at the given location can collect this capsule.
     * 
     * @param robotLocation the robot's location
     * @return true if robot can collect the capsule
     */
    boolean canBeCollected(Location robotLocation)
    {
        if (collected) {
            return false;
        }
        
        double dx = robotLocation.getX() - location.getX();
        double dy = robotLocation.getY() - location.getY();
        double distance = Math.hypot(dx, dy);
        
        return distance <= COLLECTION_RADIUS;
    }
    
    /**
     * Collect this capsule (mark as collected).
     * 
     * @return the energy value of this capsule
     */
    int collect()
    {
        if (collected) {
            return 0;
        }
        collected = true;
        return ENERGY_VALUE;
    }
    
    /**
     * Check if this capsule has been collected.
     * 
     * @return true if collected, false otherwise
     */
    boolean isCollected()
    {
        return collected;
    }
    
    /**
     * Get the location of this capsule.
     * 
     * @return the location
     */
    Location getLocation()
    {
        return location;
    }
    
    /**
     * Draw the energy capsule.
     * CRITICAL FIX: Use Graphics2D copy for isolated transforms.
     * 
     * @param g2d the graphics context
     */
    void draw(Graphics2D g2d)
    {
        if (collected) {
            return; // Don't draw collected capsules
        }
        
        // CRITICAL FIX: Use Graphics2D copy for isolated transforms
        Graphics2D gCopy = (Graphics2D) g2d.create();
        try {
            int x = location.getX();
            int y = location.getY();
            gCopy.translate(x, y);
            
            // Pulsing animation
            double pulsePhase = (age % PULSE_DURATION) / (double) PULSE_DURATION;
            double pulseScale = 0.8 + 0.2 * Math.sin(pulsePhase * 2 * Math.PI);
            
            int baseSize = 15;
            int size = (int)(baseSize * pulseScale);
            
            // Draw outer glow (bright blue-green)
            gCopy.setColor(new Color(0, 255, 200, 100));
            gCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            gCopy.fillOval(-size - 5, -size - 5, (size + 5) * 2, (size + 5) * 2);
            
            // Draw capsule body (bright cyan)
            gCopy.setColor(new Color(0, 255, 200, 200));
            gCopy.fillOval(-size, -size, size * 2, size * 2);
            
            // Draw inner core (white)
            gCopy.setColor(new Color(255, 255, 255, 255));
            gCopy.fillOval(-size / 2, -size / 2, size, size);
            
            // Draw energy symbol (+)
            gCopy.setColor(new Color(0, 200, 150, 255));
            gCopy.setStroke(new BasicStroke(2.0f));
            gCopy.drawLine(-4, 0, 4, 0);
            gCopy.drawLine(0, -4, 0, 4);
            
            // CRITICAL: Reset composite after drawing
            gCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        } finally {
            // CRITICAL: Dispose copy to prevent transform/Composite accumulation
            gCopy.dispose();
        }
    }
    
    /**
     * Get the energy value of this capsule.
     * 
     * @return the energy value
     */
    static int getEnergyValue()
    {
        return ENERGY_VALUE;
    }
}

