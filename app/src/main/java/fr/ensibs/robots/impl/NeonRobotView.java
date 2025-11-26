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
    private static final double RADAR_LENGTH = 45.0;
    private static final float RADAR_FIELD_ALPHA = 0.2f;
    
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
     * Draw radar dish - CLEAR GEOMETRY, NO GLOW.
     * Radar: Triangle or Arc sitting on top of the Gun.
     */
    private void drawRadarDish(Graphics2D g2d)
    {
        // Radar: Triangle sitting on top of gun
        int[] xPoints = {-6, 0, 6};
        int[] yPoints = {-25, -35, -25}; // Positioned above gun
        
        // Color: White
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Outline
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * Draw radar field of vision - REMOVED to prevent trail artifacts.
     * Radar cone should not persist between frames.
     */
    private void drawRadarField(Graphics2D g2D, Location location, double heading, double halfField)
    {
        // DISABLED: Radar field visualization removed to prevent trail artifacts
        // The radar dish triangle is sufficient to show radar direction
    }
}

