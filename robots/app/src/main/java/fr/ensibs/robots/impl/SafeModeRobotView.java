package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.view.RobotView;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Safe Mode renderer for robots - Simple, solid geometry only.
 */
class SafeModeRobotView<R extends Robot> extends RobotView<R>
{
    SafeModeRobotView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }
    
    @Override
    protected void drawBody(Graphics2D g2d)
    {
        new SafeModeDroidView<>(getRobot(), getName(), getColor()).drawBody(g2d);
    }
    
    @Override
    protected void drawGun(Graphics2D g2d)
    {
        new SafeModeDroidView<>(getRobot(), getName(), getColor()).drawGun(g2d);
    }
    
    @Override
    protected void drawRadar(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double x = location.getX();
        double y = location.getY();
        double radarHeading = getRobot().getRadarHeading();
        
        // MISSION 3: Fix radar ghosting - only draw radar dish, no scan arc
        // CRITICAL: Save transform - the panel has already applied battlefield coordinate transform
        AffineTransform old = g2d.getTransform();
        
        // Translate to robot center (in battlefield coordinates)
        g2d.translate(x, y);
        // Rotate radar independently
        g2d.rotate(Math.toRadians(radarHeading));
        
        // MISSION 1: Radar as hollow circle or triangle (strict geometric)
        // Positioned above gun (30px gun length + 5px gap = 35px from center)
        int radarRadius = 8; // Small hollow circle
        int radarY = -35; // Position above gun
        
        // MISSION 2: Use team color for radar
        Color teamColor = determineTeamColor();
        
        // Draw radar as hollow circle (no fill, just outline)
        g2d.setColor(teamColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(-radarRadius, radarY - radarRadius, radarRadius * 2, radarRadius * 2);
        
        // Draw small triangle indicator pointing in scan direction (hollow)
        int[] xPoints = {-4, 0, 4};
        int[] yPoints = {radarY - radarRadius - 6, radarY - radarRadius - 12, radarY - radarRadius - 6};
        g2d.drawPolygon(xPoints, yPoints, 3);
        
        // MISSION 3: NO scan arc visualization - prevents ghosting
        // Only the radar dish is drawn, no transient scan effects
        
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
}

