package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;

import java.awt.*;
import java.util.Random;

/**
 * Represents a robot wreckage (dead robot that remains on the battlefield).
 * 
 * <p>MISSION 2.3: Persistent Debris - The "Graveyard" Effect
 * When a robot dies, it doesn't just disappear. It becomes wreckage:
 * <ul>
 *   <li>Darker color (50% opacity of original team color)</li>
 *   <li>Non-collidable (doesn't block movement or bullets)</li>
 *   <li>Occasional sparks for visual interest</li>
 *   <li>Remains on field until battle ends</li>
 * </ul>
 * 
 * <p>By the end of the match, the field is littered with the corpses
 * of the losers. Tells a story.
 * 
 * @author Robot Wars Team
 */
class RobotWreckage
{
    private final Location location;
    private final Color originalTeamColor;
    private final double bodyHeading;
    private final double gunHeading;
    private final Random random;
    private int sparkTimer;
    private static final int SPARK_INTERVAL = 60; // Sparks every 60 frames
    
    /**
     * Constructor
     * 
     * @param location the location where the robot died
     * @param teamColor the original team color of the robot
     * @param bodyHeading the body heading when the robot died
     * @param gunHeading the gun heading when the robot died
     */
    RobotWreckage(Location location, Color teamColor, double bodyHeading, double gunHeading)
    {
        this.location = location;
        this.originalTeamColor = teamColor;
        this.bodyHeading = bodyHeading;
        this.gunHeading = gunHeading;
        this.random = new Random();
        this.sparkTimer = random.nextInt(SPARK_INTERVAL); // Random initial spark time
    }
    
    /**
     * Get the location of this wreckage.
     * 
     * @return the location
     */
    Location getLocation()
    {
        return location;
    }
    
    /**
     * Get the wreckage color (darker version of team color).
     * 
     * @return the darkened team color
     */
    Color getWreckageColor()
    {
        // Darken the color: reduce brightness by 50% and add opacity
        int r = Math.max(0, originalTeamColor.getRed() / 2);
        int g = Math.max(0, originalTeamColor.getGreen() / 2);
        int b = Math.max(0, originalTeamColor.getBlue() / 2);
        return new Color(r, g, b, 180); // 70% opacity
    }
    
    /**
     * Update the wreckage (for spark timing).
     */
    void update()
    {
        sparkTimer++;
        if (sparkTimer >= SPARK_INTERVAL) {
            sparkTimer = 0;
        }
    }
    
    /**
     * Check if this wreckage should emit sparks this frame.
     * 
     * @return true if sparks should be emitted
     */
    boolean shouldEmitSparks()
    {
        return sparkTimer == 0;
    }
    
    /**
     * Draw the wreckage.
     * CRITICAL FIX: Use Graphics2D copy for isolated transforms.
     * 
     * @param g2d the graphics context
     */
    void draw(Graphics2D g2d)
    {
        int x = location.getX();
        int y = location.getY();
        
        // CRITICAL FIX: Use Graphics2D copy for body
        Graphics2D gBody = (Graphics2D) g2d.create();
        try {
            gBody.translate(x, y);
            
            // Draw body (darker, damaged look)
            double bodyHeadingRad = Math.toRadians(bodyHeading - 90); // Convert North->East
            gBody.rotate(bodyHeadingRad);
            
            Color wreckageColor = getWreckageColor();
            gBody.setColor(wreckageColor);
            gBody.setStroke(new BasicStroke(2)); // Thinner stroke for wreckage
            gBody.drawRect(-20, -20, 40, 40); // Outline
            
            // Fill with darker color
            gBody.setColor(new Color(wreckageColor.getRed(), wreckageColor.getGreen(), 
                wreckageColor.getBlue(), 150)); // More transparent
            gBody.fillRect(-20, -20, 40, 40);
        } finally {
            gBody.dispose(); // CRITICAL: Dispose copy
        }
        
        // CRITICAL FIX: Use Graphics2D copy for gun
        Graphics2D gGun = (Graphics2D) g2d.create();
        try {
            gGun.translate(x, y);
            double gunHeadingRad = Math.toRadians(gunHeading - 90);
            gGun.rotate(gunHeadingRad);
            Color wreckageColor = getWreckageColor();
            gGun.setColor(new Color(wreckageColor.getRed(), wreckageColor.getGreen(), 
                wreckageColor.getBlue(), 100)); // Even more transparent for gun
            gGun.setStroke(new BasicStroke(2));
            gGun.drawLine(0, 0, 35, 0);
        } finally {
            gGun.dispose(); // CRITICAL: Dispose copy
        }
        
        // MISSION 2.3: Draw occasional sparks
        if (shouldEmitSparks()) {
            // CRITICAL FIX: Use Graphics2D copy for sparks
            Graphics2D gSparks = (Graphics2D) g2d.create();
            try {
                gSparks.translate(x, y);
                drawSparks(gSparks);
            } finally {
                gSparks.dispose(); // CRITICAL: Dispose copy
            }
        }
    }
    
    /**
     * Draw sparks around the wreckage.
     * 
     * @param g2d the graphics context
     */
    private void drawSparks(Graphics2D g2d)
    {
        // Draw 3-5 small sparks around the wreckage
        int sparkCount = 3 + random.nextInt(3);
        for (int i = 0; i < sparkCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 15 + random.nextDouble() * 20; // 15-35 pixels from center
            double sparkX = Math.cos(angle) * distance;
            double sparkY = Math.sin(angle) * distance;
            
            // Draw small yellow-orange spark
            g2d.setColor(new Color(255, 200, 50, 200));
            g2d.fillOval((int)sparkX - 2, (int)sparkY - 2, 4, 4);
        }
    }
    
    /**
     * Get the body heading of the wreckage.
     * 
     * @return the body heading in degrees
     */
    double getBodyHeading()
    {
        return bodyHeading;
    }
    
    /**
     * Get the gun heading of the wreckage.
     * 
     * @return the gun heading in degrees
     */
    double getGunHeading()
    {
        return gunHeading;
    }
}

