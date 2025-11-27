package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;

/**
 * Utility class for predictive targeting calculations.
 * 
 * <p>Implements linear predictive targeting to calculate where to aim
 * to intercept a moving target. Solves the interception triangle problem:
 * given shooter position, target position, target velocity, and bullet speed,
 * calculate the heading angle to aim at the predicted intercept point.
 * 
 * <p>This eliminates "random turning bullshit" by making robots aim where
 * targets will be, not where they currently are.
 * 
 * @author Robot Wars Team
 */
public final class PredictiveTargeting
{
    /**
     * Private constructor to prevent instantiation (utility class).
     */
    private PredictiveTargeting()
    {
        // Utility class - no instances
    }
    
    /**
     * Calculate the intercept heading angle to hit a moving target.
     * 
     * <p>Solves the interception problem:
     * <ul>
     *   <li>Bullet travels at constant speed in a straight line</li>
     *   <li>Target moves at constant velocity</li>
     *   <li>Find the heading angle that will cause bullet and target to meet</li>
     * </ul>
     * 
     * <p>The coordinate system uses:
     * <ul>
     *   <li>0° = North (negative Y direction)</li>
     *   <li>90° = East (positive X direction)</li>
     *   <li>180° = South (positive Y direction)</li>
     *   <li>270° = West (negative X direction)</li>
     * </ul>
     * 
     * @param shooterPos the position of the shooter (gun barrel tip)
     * @param targetPos the current position of the target
     * @param targetVelX the X component of target velocity (pixels per tick)
     * @param targetVelY the Y component of target velocity (pixels per tick)
     * @param bulletSpeed the speed of the bullet (pixels per tick)
     * @return the heading angle in degrees (0-360, where 0 = North), or null if no solution exists
     */
    public static Double calculateIntercept(
        Location shooterPos,
        Location targetPos,
        double targetVelX,
        double targetVelY,
        double bulletSpeed)
    {
        // Relative position vector (target relative to shooter)
        double dx = targetPos.getX() - shooterPos.getX();
        double dy = targetPos.getY() - shooterPos.getY();
        
        // If target is stationary, use simple aiming
        double targetSpeed = Math.hypot(targetVelX, targetVelY);
        if (targetSpeed < 0.01) {
            // Target is stationary - aim directly at it
            return calculateBearing(shooterPos, targetPos);
        }
        
        // If bullet is slower than target, interception may be impossible
        // (unless target is moving towards shooter)
        if (bulletSpeed < targetSpeed) {
            // Check if target is moving towards shooter (interception possible)
            double relativeVelX = targetVelX;
            double relativeVelY = targetVelY;
            double dotProduct = dx * relativeVelX + dy * relativeVelY;
            if (dotProduct > 0) {
                // Target moving away - interception may be impossible
                // Fall back to leading shot (aim ahead of target)
                return calculateLeadingShot(shooterPos, targetPos, targetVelX, targetVelY, bulletSpeed);
            }
        }
        
        // Solve the interception problem using quadratic equation
        // We need to find time t and angle θ such that:
        //   shooterPos + bulletSpeed * t * [sin(θ), -cos(θ)] = targetPos + [targetVelX, targetVelY] * t
        //
        // This gives us:
        //   (dx + targetVelX * t)² + (dy + targetVelY * t)² = (bulletSpeed * t)²
        //
        // Expanding and rearranging:
        //   (targetVelX² + targetVelY² - bulletSpeed²) * t² + 2 * (dx * targetVelX + dy * targetVelY) * t + (dx² + dy²) = 0
        
        double a = targetVelX * targetVelX + targetVelY * targetVelY - bulletSpeed * bulletSpeed;
        double b = 2.0 * (dx * targetVelX + dy * targetVelY);
        double c = dx * dx + dy * dy;
        
        // Solve quadratic: a*t² + b*t + c = 0
        double discriminant = b * b - 4.0 * a * c;
        
        if (discriminant < 0) {
            // No real solution - target moving too fast or in wrong direction
            // Fall back to leading shot
            return calculateLeadingShot(shooterPos, targetPos, targetVelX, targetVelY, bulletSpeed);
        }
        
        // Calculate the two possible solutions
        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDiscriminant) / (2.0 * a);
        double t2 = (-b + sqrtDiscriminant) / (2.0 * a);
        
