package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.view.RobotView;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

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
        double heading = Math.toRadians(getRobot().getRadarHeading());
        double halfField = Math.toRadians(BattleSetup.VISION_FIELD / 2.0);
        
        // Draw radar field of vision arc
        drawRadarField(g2d, location, heading, halfField);
        
        // Draw radar line
        double dx = Math.sin(heading) * RADAR_LENGTH;
        double dy = -Math.cos(heading) * RADAR_LENGTH;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(location.getX(), location.getY(), 
                     (int) Math.round(location.getX() + dx), 
                     (int) Math.round(location.getY() + dy));
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


