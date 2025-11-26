package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.view.RobotView;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Enhanced graphical representation for robots with improved radar visualization
 * showing the field of vision.
 */
class SimpleRobotView<R extends Robot> extends RobotView<R>
{
    private static final double RADAR_LENGTH = 40.0;
    private static final float RADAR_ARC_ALPHA = 0.15f;

    SimpleRobotView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }

    @Override
    protected void drawBody(Graphics2D g2d)
    {
        new SimpleDroidView<>(getRobot(), getName(), getColor()).drawBody(g2d);
    }

    @Override
    protected void drawGun(Graphics2D g2d)
    {
        new SimpleDroidView<>(getRobot(), getName(), getColor()).drawGun(g2d);
    }

    @Override
    protected void drawRadar(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double radarHeading = getRobot().getRadarHeading();
        double halfField = Math.toRadians(BattleSetup.VISION_FIELD / 2.0);
        
        // Draw radar field of vision arc
        drawRadarField(g2d, location, Math.toRadians(radarHeading), halfField);
        
        // Save original transform
        AffineTransform originalTransform = g2d.getTransform();
        
        // Translate to robot center
        g2d.translate(location.getX(), location.getY());
        
        // Rotate around center based on RADAR heading (independent of gun and body)
        g2d.rotate(Math.toRadians(radarHeading));
        
        // Draw radar dish
        drawRadarDish(g2d);
        
        // Restore transform
        g2d.setTransform(originalTransform);
    }
    
    /**
     * Draw the radar dish as a distinct shape.
     * This is drawn with the radar's rotation applied via AffineTransform,
     * which is independent of both gun and body rotations.
     */
    private void drawRadarDish(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        double dishRadius = radius * 0.8;
        double dishLength = RADAR_LENGTH;
        
        // Draw radar dish (semi-circle/arc shape)
        Shape radarDish = new Ellipse2D.Double(
            -dishRadius / 2.0, -dishLength, dishRadius, dishRadius);
        
        // Radar color (cyan/blue for visibility)
        g2d.setColor(new Color(0, 150, 255, 180)); // Semi-transparent cyan
        g2d.fill(radarDish);
        
        // Draw border
        g2d.setColor(new Color(0, 100, 200));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(radarDish);
        
        // Draw radar beam line
        g2d.setColor(new Color(255, 200, 0, 200)); // Yellow-orange beam
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(0, 0, 0, (int) -dishLength);
    }

    /**
     * Draws the radar's field of vision as a semi-transparent arc.
     */
    private void drawRadarField(Graphics2D g2d, Location location, double heading, double halfField)
    {
        double startAngle = Math.toDegrees(heading - halfField);
        double arcAngle = Math.toDegrees(halfField * 2);
        int radius = (int) (RADAR_LENGTH * 1.5);
        int arcX = location.getX() - radius;
        int arcY = location.getY() - radius;
        int diameter = radius * 2;
        
        // Save original composite
        AlphaComposite original = (AlphaComposite) g2d.getComposite();
        
        // Draw semi-transparent arc
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, RADAR_ARC_ALPHA));
        g2d.setColor(Color.RED);
        g2d.fillArc(arcX, arcY, diameter, diameter, 
                    (int) Math.round(startAngle), (int) Math.round(arcAngle));
        
        // Draw arc border
        g2d.setComposite(original);
        g2d.setColor(new Color(255, 0, 0, 100));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawArc(arcX, arcY, diameter, diameter, 
                   (int) Math.round(startAngle), (int) Math.round(arcAngle));
    }
}


