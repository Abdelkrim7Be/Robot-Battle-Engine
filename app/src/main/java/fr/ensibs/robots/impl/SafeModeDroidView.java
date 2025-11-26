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
        
        Color teamColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            teamColor = new Color(50, 50, 50); // Dark gray when dead
        }
        
        // SAFE MODE: Simple rectangle, no transforms
        // 1. Draw Body (The Tank)
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect((int)x - 20, (int)y - 20, 40, 40); // 40x40 box centered
        
        g2d.setColor(teamColor); // Cyan or Red
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect((int)x - 20, (int)y - 20, 40, 40); // Outline
    }
    
    @Override
    protected void drawGun(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double x = location.getX();
        double y = location.getY();
        double gunHeading = getRobot().getGunHeading();
        
        // SAFE MODE: Simple rectangle with rotation
        // 2. Draw Gun (The Turret)
        AffineTransform old = g2d.getTransform();
        g2d.rotate(Math.toRadians(gunHeading), x, y);
        g2d.setColor(Color.GRAY);
        g2d.fillRect((int)x - 3, (int)y - 5, 6, 35); // Simple barrel
        g2d.setTransform(old); // RESET ROTATION
    }
}

