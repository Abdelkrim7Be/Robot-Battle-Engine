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
        
        // CRITICAL FIX: Use high-contrast colors for visibility
        // Body: Dark gray fill with CYAN outline (high visibility on dark background)
        Color teamColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            teamColor = new Color(50, 50, 50); // Dark gray when dead
        }
        
        // Main body - DARK GRAY fill (visible on dark background)
        RoundRectangle2D body = new RoundRectangle2D.Double(
            -width / 2.0, -height / 2.0, width, height,
            radius * 0.4, radius * 0.4);
        
        // Fill with dark gray (high contrast)
        g2d.setColor(Color.DARK_GRAY);
        g2d.fill(body);
        
        // Outline with CYAN (high visibility)
        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(body);
        
        // Draw treads (side details) - lighter gray
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(1.5f));
        // Left tread
        g2d.drawLine(-width / 2, -height / 4, -width / 2, height / 4);
        // Right tread
        g2d.drawLine(width / 2, -height / 4, width / 2, height / 4);
        
        // Front indicator (direction arrow) - CYAN for visibility
        int[] xPoints = {radius, 0, -radius};
        int[] yPoints = {-height / 2 - 3, -height / 2 - 8, -height / 2 - 3};
        g2d.setColor(Color.CYAN);
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
     * CRITICAL: Use lighter gray for visibility on dark background.
     */
    private void drawTurret(Graphics2D g2d)
    {
        int radius = BattleSetup.ROBOT_RADIUS;
        double gunLength = radius * 3.0;
        double gunWidth = radius * 0.5;
        
        // Heat-based color: cool = light gray, hot = red-orange
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
            // Cool: LIGHT gray (was dark gray - too hard to see)
            int gray = 150 + (int) (50 * heatRatio);
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

