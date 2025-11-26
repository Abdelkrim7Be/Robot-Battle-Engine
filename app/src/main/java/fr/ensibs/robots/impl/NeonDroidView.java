package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.awt.geom.AffineTransform;

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
     * Draw tank chassis - CLEAR GEOMETRY, NO GLOW.
     * Body: Rectangle with team color outline.
     */
    private void drawTankChassis(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        int width = 40; // Fixed 40px as specified
        int height = 40; // Fixed 40px as specified
        
        Color teamColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            teamColor = new Color(50, 50, 50); // Dark gray when dead
        }
        
        // Body: Rectangle (40x40px)
        Rectangle body = new Rectangle(-width / 2, -height / 2, width, height);
        
        // Fill: Dark gray (or team color with 50% opacity)
        Color fillColor = new Color(teamColor.getRed(), teamColor.getGreen(), 
                                   teamColor.getBlue(), 128); // 50% opacity
        g2d.setColor(fillColor);
        g2d.fill(body);
        
        // Outline: 2px solid stroke in Team Color
        g2d.setColor(teamColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(body);
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
     * Draw turret barrel - CLEAR GEOMETRY, NO GLOW.
     * Gun: Long Rectangle/Line (4px wide, 30px long).
     */
    private void drawTurret(Graphics2D g2d)
    {
        // Gun: 4px wide, 30px long
        double gunWidth = 4.0;
        double gunLength = 30.0;
        
        // Fill: Gray
        g2d.setColor(Color.GRAY);
        Rectangle gun = new Rectangle((int) (-gunWidth / 2), (int) (-gunLength / 2), 
                                     (int) gunWidth, (int) gunLength);
        g2d.fill(gun);
    }
    
    /**
     * Draw health bar above robot (tiny horizontal line as specified).
     * Green if > 50%, Red if < 20%.
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
        
        // Tiny horizontal line (as specified)
        int fillWidth = (int) (ENERGY_BAR_WIDTH * energyRatio);
        if (fillWidth > 0) {
            // Color: Green if > 50%, Red if < 20%, Yellow otherwise
            Color energyColor;
            if (energyRatio > 0.5) {
                energyColor = Color.GREEN; // Green if > 50%
            } else if (energyRatio < 0.2) {
                energyColor = Color.RED; // Red if < 20%
            } else {
                energyColor = Color.YELLOW; // Yellow otherwise
            }
            
            // Draw tiny horizontal line
            g2d.setColor(energyColor);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(barX, barY, barX + fillWidth, barY);
        }
    }
}

