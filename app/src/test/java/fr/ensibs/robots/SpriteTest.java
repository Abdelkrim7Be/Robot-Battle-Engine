package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for sprite rendering and affine transformations.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Affine transforms correctly rotate body, gun, and radar independently</li>
 *   <li>Transformation matrices calculate correctly</li>
 *   <li>Each component maintains its own rotation state</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class SpriteTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test that body rotation is applied correctly via AffineTransform.
     */
    @Test
    void testBodyRotationTransform()
    {
        Droid droid = factory.makeDroid();
        
        // Set body heading to 90 degrees (East)
        droid.turnRobot(90.0 - droid.getHeading());
        
        // Verify heading is set
        assertEquals(90.0, droid.getHeading(), 0.1,
            "Body heading should be 90 degrees");
        
        // Create transform and verify rotation
        AffineTransform transform = new AffineTransform();
        transform.translate(droid.getLocation().getX(), droid.getLocation().getY());
        transform.rotate(Math.toRadians(droid.getHeading()));
        
        // Test that a point at (1, 0) in local space rotates to (0, 1) in world space
        // (90 degrees rotation: x becomes y, y becomes -x)
        Point2D localPoint = new Point2D.Double(1, 0);
        Point2D worldPoint = new Point2D.Double();
        transform.transform(localPoint, worldPoint);
        
        // After 90° rotation, (1, 0) should become approximately (centerX, centerY + 1)
        double expectedX = droid.getLocation().getX();
        double expectedY = droid.getLocation().getY() + 1;
        
        assertEquals(expectedX, worldPoint.getX(), 0.1,
            "X coordinate should match after 90° rotation");
        assertEquals(expectedY, worldPoint.getY(), 0.1,
            "Y coordinate should match after 90° rotation");
    }

    /**
     * Test that gun rotation is independent of body rotation.
     */
    @Test
    void testGunRotationIndependent()
    {
        Robot robot = factory.makeRobot();
        
        // Set body to 0 degrees (North)
        robot.turnRobot(0.0 - robot.getHeading());
        
        // Set gun to 90 degrees (East) - independent of body
        robot.turnGun(90.0 - robot.getGunHeading());
        
        // Verify they are independent
        assertEquals(0.0, robot.getHeading(), 0.1,
            "Body heading should be 0 degrees");
        assertEquals(90.0, robot.getGunHeading(), 0.1,
            "Gun heading should be 90 degrees (independent of body)");
        
        // Create transforms
        AffineTransform bodyTransform = new AffineTransform();
        bodyTransform.translate(robot.getLocation().getX(), robot.getLocation().getY());
        bodyTransform.rotate(Math.toRadians(robot.getHeading()));
        
        AffineTransform gunTransform = new AffineTransform();
        gunTransform.translate(robot.getLocation().getX(), robot.getLocation().getY());
        gunTransform.rotate(Math.toRadians(robot.getGunHeading()));
        
        // Test that gun transform is different from body transform
        Point2D testPoint = new Point2D.Double(0, -1); // Point forward in local space
        
        Point2D bodyResult = new Point2D.Double();
        bodyTransform.transform(testPoint, bodyResult);
        
        Point2D gunResult = new Point2D.Double();
        gunTransform.transform(testPoint, gunResult);
        
        // Results should be different (gun rotated 90° more than body)
        assertNotEquals(bodyResult.getX(), gunResult.getX(), 0.1,
            "Gun transform should produce different X than body transform");
        assertNotEquals(bodyResult.getY(), gunResult.getY(), 0.1,
            "Gun transform should produce different Y than body transform");
    }

    /**
     * Test that radar rotation is independent of both body and gun.
     */
    @Test
    void testRadarRotationIndependent()
    {
        Robot robot = factory.makeRobot();
        
        // Set body to 0 degrees
        robot.turnRobot(0.0 - robot.getHeading());
        
        // Set gun to 45 degrees
        robot.turnGun(45.0 - robot.getGunHeading());
        
        // Set radar to 180 degrees (South) - independent of both
        robot.turnRadar(180.0 - robot.getRadarHeading());
        
        // Verify all three are independent
        assertEquals(0.0, robot.getHeading(), 0.1,
            "Body heading should be 0 degrees");
        assertEquals(45.0, robot.getGunHeading(), 0.1,
            "Gun heading should be 45 degrees");
        assertEquals(180.0, robot.getRadarHeading(), 0.1,
            "Radar heading should be 180 degrees (independent of body and gun)");
        
        // Create transforms for each
        AffineTransform bodyTransform = new AffineTransform();
        bodyTransform.translate(robot.getLocation().getX(), robot.getLocation().getY());
        bodyTransform.rotate(Math.toRadians(robot.getHeading()));
        
        AffineTransform gunTransform = new AffineTransform();
        gunTransform.translate(robot.getLocation().getX(), robot.getLocation().getY());
        gunTransform.rotate(Math.toRadians(robot.getGunHeading()));
        
        AffineTransform radarTransform = new AffineTransform();
        radarTransform.translate(robot.getLocation().getX(), robot.getLocation().getY());
        radarTransform.rotate(Math.toRadians(robot.getRadarHeading()));
        
        // All three should produce different results
        Point2D testPoint = new Point2D.Double(0, -1);
        
        Point2D bodyResult = new Point2D.Double();
        bodyTransform.transform(testPoint, bodyResult);
        
        Point2D gunResult = new Point2D.Double();
        gunTransform.transform(testPoint, gunResult);
        
        Point2D radarResult = new Point2D.Double();
        radarTransform.transform(testPoint, radarResult);
        
        // Verify all three are different
        assertTransformsDiffer(bodyResult, gunResult, "Body and gun transforms should differ");
        assertTransformsDiffer(gunResult, radarResult, "Gun and radar transforms should differ");
        assertTransformsDiffer(bodyResult, radarResult, "Body and radar transforms should differ");
    }

    /**
     * Test that transformation matrices are correctly calculated for rotation.
     */
    @Test
    void testTransformationMatrixCalculation()
    {
        // Test rotation matrix calculation
        double angle = 45.0; // 45 degrees
        double radians = Math.toRadians(angle);
        
        // Expected rotation matrix for 45°:
        // [cos(45)  -sin(45)]   [√2/2  -√2/2]
        // [sin(45)   cos(45)] = [√2/2   √2/2]
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        // Verify cosine and sine values
        assertEquals(Math.sqrt(2) / 2, cos, 0.001,
            "cos(45°) should be √2/2");
        assertEquals(Math.sqrt(2) / 2, sin, 0.001,
            "sin(45°) should be √2/2");
        
        // Create transform and verify matrix elements
        AffineTransform transform = new AffineTransform();
        transform.rotate(radians);
        
        // Get matrix elements
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        // matrix = [m00, m10, m01, m11, m02, m12]
        // For rotation: [cos, sin, -sin, cos, 0, 0]
        
        assertEquals(cos, matrix[0], 0.001, "m00 (cos) should match");
        assertEquals(sin, matrix[1], 0.001, "m10 (sin) should match");
        assertEquals(-sin, matrix[2], 0.001, "m01 (-sin) should match");
        assertEquals(cos, matrix[3], 0.001, "m11 (cos) should match");
    }

    /**
     * Test that multiple rotations accumulate correctly.
     */
    @Test
    void testMultipleRotations()
    {
        Droid droid = factory.makeDroid();
        droid.turnRobot(-droid.getHeading());
        
        // Rotate body multiple times
        droid.turnRobot(30.0);
        droid.turnRobot(30.0);
        droid.turnRobot(30.0);
        
        // Should have rotated 90 degrees total
        assertEquals(90.0, droid.getHeading(), 0.1,
            "Three 30° rotations should result in 90°");
    }
    private void assertTransformsDiffer(Point2D first, Point2D second, String message)
    {
        boolean differs = Math.abs(first.getX() - second.getX()) > 0.1
            || Math.abs(first.getY() - second.getY()) > 0.1;
        assertTrue(differs, message);
    }
}

