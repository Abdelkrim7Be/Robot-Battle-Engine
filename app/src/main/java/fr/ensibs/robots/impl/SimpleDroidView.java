package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.view.DroidView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

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
        int diameter = BattleSetup.ROBOT_RADIUS * 2;
        int topLeftX = location.getX() - BattleSetup.ROBOT_RADIUS;
        int topLeftY = location.getY() - BattleSetup.ROBOT_RADIUS;
        
        // Draw energy bar above the robot
        drawEnergyBar(g2d, location);
        
        // Draw robot body with gradient effect
        Color bodyColor = getColor();
        if (getRobot().getEnergy() <= 0) {
            bodyColor = Color.GRAY;
        }
        g2d.setColor(bodyColor);
        g2d.fillOval(topLeftX, topLeftY, diameter, diameter);
        
        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(topLeftX, topLeftY, diameter, diameter);
        
        // Draw direction indicator (small line showing body heading)
        drawDirectionIndicator(g2d, location);
    }

    @Override
    protected void drawGun(Graphics2D g2d)
    {
        Location location = getRobot().getLocation();
        double heading = Math.toRadians(getRobot().getGunHeading());
        double gunLength = BattleSetup.ROBOT_RADIUS * 2.5;
        double dx = Math.sin(heading) * gunLength;
        double dy = -Math.cos(heading) * gunLength;
        
        // Draw gun with different color if overheated
        if (getRobot().getGunHeat() > 1) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.DARK_GRAY);
        }
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawLine(location.getX(), location.getY(), 
                     (int) Math.round(location.getX() + dx), 
                     (int) Math.round(location.getY() + dy));
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

    /**
     * Draws a small line indicating the body's heading direction.
     */
    private void drawDirectionIndicator(Graphics2D g2d, Location location)
    {
        double heading = Math.toRadians(getRobot().getHeading());
        double indicatorLength = BattleSetup.ROBOT_RADIUS * 0.6;
        double dx = Math.sin(heading) * indicatorLength;
        double dy = -Math.cos(heading) * indicatorLength;
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(location.getX(), location.getY(),
                     (int) Math.round(location.getX() + dx),
                     (int) Math.round(location.getY() + dy));
    }
}


