package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;

import java.util.List;

/**
 * Smart camera / auto-director system for spectator experience.
 * 
 * <p>MISSION 4.1: Smart Camera / Auto-Director
 * Calculates the centroid of all active bullets and robots.
 * Centers the view there. Zooms in when combat is close quarters,
 * zooms out when spread out.
 * 
 * <p>Makes the battle "Esports-ready" and fun to watch.
 * 
 * @author Robot Wars Team
 */
public class AutoDirector
{
    private static final double MIN_ZOOM = 0.5; // 50% zoom (zoomed out)
    private static final double MAX_ZOOM = 1.5; // 150% zoom (zoomed in)
    private static final double DEFAULT_ZOOM = 1.0; // 100% zoom
    private static final double ZOOM_SMOOTHING = 0.1; // Smoothing factor for zoom changes
    private static final double PAN_SMOOTHING = 0.15; // Smoothing factor for pan changes
    
    private double currentZoom;
    private double targetZoom;
    private double panX; // Pan offset in pixels
    private double panY;
    private double targetPanX;
    private double targetPanY;
    
    /**
     * Constructor
     */
    public AutoDirector()
    {
        this.currentZoom = DEFAULT_ZOOM;
        this.targetZoom = DEFAULT_ZOOM;
        this.panX = 0.0;
        this.panY = 0.0;
        this.targetPanX = 0.0;
        this.targetPanY = 0.0;
    }
    
    /**
     * Update the auto-director based on current battle state.
     * 
     * @param robots list of all robots
     * @param bullets list of all active bullets
     */
    public void update(List<Droid> robots, List<Bullet> bullets)
    {
        // Filter to only alive robots
        List<Droid> aliveRobots = robots.stream()
            .filter(r -> r.getEnergy() > 0)
            .toList();
        
        if (aliveRobots.isEmpty() && bullets.isEmpty()) {
            // No action - reset to default
            targetZoom = DEFAULT_ZOOM;
            targetPanX = 0.0;
            targetPanY = 0.0;
            return;
        }
        
        // Calculate centroid of all interesting entities
        double totalX = 0.0;
        double totalY = 0.0;
        int count = 0;
        
        // Add robots to centroid
        for (Droid robot : aliveRobots) {
            Location loc = robot.getLocation();
            totalX += loc.getX();
            totalY += loc.getY();
            count++;
        }
        
        // Add bullets to centroid (weighted less)
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                Location loc = bullet.getLocation();
                totalX += loc.getX() * 0.5; // Bullets weighted 50%
                totalY += loc.getY() * 0.5;
                count += 0.5;
            }
        }
        
        if (count > 0) {
            double centroidX = totalX / count;
            double centroidY = totalY / count;
            
            // Calculate pan to center on centroid
            double fieldCenterX = BattleSetup.FIELD_WIDTH / 2.0;
            double fieldCenterY = BattleSetup.FIELD_HEIGHT / 2.0;
            
            targetPanX = centroidX - fieldCenterX;
            targetPanY = centroidY - fieldCenterY;
            
            // Clamp pan to field bounds
            double maxPanX = BattleSetup.FIELD_WIDTH / 2.0;
            double maxPanY = BattleSetup.FIELD_HEIGHT / 2.0;
            targetPanX = Math.max(-maxPanX, Math.min(maxPanX, targetPanX));
            targetPanY = Math.max(-maxPanY, Math.min(maxPanY, targetPanY));
        }
        
        // Calculate zoom based on spread of entities
        if (aliveRobots.size() > 1 || !bullets.isEmpty()) {
            double minX = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = Double.MIN_VALUE;
            
            // Find bounding box of all entities
            for (Droid robot : aliveRobots) {
                Location loc = robot.getLocation();
                minX = Math.min(minX, loc.getX());
                maxX = Math.max(maxX, loc.getX());
                minY = Math.min(minY, loc.getY());
                maxY = Math.max(maxY, loc.getY());
            }
            
            for (Bullet bullet : bullets) {
                if (bullet.isActive()) {
                    Location loc = bullet.getLocation();
                    minX = Math.min(minX, loc.getX());
                    maxX = Math.max(maxX, loc.getX());
                    minY = Math.min(minY, loc.getY());
                    maxY = Math.max(maxY, loc.getY());
                }
            }
            
            double width = maxX - minX;
            double height = maxY - minY;
            double maxDimension = Math.max(width, height);
            
            // Calculate zoom: smaller spread = zoom in, larger spread = zoom out
            double fieldSize = Math.max(BattleSetup.FIELD_WIDTH, BattleSetup.FIELD_HEIGHT);
            double spreadRatio = maxDimension / fieldSize;
            
            // Invert: low spread = high zoom, high spread = low zoom
            targetZoom = MIN_ZOOM + (MAX_ZOOM - MIN_ZOOM) * (1.0 - spreadRatio);
            targetZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, targetZoom));
        } else {
            targetZoom = DEFAULT_ZOOM;
        }
        
        // Smooth zoom and pan changes
        currentZoom += (targetZoom - currentZoom) * ZOOM_SMOOTHING;
        panX += (targetPanX - panX) * PAN_SMOOTHING;
        panY += (targetPanY - panY) * PAN_SMOOTHING;
    }
    
    /**
     * Get the current zoom level.
     * 
     * @return zoom level (1.0 = 100%, >1.0 = zoomed in, <1.0 = zoomed out)
     */
    public double getZoom()
    {
        return currentZoom;
    }
    
    /**
     * Get the current pan offset in X direction.
     * 
     * @return pan offset in pixels
     */
    public double getPanX()
    {
        return panX;
    }
    
    /**
     * Get the current pan offset in Y direction.
     * 
     * @return pan offset in pixels
     */
    public double getPanY()
    {
        return panY;
    }
    
    /**
     * Reset the auto-director to default state.
     */
    public void reset()
    {
        currentZoom = DEFAULT_ZOOM;
        targetZoom = DEFAULT_ZOOM;
        panX = 0.0;
        panY = 0.0;
        targetPanX = 0.0;
        targetPanY = 0.0;
    }
}

