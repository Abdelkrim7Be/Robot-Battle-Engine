package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Safe Mode renderer - Simple, solid geometry only. No effects, no transparency, no blur.
 * 
 * @param <R> the droid type
 */
class SafeModeDroidView<R extends Droid> extends DroidView<R>
{
    SafeModeDroidView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }
    
    @Override
    protected void drawBody(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double x = location.getX();
        double y = location.getY();
        double bodyHeading = getRobot().getHeading();
        
        Color teamColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            teamColor = new Color(50, 50, 50); // Dark gray when dead
        }
        
        // SAFE MODE: Simple rectangle with body rotation
        // CRITICAL FIX: Translate to center, rotate, then draw centered at (0,0)
        AffineTransform old = g2d.getTransform();
        
        // Translate to robot center
        g2d.translate(x, y);
        // Rotate around center
        g2d.rotate(Math.toRadians(bodyHeading));
        
        // CRITICAL: Make robots MUCH larger and brighter for visibility
        // Draw body centered at (0,0) after translation
        int size = 60; // MUCH LARGER: 60x60 instead of 40x40
        int halfSize = size / 2;
        
        // Bright fill color (not dark gray - too hard to see!)
        g2d.setColor(new Color(100, 100, 100)); // Light gray fill
        g2d.fillRect(-halfSize, -halfSize, size, size);
        
        // Bright, thick outline in team color
        g2d.setColor(teamColor);
        g2d.setStroke(new BasicStroke(4)); // Thicker outline
        g2d.drawRect(-halfSize, -halfSize, size, size);
        
        g2d.setTransform(old); // RESET TRANSFORM
    }
    
    @Override
    protected void drawGun(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double x = location.getX();
        double y = location.getY();
        double gunHeading = getRobot().getGunHeading();
        
        // SAFE MODE: Simple rectangle with rotation
        // CRITICAL FIX: Translate to center, rotate, then draw centered
        AffineTransform old = g2d.getTransform();
        
        // Translate to robot center
        g2d.translate(x, y);
        // Rotate gun independently
        g2d.rotate(Math.toRadians(gunHeading));
        
        // Draw gun barrel (extends upward from center) - MUCH LARGER
        g2d.setColor(Color.LIGHT_GRAY);
        int gunWidth = 8; // Wider
        int gunLength = 50; // Longer
        g2d.fillRect(-gunWidth/2, -gunLength/2, gunWidth, gunLength);
        
        g2d.setTransform(old); // RESET TRANSFORM
    }
}

