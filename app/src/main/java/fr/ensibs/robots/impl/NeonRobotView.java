package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.view.RobotView;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

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
        double halfField = Math.toRadians(BattleSetup.VISION_FIELD / 2.0);
        
        // Draw radar field of vision (cone)
        drawRadarField(g2d, location, Math.toRadians(radarHeading), halfField);
        
        AffineTransform originalTransform = g2d.getTransform();
        
        g2d.translate(location.getX(), location.getY());
        g2d.rotate(Math.toRadians(radarHeading));
        
        // Draw radar dish
        drawRadarDish(g2d);
        
        g2d.setTransform(originalTransform);
    }
    
    /**
     * Draw radar dish with neon cyan color.
     */
    private void drawRadarDish(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        double dishRadius = radius * 0.9;
        double dishLength = RADAR_LENGTH;
        
        // Draw radar dish (semi-circle)
        Ellipse2D radarDish = new Ellipse2D.Double(
            -dishRadius / 2.0, -dishLength, dishRadius, dishRadius);
        
        // Neon cyan color
        Color radarColor = new Color(0, 255, 255, 200); // Bright cyan
        
        // Glow
        g2d.setColor(new Color(0, 200, 200, 100));
        Ellipse2D glow = new Ellipse2D.Double(
            -dishRadius / 2.0 - 2, -dishLength - 2,
            dishRadius + 4, dishRadius + 4);
        g2d.fill(glow);
        
        // Main dish
        g2d.setColor(radarColor);
        g2d.fill(radarDish);
        
        // Border
        g2d.setColor(new Color(0, 255, 255));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(radarDish);
        
        // Beam line (bright yellow)
        g2d.setColor(new Color(255, 255, 0, 220));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawLine(0, 0, 0, (int) -dishLength);
    }
    
    /**
     * Draw radar field of vision as a semi-transparent cone.
     */
    private void drawRadarField(Graphics2D g2D, Location location, double heading, double halfField)
    {
        double startAngle = Math.toDegrees(heading - halfField);
        double arcAngle = Math.toDegrees(halfField * 2);
        int radius = (int) (RADAR_LENGTH * 1.8);
        int arcX = location.getX() - radius;
        int arcY = location.getY() - radius;
        int diameter = radius * 2;
        
        // Save composite
        Composite original = g2D.getComposite();
        
        // Draw semi-transparent cone
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, RADAR_FIELD_ALPHA));
        g2D.setColor(new Color(0, 255, 255)); // Cyan
        
        // Draw arc (pie slice)
        Arc2D arc = new Arc2D.Double(arcX, arcY, diameter, diameter,
                                    startAngle, arcAngle, Arc2D.PIE);
        g2D.fill(arc);
        
        // Draw border
        g2D.setComposite(original);
        g2D.setColor(new Color(0, 200, 200, 150));
        g2D.setStroke(new BasicStroke(2.0f));
        g2D.draw(arc);
    }
}

