package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.view.RobotView;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Neon-themed robot view with radar field visualization.
 */
class NeonRobotView<R extends Robot> extends RobotView<R>
{
    
    NeonRobotView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }
    
    @Override
    protected void drawBody(Graphics2D g2d)
    {
        new NeonDroidView<>(getRobot(), getName(), getColor()).drawBody(g2d);
    }
    
    @Override
    protected void drawGun(Graphics2D g2d)
    {
        new NeonDroidView<>(getRobot(), getName(), getColor()).drawGun(g2d);
    }
    
    @Override
    protected void drawRadar(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double radarHeading = getRobot().getRadarHeading();
        
        AffineTransform originalTransform = g2d.getTransform();
        
        g2d.translate(location.getX(), location.getY());
        g2d.rotate(Math.toRadians(radarHeading));
        
        // Draw radar dish only (no field visualization to prevent trails)
        drawRadarDish(g2d);
        
        g2d.setTransform(originalTransform);
    }
    
    /**
     * FIX 1: SOLID GEOMETRY ONLY - Radar dish.
     * Radar: Thin Triangle on top of gun, WHITE color.
     */
    private void drawRadarDish(Graphics2D g2d)
    {
        // Radar: Thin triangle on top of gun
        int[] xPoints = {-5, 0, 5};
        int[] yPoints = {-30, -40, -30}; // Positioned above gun (35px gun length + 5px gap)
        
        // Color: WHITE (Solid, no transparency)
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Outline for visibility
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
    
}

