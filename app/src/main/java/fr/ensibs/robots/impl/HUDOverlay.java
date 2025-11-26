package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.util.List;

/**
 * Heads-Up Display (HUD) overlay that displays robot statistics on the battlefield.
 * 
 * <p>Shows:
 * <ul>
 *   <li>Energy level</li>
 *   <li>Gun heat</li>
 *   <li>Robot name</li>
 *   <li>Additional stats for robots (radar info)</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
public class HUDOverlay
{
    private static final int HUD_OFFSET_Y = -35; // Offset above robot
    private static final int HUD_WIDTH = 80;
    private static final int HUD_HEIGHT = 30;
    private static final Font HUD_FONT = new Font("Arial", Font.BOLD, 10);
    private static final Font NAME_FONT = new Font("Arial", Font.BOLD, 9);
    
    /**
     * Draw HUD overlay for all robots.
     * 
     * @param g2d the graphics context
     * @param views the robot views to display HUD for
     */
    public void draw(Graphics2D g2d, List<DroidView<? extends Droid>> views)
    {
        // Save original composite for transparency
        Composite originalComposite = g2d.getComposite();
        
        // Draw HUD for each robot
        for (DroidView<? extends Droid> view : views) {
            Droid robot = view.getRobot();
            if (robot.getEnergy() > 0) { // Only show HUD for alive robots
                drawRobotHUD(g2d, view, robot);
            }
        }
        
        // Restore composite
        g2d.setComposite(originalComposite);
    }
    
    /**
     * Draw HUD for a single robot.
     */
    private void drawRobotHUD(Graphics2D g2d, DroidView<? extends Droid> view, Droid robot)
    {
        Location location = robot.getLocation();
        int x = location.getX() - HUD_WIDTH / 2;
        int y = location.getY() + HUD_OFFSET_Y;
        
        // Draw semi-transparent background
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.setColor(new Color(0, 0, 0, 200)); // Dark background
        g2d.fillRoundRect(x, y, HUD_WIDTH, HUD_HEIGHT, 5, 5);
        
        // Draw border
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(view.getColor());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, HUD_WIDTH, HUD_HEIGHT, 5, 5);
        
        // Draw robot name
        g2d.setFont(NAME_FONT);
        g2d.setColor(Color.WHITE);
        String name = view.getName();
        if (name.length() > 12) {
            name = name.substring(0, 10) + "..";
        }
        g2d.drawString(name, x + 3, y + 11);
        
        // Draw energy bar
        drawEnergyBar(g2d, x + 3, y + 13, HUD_WIDTH - 6, 6, robot);
        
        // Draw gun heat (if applicable)
        if (robot instanceof Robot) {
            Robot fullRobot = (Robot) robot;
            drawGunHeat(g2d, x + 3, y + 20, HUD_WIDTH - 6, 4, fullRobot);
        } else {
            // For droids, show gun heat too
            drawGunHeat(g2d, x + 3, y + 20, HUD_WIDTH - 6, 4, robot);
        }
    }
    
    /**
     * Draw energy bar.
     */
    private void drawEnergyBar(Graphics2D g2d, int x, int y, int width, int height, Droid robot)
    {
        int energy = robot.getEnergy();
        int maxEnergy = robot instanceof Robot 
            ? BattleSetup.ROBOT_INITIAL_ENERGY 
            : BattleSetup.DROID_INITIAL_ENERGY;
        
        double energyRatio = Math.max(0.0, Math.min(1.0, energy / (double) maxEnergy));
        int fillWidth = (int) (width * energyRatio);
        
        // Background
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(x, y, width, height);
        
        // Energy fill
        if (fillWidth > 0) {
            Color energyColor = energyRatio > 0.5 ? Color.GREEN 
                              : energyRatio > 0.25 ? Color.YELLOW 
                              : Color.RED;
            g2d.setColor(energyColor);
            g2d.fillRect(x, y, fillWidth, height);
        }
        
        // Border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRect(x, y, width, height);
        
        // Energy text
        g2d.setFont(HUD_FONT);
        g2d.setColor(Color.WHITE);
        String energyText = Integer.toString(energy);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(energyText)) / 2;
        g2d.drawString(energyText, textX, y + height - 1);
    }
    
    /**
     * Draw gun heat indicator.
     */
    private void drawGunHeat(Graphics2D g2d, int x, int y, int width, int height, Droid robot)
    {
        int heat = robot.getGunHeat();
        // Maximum heat is typically 3 * MAX_FIRE_POWER (when firing at max power)
        int maxHeat = BattleSetup.MAX_FIRE_POWER * 3;
        double heatRatio = Math.max(0.0, Math.min(1.0, heat / (double) maxHeat));
        int fillWidth = (int) (width * heatRatio);
        
        // Background
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillRect(x, y, width, height);
        
        // Heat fill
        if (fillWidth > 0) {
            Color heatColor = heatRatio > 0.8 ? Color.RED 
                            : heatRatio > 0.5 ? Color.ORANGE 
                            : Color.YELLOW;
            g2d.setColor(heatColor);
            g2d.fillRect(x, y, fillWidth, height);
        }
        
        // Border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawRect(x, y, width, height);
    }
}

