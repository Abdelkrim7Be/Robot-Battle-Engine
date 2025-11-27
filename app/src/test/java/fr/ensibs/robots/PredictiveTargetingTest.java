package fr.ensibs.robots;

import fr.ensibs.robots.impl.PredictiveTargeting;
import fr.ensibs.robots.logic.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for predictive targeting calculations.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Intercept calculations work for moving targets</li>
 *   <li>Stationary targets use simple aiming</li>
 *   <li>Bearing calculations are correct</li>
 *   <li>Velocity calculations work correctly</li>
 *   <li>Bullet speed calculations match Bullet class</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class PredictiveTargetingTest
{
    /**
     * Test intercept calculation for a target moving directly away.
     */
    @Test
    void testInterceptTargetMovingAway()
    {
        Location shooter = new Location(100, 100);
        Location target = new Location(200, 100); // Target is 100 pixels to the East
        double targetVelX = 5.0; // Moving East at 5 pixels/tick
        double targetVelY = 0.0;
        double bulletSpeed = 20.0; // Bullet speed 20 pixels/tick
        
        Double interceptHeading = PredictiveTargeting.calculateIntercept(
            shooter, target, targetVelX, targetVelY, bulletSpeed
        );
        
        assertNotNull(interceptHeading, "Intercept heading should not be null");
        assertTrue(interceptHeading >= 0 && interceptHeading < 360, 
            "Intercept heading should be in range [0, 360)");
        
        // Target is East (90°), so intercept should be close to 90° but slightly ahead
        assertTrue(interceptHeading > 85 && interceptHeading < 95,
            "Intercept heading should be approximately East (90°)");
    }
    
    /**
     * Test intercept calculation for a stationary target.
     */
    @Test
    void testInterceptStationaryTarget()
    {
        Location shooter = new Location(100, 100);
        Location target = new Location(200, 100); // Target is 100 pixels to the East
        double targetVelX = 0.0;
        double targetVelY = 0.0;
        double bulletSpeed = 20.0;
        
        Double interceptHeading = PredictiveTargeting.calculateIntercept(
            shooter, target, targetVelX, targetVelY, bulletSpeed
        );
        
        assertNotNull(interceptHeading, "Intercept heading should not be null");
        
        // For stationary target, should aim directly at it (90° = East)
        assertEquals(90.0, interceptHeading, 1.0,
            "Intercept heading for stationary target should be direct bearing");
    }
    
    /**
     * Test intercept calculation for a target moving towards shooter.
     */
    @Test
    void testInterceptTargetMovingTowards()
    {
        Location shooter = new Location(100, 100);
        Location target = new Location(200, 100); // Target is East of shooter
        double targetVelX = -5.0; // Moving West (towards shooter) at 5 pixels/tick
        double targetVelY = 0.0;
        double bulletSpeed = 20.0;
        
        Double interceptHeading = PredictiveTargeting.calculateIntercept(
            shooter, target, targetVelX, targetVelY, bulletSpeed
        );
        
        assertNotNull(interceptHeading, "Intercept heading should not be null");
        assertTrue(interceptHeading >= 0 && interceptHeading < 360,
            "Intercept heading should be in range [0, 360)");
    }
    
    /**
     * Test bearing calculation.
     */
    @Test
    void testCalculateBearing()
    {
        Location from = new Location(100, 100);
        
        // Test North (0°)
        Location north = new Location(100, 50);
        double bearingNorth = PredictiveTargeting.calculateBearing(from, north);
        assertEquals(0.0, bearingNorth, 1.0, "Bearing to North should be 0°");
        
        // Test East (90°)
        Location east = new Location(150, 100);
        double bearingEast = PredictiveTargeting.calculateBearing(from, east);
        assertEquals(90.0, bearingEast, 1.0, "Bearing to East should be 90°");
        
        // Test South (180°)
        Location south = new Location(100, 150);
        double bearingSouth = PredictiveTargeting.calculateBearing(from, south);
        assertEquals(180.0, bearingSouth, 1.0, "Bearing to South should be 180°");
        
        // Test West (270°)
        Location west = new Location(50, 100);
        double bearingWest = PredictiveTargeting.calculateBearing(from, west);
        assertEquals(270.0, bearingWest, 1.0, "Bearing to West should be 270°");
    }
    
    /**
     * Test velocity calculation from position samples.
     */
    @Test
    void testCalculateVelocity()
    {
        Location oldPos = new Location(100, 100);
        Location newPos = new Location(110, 105); // Moved 10 pixels East, 5 pixels South
        double timeDelta = 2.0; // 2 ticks
        
        double[] velocity = PredictiveTargeting.calculateVelocity(oldPos, newPos, timeDelta);
        
        assertEquals(2, velocity.length, "Velocity should have 2 components");
        assertEquals(5.0, velocity[0], 0.01, "Velocity X should be 5.0 pixels/tick");
        assertEquals(2.5, velocity[1], 0.01, "Velocity Y should be 2.5 pixels/tick");
    }
    
    /**
     * Test velocity calculation with zero time delta.
     */
    @Test
    void testCalculateVelocityZeroTime()
    {
        Location oldPos = new Location(100, 100);
        Location newPos = new Location(110, 105);
        double timeDelta = 0.0;
        
        double[] velocity = PredictiveTargeting.calculateVelocity(oldPos, newPos, timeDelta);
        
        assertEquals(2, velocity.length, "Velocity should have 2 components");
        assertEquals(0.0, velocity[0], 0.01, "Velocity X should be 0.0 for zero time");
        assertEquals(0.0, velocity[1], 0.01, "Velocity Y should be 0.0 for zero time");
    }
    
    /**
     * Test bullet speed calculation.
     */
    @Test
    void testCalculateBulletSpeed()
    {
        // Base speed is 20.0, plus 0.5 per power
        double speed1 = PredictiveTargeting.calculateBulletSpeed(1);
        assertEquals(20.5, speed1, 0.01, "Bullet speed for power 1 should be 20.5");
        
        double speed5 = PredictiveTargeting.calculateBulletSpeed(5);
        assertEquals(22.5, speed5, 0.01, "Bullet speed for power 5 should be 22.5");
        
        double speed10 = PredictiveTargeting.calculateBulletSpeed(10);
        assertEquals(25.0, speed10, 0.01, "Bullet speed for power 10 should be 25.0");
    }
    
    /**
     * Test intercept calculation for target at same position (edge case).
     */
    @Test
    void testInterceptSamePosition()
    {
        Location shooter = new Location(100, 100);
        Location target = new Location(100, 100); // Same position
        double targetVelX = 5.0;
        double targetVelY = 0.0;
        double bulletSpeed = 20.0;
        
        Double interceptHeading = PredictiveTargeting.calculateIntercept(
            shooter, target, targetVelX, targetVelY, bulletSpeed
        );
        
        // Should handle gracefully (may return null or a valid heading)
        if (interceptHeading != null) {
            assertTrue(interceptHeading >= 0 && interceptHeading < 360,
                "Intercept heading should be in range [0, 360) if not null");
        }
    }
}

