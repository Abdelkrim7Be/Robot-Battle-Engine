package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.view.DroidView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * Enhanced graphical representation for a droid with energy bar and direction indicator.
 *
 * @param <R> the droid type
 */
class SimpleDroidView<R extends Droid> extends DroidView<R>
{
    private static final int ENERGY_BAR_HEIGHT = 3;
    private static final int ENERGY_BAR_WIDTH = BattleSetup.ROBOT_RADIUS * 2 + 4;
    private static final int MAX_ENERGY = BattleSetup.DROID_INITIAL_ENERGY;

    SimpleDroidView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }

    @Override
    protected void drawBody(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        
        // Draw energy bar above the robot
        drawEnergyBar(g2d, location);
        
        // Save original transform
        AffineTransform originalTransform = g2d.getTransform();
        
        // Translate to robot center
        g2d.translate(location.getX(), location.getY());
        
        // Rotate around center based on body heading
        double bodyHeading = getRobot().getHeading();
        g2d.rotate(Math.toRadians(bodyHeading));
        
        // Draw tank base (rectangular body shape)
        drawTankBase(g2d);
        
        // Restore transform
        g2d.setTransform(originalTransform);
    }
    
    /**
     * Draw the tank base (body) as a distinct rectangular shape.
     * This is drawn with the body's rotation applied via AffineTransform.
     */
    private void drawTankBase(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        int width = radius * 2;
        int height = (int) (radius * 1.6); // Slightly flattened
        
        // Body color (team color)
        Color bodyColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            bodyColor = Color.GRAY;
        }
        
        // Draw tank base as rounded rectangle
        Shape tankBase = new RoundRectangle2D.Double(
            -width / 2.0, -height / 2.0, width, height, radius * 0.3, radius * 0.3);
        
        g2d.setColor(bodyColor);
        g2d.fill(tankBase);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(tankBase);
        
        // Draw front indicator (small triangle pointing forward)
        int[] xPoints = {radius, 0, -radius};
        int[] yPoints = {-height / 2 - 2, -height / 2 - 6, -height / 2 - 2};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawPolygon(xPoints, yPoints, 3);
    }

    @Override
    protected void drawGun(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        
        // Save original transform
        AffineTransform originalTransform = g2d.getTransform();
        
        // Translate to robot center
        g2d.translate(location.getX(), location.getY());
        
        // Rotate around center based on GUN heading (independent of body)
        double gunHeading = getRobot().getGunHeading();
        g2d.rotate(Math.toRadians(gunHeading));
        
        // Draw turret (gun barrel)
        drawTurret(g2d);
        
        // Restore transform
        g2d.setTransform(originalTransform);
    }
    
    /**
     * Draw the turret (gun) as a distinct shape.
     * This is drawn with the gun's rotation applied via AffineTransform,
     * which is independent of the body rotation.
     */
    private void drawTurret(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        double gunLength = radius * 2.5;
        double gunWidth = radius * 0.4;
        
        // Gun color - red if overheated, dark gray otherwise
        Color gunColor = getRobot().getGunHeat() > 1 ? Color.RED : Color.DARK_GRAY;
        
        // Draw turret barrel as a rounded rectangle
        Shape turret = new RoundRectangle2D.Double(
            -gunWidth / 2.0, -gunLength / 2.0, gunWidth, gunLength, gunWidth * 0.5, gunWidth * 0.5);
        
        g2d.setColor(gunColor);
        g2d.fill(turret);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(turret);
        
        // Draw gun tip (muzzle)
        int tipSize = (int) (radius * 0.3);
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(-tipSize / 2, (int) (-gunLength / 2 - tipSize / 2), tipSize, tipSize);
    }

    /**
     * Draws an energy bar above the robot showing current energy level.
     */
    private void drawEnergyBar(Graphics2D g2d, Location location)
    {
        int energy = getRobot().getEnergy();
        int maxEnergy = getRobot() instanceof fr.ensibs.robots.logic.Robot 
            ? BattleSetup.ROBOT_INITIAL_ENERGY 
            : MAX_ENERGY;
        
        double energyRatio = Math.max(0.0, Math.min(1.0, energy / (double) maxEnergy));
        int barX = location.getX() - ENERGY_BAR_WIDTH / 2;
        int barY = location.getY() - BattleSetup.ROBOT_RADIUS - ENERGY_BAR_HEIGHT - 2;
        
        // Background
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(barX, barY, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);
        
        // Energy fill
        int fillWidth = (int) (ENERGY_BAR_WIDTH * energyRatio);
        if (fillWidth > 0) {
            Color energyColor = energyRatio > 0.5 ? Color.GREEN 
                              : energyRatio > 0.25 ? Color.YELLOW 
                              : Color.RED;
            g2d.setColor(energyColor);
            g2d.fillRect(barX, barY, fillWidth, ENERGY_BAR_HEIGHT);
        }
        
        // Border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRect(barX, barY, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT);
    }

}


