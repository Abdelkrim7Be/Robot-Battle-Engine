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
    private static final int ENERGY_BAR_HEIGHT = 2;
    private static final int ENERGY_BAR_WIDTH = 40; // Match body width
    private static final int MAX_ENERGY = BattleSetup.DROID_INITIAL_ENERGY;
    
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
     * FIX 1: SOLID GEOMETRY ONLY - High contrast, no blur/glow.
     * Body: 40x40px Rectangle, DARK_GRAY fill, Team Color border.
     */
    private void drawTankChassis(Graphics2D g2d)
    {
        // Exact dimensions as specified
        int width = 40; // 40px width
        int height = 40; // 40px height
        
        Color teamColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            teamColor = new Color(50, 50, 50); // Dark gray when dead
        }
        
        // Body: Rectangle (40x40px)
        Rectangle body = new Rectangle(-width / 2, -height / 2, width, height);
        
        // Fill: DARK_GRAY (Solid, no opacity tricks)
        g2d.setColor(Color.DARK_GRAY);
        g2d.fill(body);
        
        // Border: 2px Thick Line in Team Color (makes it visible against black)
        g2d.setColor(teamColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.draw(body);
        
        // Front indicator (small triangle to show direction)
        int[] xPoints = {0, -8, 8};
        int[] yPoints = {-height / 2, -height / 2 - 10, -height / 2};
        g2d.setColor(teamColor);
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
     * FIX 1: SOLID GEOMETRY ONLY - Gun turret.
     * Gun: 6px width, 35px length, LIGHT_GRAY fill, mounted at body center.
     */
    private void drawTurret(Graphics2D g2d)
    {
        // Exact dimensions as specified
        int gunWidth = 6; // 6px width
        int gunLength = 35; // 35px length
        
        // Gun: Rectangle (6px x 35px)
        Rectangle gun = new Rectangle(-gunWidth / 2, -gunLength / 2, gunWidth, gunLength);
        
        // Fill: LIGHT_GRAY (Solid)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fill(gun);
        
        // Border for visibility
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(gun);
    }
    
    /**
     * Draw health bar above robot (tiny horizontal line).
     * Green if > 50%, Red if < 20%.
     */
    private void drawNeonEnergyBar(Graphics2D g2d, Location location)
    {
        int energy = getRobot().getEnergy();
        int maxEnergy = getRobot() instanceof fr.ensibs.robots.logic.Robot 
            ? BattleSetup.ROBOT_INITIAL_ENERGY 
            : MAX_ENERGY;
        
        if (energy <= 0) {
            return; // Don't draw for dead robots
        }
        
        double energyRatio = Math.max(0.0, Math.min(1.0, energy / (double) maxEnergy));
        int barX = location.getX() - ENERGY_BAR_WIDTH / 2;
        int barY = location.getY() - 25; // Above 40px body
        
        // Tiny horizontal line
        int fillWidth = (int) (ENERGY_BAR_WIDTH * energyRatio);
        if (fillWidth > 0) {
            // Color: Green if > 50%, Red if < 20%, Yellow otherwise
            Color energyColor;
            if (energyRatio > 0.5) {
                energyColor = Color.GREEN;
            } else if (energyRatio < 0.2) {
                energyColor = Color.RED;
            } else {
                energyColor = Color.YELLOW;
            }
            
            // Draw tiny horizontal line (1px stroke for performance)
            g2d.setColor(energyColor);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawLine(barX, barY, barX + fillWidth, barY);
        }
    }
}

