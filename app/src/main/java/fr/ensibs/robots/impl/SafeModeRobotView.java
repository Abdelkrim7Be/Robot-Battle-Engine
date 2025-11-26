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
        
        // SAFE MODE: Simple circle
        // 3. Draw Radar (The Dish)
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(radarHeading), x, y);
        g2d.setColor(Color.WHITE);
        g2d.drawOval((int)x - 10, (int)y - 10, 20, 20); // Simple circle
        g2d.setTransform(old); // RESET ROTATION
    }
}