        // Choose the positive solution (or the smaller positive one if both are positive)
        double t = Double.POSITIVE_INFINITY;
        if (t1 > 0 && t1 < t) {
            t = t1;
        }
        if (t2 > 0 && t2 < t) {
            t = t2;
        }
        
        if (t == Double.POSITIVE_INFINITY || t > 1000) {
            // No valid solution or solution too far in future
            // Fall back to leading shot
            return calculateLeadingShot(shooterPos, targetPos, targetVelX, targetVelY, bulletSpeed);
        }
        
        // Calculate intercept point
        double interceptX = targetPos.getX() + targetVelX * t;
        double interceptY = targetPos.getY() + targetVelY * t;
        
        // Calculate heading angle to intercept point
        return calculateBearing(shooterPos, new Location((int) Math.round(interceptX), (int) Math.round(interceptY)));
    }
    
    /**
     * Calculate a simple leading shot when exact interception is not possible.
     * Estimates where to aim based on target velocity and distance.
     * 
     * @param shooterPos the position of the shooter
     * @param targetPos the current position of the target
     * @param targetVelX the X component of target velocity
     * @param targetVelY the Y component of target velocity
     * @param bulletSpeed the speed of the bullet
     * @return the heading angle in degrees (0-360)
     */
    private static double calculateLeadingShot(
        Location shooterPos,
        Location targetPos,
        double targetVelX,
        double targetVelY,
        double bulletSpeed)
    {
        // Estimate time to target based on distance and bullet speed
        double dx = targetPos.getX() - shooterPos.getX();
        double dy = targetPos.getY() - shooterPos.getY();
        double distance = Math.hypot(dx, dy);
        
        if (distance < 0.01) {
            // Target is at shooter position - aim at current position
            return 0.0;
        }
        
        // Estimate travel time
        double estimatedTime = distance / bulletSpeed;
        
        // Predict target position
        double predictedX = targetPos.getX() + targetVelX * estimatedTime;
        double predictedY = targetPos.getY() + targetVelY * estimatedTime;
        
        // Aim at predicted position
        return calculateBearing(shooterPos, new Location((int) Math.round(predictedX), (int) Math.round(predictedY)));
    }
    
    /**
     * Calculate the bearing angle from one location to another.
     * 
     * @param from the source location
     * @param to the target location
     * @return the bearing angle in degrees (0-360, where 0 = North)
     */
    public static double calculateBearing(Location from, Location to)
    {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        
        // Calculate angle using atan2 (returns -180 to 180)
        // Convert to 0-360 range where 0 = North
        double angle = Math.toDegrees(Math.atan2(dx, -dy));
        if (angle < 0) {
            angle += 360.0;
        }
        
        return angle;
    }
    
    /**
     * Calculate target velocity from two position samples.
     * Useful for robots that track enemy positions over time.
     * 
     * @param oldPos the previous position of the target
     * @param newPos the current position of the target
     * @param timeDelta the time difference between samples (in game ticks)
     * @return array [velocityX, velocityY] in pixels per tick
     */
    public static double[] calculateVelocity(Location oldPos, Location newPos, double timeDelta)
    {
        if (timeDelta <= 0) {
            return new double[]{0.0, 0.0};
        }
        
        double dx = newPos.getX() - oldPos.getX();
        double dy = newPos.getY() - oldPos.getY();
        
        return new double[]{dx / timeDelta, dy / timeDelta};
    }
    
    /**
     * Calculate bullet speed based on power.
     * Matches the formula used in Bullet class.
     * 
     * @param power the bullet power
     * @return the bullet speed in pixels per tick
     */
    public static double calculateBulletSpeed(int power)
    {
        double BASE_BULLET_SPEED = 20.0;
        return BASE_BULLET_SPEED + (power * 0.5);
    }
}

