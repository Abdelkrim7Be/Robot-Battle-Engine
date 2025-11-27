package fr.ensibs.robots.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks incoming bullets and calculates evasion vectors.
 * 
 * <p>MISSION D: Evasion System
 * Allows robots to detect and dodge incoming bullets.
 * 
 * @author Robot Wars Team
 */
public class BulletTracker {
    
    private static class TrackedBullet {
        double x, y;           // Current position
        double vx, vy;         // Velocity vector
        double speed;
        long trackedSince;
        boolean dangerous;     // Will it hit me?
        
        TrackedBullet(double x, double y, double vx, double vy, long tick) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.speed = Math.sqrt(vx * vx + vy * vy);
            this.trackedSince = tick;
            this.dangerous = false;
        }
        
        void update() {
            x += vx;
            y += vy;
        }
    }
    
    private List<TrackedBullet> incomingBullets = new ArrayList<>();
    private long currentTick = 0;
    
    // My position (updated each tick)
    private double myX, myY;
    private double myRadius = 20; // Robot hitbox radius
    
    // Evasion recommendation
    private double recommendedEvasionAngle = 0;
    private boolean shouldEvade = false;
    
    public void update(double myX, double myY, long tick) {
        this.myX = myX;
        this.myY = myY;
        this.currentTick = tick;
        
        // Update all tracked bullets
        Iterator<TrackedBullet> iter = incomingBullets.iterator();
        while (iter.hasNext()) {
            TrackedBullet bullet = iter.next();
            bullet.update();
            
            // Remove bullets that are too old or far away
            double dist = Math.sqrt(Math.pow(bullet.x - myX, 2) + Math.pow(bullet.y - myY, 2));
            if (dist > 800 || (currentTick - bullet.trackedSince) > 120) {
                iter.remove();
                continue;
            }
            
            // Check if bullet is headed toward us
            bullet.dangerous = willBulletHitMe(bullet);
        }
        
        // Calculate evasion
        calculateEvasion();
    }
    
    /**
     * Register a new bullet we've detected
     */
    public void trackBullet(double x, double y, double vx, double vy) {
        // Don't double-track
        for (TrackedBullet b : incomingBullets) {
            double dist = Math.sqrt(Math.pow(b.x - x, 2) + Math.pow(b.y - y, 2));
            if (dist < 10) return; // Already tracking
        }
        
        incomingBullets.add(new TrackedBullet(x, y, vx, vy, currentTick));
    }
    
    /**
     * Check if a bullet will hit us
     */
    private boolean willBulletHitMe(TrackedBullet bullet) {
        // Vector from bullet to me
        double dx = myX - bullet.x;
        double dy = myY - bullet.y;
        
        // Normalize bullet velocity
        double bulletSpeed = bullet.speed;
        if (bulletSpeed < 0.001) return false;
        
        double nvx = bullet.vx / bulletSpeed;
        double nvy = bullet.vy / bulletSpeed;
        
        // Project my position onto bullet's path
        double dot = dx * nvx + dy * nvy;
        
        // If dot < 0, bullet is moving away
        if (dot < 0) return false;
        
        // Closest point on bullet's path to me
        double closestX = bullet.x + nvx * dot;
        double closestY = bullet.y + nvy * dot;
        
        // Distance from that point to me
        double closestDist = Math.sqrt(Math.pow(closestX - myX, 2) + Math.pow(closestY - myY, 2));
        
        // Will hit if within my radius (plus margin)
        return closestDist < (myRadius + 10);
    }
    
    /**
     * Calculate which way to dodge
     */
    private void calculateEvasion() {
        shouldEvade = false;
        recommendedEvasionAngle = 0;
        
        // Find the most dangerous incoming bullet
        TrackedBullet mostDangerous = null;
        double minDist = Double.MAX_VALUE;
        
        for (TrackedBullet bullet : incomingBullets) {
            if (bullet.dangerous) {
                double dist = Math.sqrt(Math.pow(bullet.x - myX, 2) + Math.pow(bullet.y - myY, 2));
                if (dist < minDist) {
                    minDist = dist;
                    mostDangerous = bullet;
                }
            }
        }
        
        if (mostDangerous != null && minDist < 300) {
            shouldEvade = true;
            
            // Calculate perpendicular direction to bullet path
            double bulletAngle = Math.atan2(mostDangerous.vy, mostDangerous.vx);
            
            // Perpendicular is +/- 90 degrees
            // Choose randomly or based on which side has more space
            double perpAngle1 = bulletAngle + Math.PI / 2;
            double perpAngle2 = bulletAngle - Math.PI / 2;
            
            // Pick one (could be smarter about this)
            recommendedEvasionAngle = Math.random() > 0.5 ? perpAngle1 : perpAngle2;
        }
    }
    
    public boolean shouldEvade() {
        return shouldEvade;
    }
    
    public double getEvasionAngle() {
        return Math.toDegrees(recommendedEvasionAngle);
    }
    
    public int getDangerousBulletCount() {
        int count = 0;
        for (TrackedBullet b : incomingBullets) {
            if (b.dangerous) count++;
        }
        return count;
    }
    
    public void clear() {
        incomingBullets.clear();
    }
}

