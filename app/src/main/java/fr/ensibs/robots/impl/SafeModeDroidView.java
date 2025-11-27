package fr.ensibs.robots.impl;

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
        
        // MISSION 2: Team color injection based on robot name
        Color teamColor = determineTeamColor();
        if (getRobot().getEnergy() <= 0) {
            teamColor = new Color(50, 50, 50); // Dark gray when dead
        }
        
        // MISSION 1: Strict geometric representation - CAD schematic view
        // CRITICAL: Save transform - the panel has already applied battlefield coordinate transform
        AffineTransform old = g2d.getTransform();
        
        // Translate to robot center (in battlefield coordinates)
        g2d.translate(x, y);
        // Rotate around center
        g2d.rotate(Math.toRadians(bodyHeading));
        
        // Body: 36x36px Rectangle (strict geometric, no glow)
        int bodySize = 36;
        int halfBody = bodySize / 2;
        
        // Fill: Team color with transparency (50 alpha for visibility)
        Color fillColor = new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 50);
        if (getRobot().getEnergy() <= 0) {
            fillColor = new Color(50, 50, 50, 50); // Dark gray when dead
        }
        g2d.setColor(fillColor);
        g2d.fillRect(-halfBody, -halfBody, bodySize, bodySize);
        
        // Outline: Team color, 2px stroke (no glow effects)
        g2d.setColor(teamColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRect(-halfBody, -halfBody, bodySize, bodySize);
        
        g2d.setTransform(old); // RESET TRANSFORM
    }
    
    /**
     * MISSION 2: Determine team color based on robot name.
     * Duck -> CYAN (Blue), Snail -> RED, fallback to assigned color.
     */
    private Color determineTeamColor()
    {
        String name = getName().toLowerCase();
        if (name.contains("duck")) {
            return Color.CYAN;
        } else if (name.contains("snail")) {
            return Color.RED;
        }
        // Fallback to assigned color
        return getColor();
    }
    
    @Override
    protected void drawGun(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double x = location.getX();
        double y = location.getY();
        double gunHeading = getRobot().getGunHeading();
        
        // MISSION 1: Strict geometric representation - Gun as line/rectangle
        // CRITICAL: Save transform - the panel has already applied battlefield coordinate transform
        AffineTransform old = g2d.getTransform();
        
        // Translate to robot center (in battlefield coordinates)
        g2d.translate(x, y);
        // Rotate gun independently
        g2d.rotate(Math.toRadians(gunHeading));
        
        // Gun: 4px wide, 30px long rectangle (strict geometric, no glow)
        int gunWidth = 4;
        int gunLength = 30;
        
        // MISSION 2: Use team color for gun
        Color teamColor = determineTeamColor();
        
        // Draw gun barrel (extends upward from center)
        g2d.setColor(teamColor);
        g2d.setStroke(new BasicStroke(gunWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Draw as a line for cleaner appearance
        g2d.drawLine(0, 0, 0, -gunLength);
        
        g2d.setTransform(old); // RESET TRANSFORM
    }
}

