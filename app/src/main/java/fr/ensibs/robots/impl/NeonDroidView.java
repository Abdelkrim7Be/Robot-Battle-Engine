package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * Neon-themed droid view with cyber-military aesthetic.
 * Features distinct body, gun, and energy bar with neon team colors.
 * 
 * @param <R> the droid type
 */
class NeonDroidView<R extends Droid> extends DroidView<R>
{
    private static final int ENERGY_BAR_HEIGHT = 4;
    private static final int ENERGY_BAR_WIDTH = BattleSetup.ROBOT_RADIUS * 2 + 6;
    private static final int MAX_ENERGY = BattleSetup.DROID_INITIAL_ENERGY;
    
    // Neon glow effect
    private static final int GLOW_RADIUS = 2;
    
    NeonDroidView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }
    
    @Override
    protected void drawBody(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        
        // Draw energy bar above robot
        drawNeonEnergyBar(g2d, location);
        
        AffineTransform originalTransform = g2d.getTransform();
        
        // Translate to robot center
        g2d.translate(location.getX(), location.getY());
        
        // Rotate based on body heading
        double bodyHeading = getRobot().getHeading();
        g2d.rotate(Math.toRadians(bodyHeading));
        
        // Draw tank chassis with neon glow
        drawTankChassis(g2d);
        
        g2d.setTransform(originalTransform);
    }
    
    /**
     * Draw tank chassis with neon team color and treads.
     */
    private void drawTankChassis(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        int width = radius * 2;
        int height = (int) (radius * 1.8);
        
        // Team color with neon effect
        Color teamColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            teamColor = new Color(50, 50, 50); // Dark gray when dead
        }
        
        // Outer glow
        g2d.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), 
                              teamColor.getBlue(), 100));
        g2d.fill(new RoundRectangle2D.Double(
            -width / 2.0 - GLOW_RADIUS, -height / 2.0 - GLOW_RADIUS,
            width + GLOW_RADIUS * 2, height + GLOW_RADIUS * 2,
            radius * 0.4, radius * 0.4));
        
        // Main body
        RoundRectangle2D body = new RoundRectangle2D.Double(
            -width / 2.0, -height / 2.0, width, height,
            radius * 0.4, radius * 0.4);
        
        g2d.setColor(teamColor);
        g2d.fill(body);
        
        // Bright border (neon effect)
        g2d.setColor(new Color(
            Math.min(255, teamColor.getRed() + 50),
            Math.min(255, teamColor.getGreen() + 50),
            Math.min(255, teamColor.getBlue() + 50)
        ));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(body);
        
        // Draw treads (side details)
        g2d.setColor(new Color(teamColor.getRed() / 2, teamColor.getGreen() / 2, 
                              teamColor.getBlue() / 2));
        g2d.setStroke(new BasicStroke(1.5f));
        // Left tread
        g2d.drawLine(-width / 2, -height / 4, -width / 2, height / 4);
        // Right tread
        g2d.drawLine(width / 2, -height / 4, width / 2, height / 4);
        
        // Front indicator (direction arrow)
        int[] xPoints = {radius, 0, -radius};
        int[] yPoints = {-height / 2 - 3, -height / 2 - 8, -height / 2 - 3};
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    @Override
    protected void drawGun(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        
        AffineTransform originalTransform = g2d.getTransform();
        
        g2d.translate(location.getX(), location.getY());
        
        // Rotate based on gun heading (independent of body)
        double gunHeading = getRobot().getGunHeading();
        g2d.rotate(Math.toRadians(gunHeading));
        
        // Draw turret with heat indicator
        drawTurret(g2d);
        
        g2d.setTransform(originalTransform);
    }
    
    /**
     * Draw turret barrel with heat-based color.
     */
    private void drawTurret(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        double gunLength = radius * 3.0;
        double gunWidth = radius * 0.5;
        
        // Heat-based color: cool = dark gray, hot = red-orange
        int gunHeat = getRobot().getGunHeat();
        int maxHeat = 50; // BattleSetup.MAX_GUN_HEAT
        float heatRatio = Math.min(1.0f, gunHeat / (float) maxHeat);
        
        Color gunColor;
        if (heatRatio > 0.5f) {
            // Hot: red-orange gradient
            int red = 255;
            int green = (int) (255 * (1.0f - heatRatio));
            int blue = 0;
            gunColor = new Color(red, green, blue);
        } else {
            // Cool: dark gray to light gray
            int gray = 80 + (int) (100 * heatRatio);
            gunColor = new Color(gray, gray, gray);
        }
        
        // Draw turret barrel
        RoundRectangle2D turret = new RoundRectangle2D.Double(
            -gunWidth / 2.0, -gunLength / 2.0, gunWidth, gunLength,
            gunWidth * 0.5, gunWidth * 0.5);
        
        // Glow for hot guns
        if (heatRatio > 0.3f) {
            g2d.setColor(new Color(gunColor.getRed(), gunColor.getGreen(), 
                                  gunColor.getBlue(), 80));
            g2d.fill(new RoundRectangle2D.Double(
                -gunWidth / 2.0 - 1, -gunLength / 2.0 - 1,
                gunWidth + 2, gunLength + 2,
                gunWidth * 0.5, gunWidth * 0.5));
        }
        
        g2d.setColor(gunColor);
        g2d.fill(turret);
        
        // Border
        g2d.setColor(new Color(
            Math.min(255, gunColor.getRed() + 30),
            Math.min(255, gunColor.getGreen() + 30),
            Math.min(255, gunColor.getBlue() + 30)
        ));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(turret);
        
        // Muzzle tip
        int tipSize = (int) (radius * 0.4);
        g2d.setColor(new Color(200, 200, 100));
        g2d.fillOval(-tipSize / 2, (int) (-gunLength / 2 - tipSize / 2), 
                     tipSize, tipSize);
    }
    
    /**
     * Draw neon energy bar above robot.
     */
    private void drawNeonEnergyBar(Graphics2D g2d, Location location)
    {
        int energy = getRobot().getEnergy();
        int maxEnergy = getRobot() instanceof fr.ensibs.robots.logic.Robot 
            ? BattleSetup.ROBOT_INITIAL_ENERGY 
            : MAX_ENERGY;
        
        double energyRatio = Math.max(0.0, Math.min(1.0, energy / (double) maxEnergy));
        int barX = location.getX() - ENERGY_BAR_WIDTH / 2;
        int barY = location.getY() - BattleSetup.ROBOT_RADIUS - ENERGY_BAR_HEIGHT - 4;
        
        // Background (dark)
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(barX - 1, barY - 1, ENERGY_BAR_WIDTH + 2, ENERGY_BAR_HEIGHT + 2);
        
        // Energy fill with neon color gradient
        int fillWidth = (int) (ENERGY_BAR_WIDTH * energyRatio);
        if (fillWidth > 0) {
            Color energyColor;
            if (energyRatio > 0.6) {
                energyColor = new Color(0, 255, 100); // Bright green
            } else if (energyRatio > 0.3) {
                energyColor = new Color(255, 200, 0); // Yellow
            } else {
                energyColor = new Color(255, 50, 50); // Red
            }
            
            // Glow effect
            g2d.setColor(new Color(energyColor.getRed(), energyColor.getGreen(), 
                                  energyColor.getBlue(), 100));
            g2d.fillRect(barX, barY - 1, fillWidth, ENERGY_BAR_HEIGHT + 2);
            
            // Main bar
            g2d.setColor(energyColor);
            g2d.fillRect(barX, barY, fillWidth, ENERGY_BAR_HEIGHT);
            
            // Bright border
            g2d.setColor(new Color(
                Math.min(255, energyColor.getRed() + 50),
                Math.min(255, energyColor.getGreen() + 50),
                Math.min(255, energyColor.getBlue() + 50)
            ));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRect(barX, barY, fillWidth, ENERGY_BAR_HEIGHT);
        }
        
        // Border
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect(barX - 1, barY - 1, ENERGY_BAR_WIDTH + 2, ENERGY_BAR_HEIGHT + 2);
    }
}

